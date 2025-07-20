package com.example.order.application.service;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.dto.ProdutoDTO;
import com.example.order.application.mapper.PedidoMapper;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.entity.ProdutoEntity;
import com.example.order.domain.enums.PedidoStatus;
import com.example.order.infrastructure.messaging.KafkaPedidoProducer;
import com.example.order.infrastructure.repository.PedidoRepository;
import com.example.order.infrastructure.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    
    @Mock
    private PedidoRepository pedidoRepository;
    
    @Mock
    private ProdutoRepository produtoRepository;
    
    @Mock
    private PedidoMapper pedidoMapper;
    
    @Mock
    private KafkaPedidoProducer kafkaPedidoProducer;
    
    @InjectMocks
    private PedidoService pedidoService;
    
    private PedidoDTO pedidoDTO;
    private PedidoEntity pedidoEntity;
    private ProdutoEntity produtoEntity1;
    private ProdutoEntity produtoEntity2;
    
    @BeforeEach
    void setUp() {
        // Setup DTO
        ProdutoDTO produto1 = ProdutoDTO.builder()
                .nome("Produto 1")
                .preco(new BigDecimal("10.50"))
                .build();
        
        ProdutoDTO produto2 = ProdutoDTO.builder()
                .nome("Produto 2")
                .preco(new BigDecimal("20.00"))
                .build();
        
        pedidoDTO = PedidoDTO.builder()
                .externalId("EXT-001")
                .produtos(Arrays.asList(produto1, produto2))
                .build();
        
        // Setup Entity
        pedidoEntity = PedidoEntity.builder()
                .id(1L)
                .externalId("EXT-001")
                .status(PedidoStatus.PROCESSADO)
                .total(new BigDecimal("30.50"))
                .build();
        
        produtoEntity1 = ProdutoEntity.builder()
                .id(1L)
                .nome("Produto 1")
                .preco(new BigDecimal("10.50"))
                .pedidoId(1L)
                .build();
        
        produtoEntity2 = ProdutoEntity.builder()
                .id(2L)
                .nome("Produto 2")
                .preco(new BigDecimal("20.00"))
                .pedidoId(1L)
                .build();
    }
    
    @Test
    void deveProcessarPedidoComSucesso() {
        // Given
        when(pedidoRepository.existsByExternalId("EXT-001")).thenReturn(false);
        when(pedidoMapper.toEntity(pedidoDTO)).thenReturn(pedidoEntity);
        when(pedidoMapper.toEntity(any(ProdutoDTO.class))).thenReturn(produtoEntity1, produtoEntity2);
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(pedidoEntity);
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoEntity1, produtoEntity2);
        when(kafkaPedidoProducer.enviarPedidoProcessado(any())).thenReturn(null);
        
        // When
        PedidoEntity resultado = pedidoService.processarPedido(pedidoDTO);
        
        // Then
        assertNotNull(resultado);
        assertEquals("EXT-001", resultado.getExternalId());
        assertEquals(PedidoStatus.PROCESSADO, resultado.getStatus());
        
        verify(pedidoRepository).existsByExternalId("EXT-001");
        verify(pedidoRepository, times(2)).save(any(PedidoEntity.class)); // Uma vez para salvar o pedido, outra para atualizar com produtos
        verify(produtoRepository, times(2)).save(any(ProdutoEntity.class)); // Uma vez para cada produto
        verify(kafkaPedidoProducer).enviarPedidoProcessado(any(PedidoEntity.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoPedidoJaExiste() {
        // Given
        when(pedidoRepository.existsByExternalId("EXT-001")).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.processarPedido(pedidoDTO);
        });
        
        assertEquals("Pedido já existe: EXT-001", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
        verify(produtoRepository, never()).save(any());
        verify(kafkaPedidoProducer, never()).enviarPedidoProcessado(any());
    }
    
    @Test
    void deveBuscarPedidoPorIdComSucesso() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));
        
        // When
        PedidoEntity resultado = pedidoService.buscarPorId(1L);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("EXT-001", resultado.getExternalId());
    }
    
    @Test
    void deveLancarExcecaoQuandoPedidoNaoEncontradoPorId() {
        // Given
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.buscarPorId(999L);
        });
        
        assertEquals("Pedido não encontrado: 999", exception.getMessage());
    }
    
    @Test
    void deveBuscarPedidoPorExternalIdComSucesso() {
        // Given
        when(pedidoRepository.findByExternalId("EXT-001")).thenReturn(Optional.of(pedidoEntity));
        
        // When
        PedidoEntity resultado = pedidoService.buscarPorExternalId("EXT-001");
        
        // Then
        assertNotNull(resultado);
        assertEquals("EXT-001", resultado.getExternalId());
    }
    
    @Test
    void deveLancarExcecaoQuandoPedidoNaoEncontradoPorExternalId() {
        // Given
        when(pedidoRepository.findByExternalId("EXT-999")).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.buscarPorExternalId("EXT-999");
        });
        
        assertEquals("Pedido não encontrado: EXT-999", exception.getMessage());
    }
    
    @Test
    void deveSalvarPedidoPrimeiroEDepoisProdutos() {
        // Given
        PedidoEntity pedidoSemId = PedidoEntity.builder()
                .externalId("EXT-002")
                .status(PedidoStatus.PROCESSADO)
                .build();
        
        PedidoEntity pedidoComId = PedidoEntity.builder()
                .id(2L)
                .externalId("EXT-002")
                .status(PedidoStatus.PROCESSADO)
                .build();
        
        when(pedidoRepository.existsByExternalId("EXT-002")).thenReturn(false);
        when(pedidoMapper.toEntity(pedidoDTO)).thenReturn(pedidoSemId);
        when(pedidoMapper.toEntity(any(ProdutoDTO.class))).thenReturn(produtoEntity1, produtoEntity2);
        when(pedidoRepository.save(pedidoSemId)).thenReturn(pedidoComId);
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoEntity1, produtoEntity2);
        when(pedidoRepository.save(pedidoComId)).thenReturn(pedidoComId);
        when(kafkaPedidoProducer.enviarPedidoProcessado(any())).thenReturn(null);
        
        // When
        PedidoEntity resultado = pedidoService.processarPedido(pedidoDTO);
        
        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        
        // Verificar ordem das operações
        verify(pedidoRepository).save(pedidoSemId); // Primeiro salva o pedido sem produtos
        verify(produtoRepository, times(2)).save(any(ProdutoEntity.class)); // Depois salva os produtos
        verify(pedidoRepository).save(pedidoComId); // Por fim atualiza o pedido com produtos
    }
} 