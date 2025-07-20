package com.example.order.infrastructure.messaging;

import com.example.order.domain.entity.PedidoEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPedidoProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC_PEDIDOS_PROCESSADOS = "pedidos.processados";
    
    @Retryable(maxAttempts = 3)
    public CompletableFuture<SendResult<String, String>> enviarPedidoProcessado(PedidoEntity pedido) {
        try {
            String json = objectMapper.writeValueAsString(pedido);
            log.info("Enviando pedido processado para Kafka: {}", pedido.getExternalId());
            
            return kafkaTemplate.send(TOPIC_PEDIDOS_PROCESSADOS, pedido.getExternalId(), json)
                    .completable()
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("Erro ao enviar pedido para Kafka: {}", throwable.getMessage());
                        } else {
                            log.info("Pedido enviado com sucesso para Kafka: {}", pedido.getExternalId());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar pedido: {}", e.getMessage());
            throw new RuntimeException("Erro ao serializar pedido", e);
        }
    }
} 