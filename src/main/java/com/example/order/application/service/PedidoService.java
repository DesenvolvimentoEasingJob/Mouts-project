package com.example.order.application.service;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.mapper.PedidoMapper;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.entity.ProdutoEntity;
import com.example.order.infrastructure.messaging.KafkaPedidoProducer;
import com.example.order.infrastructure.repository.PedidoRepository;
import com.example.order.infrastructure.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.order.application.dto.ProdutoDTO;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {
    
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoMapper pedidoMapper;
    private final KafkaPedidoProducer kafkaPedidoProducer;
    
    @Transactional
    public PedidoEntity processarPedido(PedidoDTO pedidoDTO) {
        log.info("=== INÍCIO DO PROCESSAMENTO ===");
        log.info("Iniciando processamento do pedido: {}", pedidoDTO.getExternalId());
        
        if (pedidoRepository.existsByExternalId(pedidoDTO.getExternalId())) {
            log.warn("Pedido já existe: {}", pedidoDTO.getExternalId());
            throw new RuntimeException("Pedido já existe: " + pedidoDTO.getExternalId());
        }
        
        try {
            log.info("=== CONVERTENDO DTO ===");
            PedidoEntity pedido = pedidoMapper.toEntity(pedidoDTO);
            log.info("DTO convertido com sucesso. Pedido: {}", pedido);
            
            pedido.marcarComoProcessado();
            log.info("Pedido marcado como processado");
            
            log.info("=== CALCULANDO TOTAL ===");
            BigDecimal total = pedidoDTO.getProdutos().stream()
                    .map(ProdutoDTO::getPreco)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            pedido.calcularTotal(total);
            log.info("Total calculado: {}", total);
            

            log.info("=== SALVANDO APENAS O PEDIDO ===");
            log.info("Pedido antes de salvar: {}", pedido);
            PedidoEntity pedidoSalvo = pedidoRepository.save(pedido);
            log.info("Pedido salvo com sucesso: {}", pedidoSalvo.getId());
            log.info("Pedido completo: {}", pedidoSalvo);
            
            log.info("=== PROCESSANDO PRODUTOS ===");
            log.info("Processando {} produtos...", pedidoDTO.getProdutos().size());
            for (int i = 0; i < pedidoDTO.getProdutos().size(); i++) {
                ProdutoDTO produtoDTO = pedidoDTO.getProdutos().get(i);
                log.info("Processando produto {}: {}", i + 1, produtoDTO.getNome());
                
                ProdutoEntity produto = pedidoMapper.toEntity(produtoDTO);
                produto.setPedidoId(pedidoSalvo.getId());
                
                log.info("Produto {} antes de salvar: {}", i + 1, produto);
                ProdutoEntity produtoSalvo = produtoRepository.save(produto);
                log.info("Produto {} salvo com sucesso, ID: {}", i + 1, produtoSalvo.getId());
            }
            
            log.info("=== PROCESSAMENTO CONCLUÍDO ===");
            log.info("Processamento do pedido {} concluído com sucesso", pedidoDTO.getExternalId());
            return pedidoSalvo;
            
        } catch (Exception e) {
            log.error("=== ERRO DETALHADO ===");
            log.error("Erro detalhado ao processar pedido: {}", e.getMessage(), e);
            
            String errorDetails = String.format(
                "Erro ao processar pedido %s: %s (Causa: %s)",
                pedidoDTO.getExternalId(),
                e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "N/A"
            );
            
            throw new RuntimeException(errorDetails, e);
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
    
    @Transactional(readOnly = true)
    public List<ProdutoEntity> buscarProdutosPorPedidoId(Long pedidoId) {
        return produtoRepository.findByPedidoId(pedidoId);
    }

    public boolean testarExistsByExternalId(String externalId) {
        return pedidoRepository.existsByExternalId(externalId);
    }
    
    public long testarCount() {
        return pedidoRepository.count();
    }
    
    public PedidoEntity testarMapper(PedidoDTO dto) {
        return pedidoMapper.toEntity(dto);
    }
    
    public String testarKafka() {
        return "Kafka bean injetado com sucesso";
    }
} 