package com.example.order.application.service;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.mapper.PedidoMapper;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.infrastructure.messaging.KafkaPedidoProducer;
import com.example.order.infrastructure.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {
    
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final KafkaPedidoProducer kafkaPedidoProducer;
    
    @Transactional
    public PedidoEntity processarPedido(PedidoDTO pedidoDTO) {
        log.info("Iniciando processamento do pedido: {}", pedidoDTO.getExternalId());
        
        // Verificar se o pedido já existe
        if (pedidoRepository.existsByExternalId(pedidoDTO.getExternalId())) {
            log.warn("Pedido já existe: {}", pedidoDTO.getExternalId());
            throw new RuntimeException("Pedido já existe: " + pedidoDTO.getExternalId());
        }
        
        try {
            // Converter DTO para entidade
            PedidoEntity pedido = pedidoMapper.toEntity(pedidoDTO);
            
            // Processar produtos
            pedidoDTO.getProdutos().forEach(produtoDTO -> {
                var produto = pedidoMapper.toEntity(produtoDTO);
                pedido.adicionarProduto(produto);
            });
            
            // Marcar como processado
            pedido.marcarComoProcessado();
            
            // Salvar no banco
            PedidoEntity pedidoSalvo = pedidoRepository.save(pedido);
            log.info("Pedido salvo com sucesso: {}", pedidoSalvo.getId());
            
            // Enviar para Kafka
            kafkaPedidoProducer.enviarPedidoProcessado(pedidoSalvo);
            
            return pedidoSalvo;
            
        } catch (Exception e) {
            log.error("Erro ao processar pedido: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }
    
    @Transactional(readOnly = true)
    public PedidoEntity buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
    }
    
    @Transactional(readOnly = true)
    public PedidoEntity buscarPorExternalId(String externalId) {
        return pedidoRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + externalId));
    }
} 