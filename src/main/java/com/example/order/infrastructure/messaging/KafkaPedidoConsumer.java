package com.example.order.infrastructure.messaging;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPedidoConsumer {
    
    private final PedidoService pedidoService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "pedidos.recebidos",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirPedido(@Payload String json, 
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.info("Recebendo pedido do Kafka - Topic: {}, Partition: {}, Offset: {}", 
                    topic, partition, offset);
            
            PedidoDTO pedidoDTO = objectMapper.readValue(json, PedidoDTO.class);
            log.info("Processando pedido recebido: {}", pedidoDTO.getExternalId());
            
            pedidoService.processarPedido(pedidoDTO);
            
            log.info("Pedido processado com sucesso: {}", pedidoDTO.getExternalId());
            
        } catch (Exception e) {
            log.error("Erro ao processar pedido recebido do Kafka: {}", e.getMessage(), e);
            // Aqui poderia implementar DLQ (Dead Letter Queue) ou retry logic
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }
} 