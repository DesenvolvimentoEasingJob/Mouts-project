package com.example.order.infrastructure.controller;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.dto.ProdutoDTO;
import com.example.order.application.service.PedidoService;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.enums.PedidoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PedidoService pedidoService;
    
    private PedidoDTO pedidoDTO;
    private PedidoEntity pedidoEntity;
    
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
    }
    
    @Test
    void deveCriarPedidoComSucesso() throws Exception {
        // Given
        when(pedidoService.processarPedido(any(PedidoDTO.class))).thenReturn(pedidoEntity);
        
        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.status").value("PROCESSADO"))
                .andExpect(jsonPath("$.total").value(30.50));
    }
    
    @Test
    void deveRetornarErroQuandoDadosInvalidos() throws Exception {
        // Given
        PedidoDTO pedidoInvalido = PedidoDTO.builder()
                .externalId("") // Inválido
                .produtos(Arrays.asList()) // Inválido
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoInvalido)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void deveBuscarPedidoPorIdComSucesso() throws Exception {
        // Given
        when(pedidoService.buscarPorId(1L)).thenReturn(pedidoEntity);
        
        // When & Then
        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }
    
    @Test
    void deveBuscarPedidoPorExternalIdComSucesso() throws Exception {
        // Given
        when(pedidoService.buscarPorExternalId("EXT-001")).thenReturn(pedidoEntity);
        
        // When & Then
        mockMvc.perform(get("/api/pedidos/external/EXT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }
    
    @Test
    void deveRetornarHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pedidos/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is running!"));
    }
} 