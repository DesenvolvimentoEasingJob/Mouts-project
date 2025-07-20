package com.example.order;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.dto.ProdutoDTO;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.infrastructure.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"pedidos.recebidos", "pedidos.processados"})
class OrderServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Test
    void deveProcessarPedidoCompletoViaAPI() {
        // Given
        ProdutoDTO produto1 = ProdutoDTO.builder()
                .nome("Produto Teste 1")
                .preco(new BigDecimal("15.99"))
                .build();
        
        ProdutoDTO produto2 = ProdutoDTO.builder()
                .nome("Produto Teste 2")
                .preco(new BigDecimal("25.50"))
                .build();
        
        PedidoDTO pedidoDTO = PedidoDTO.builder()
                .externalId("TEST-INTEGRATION-001")
                .produtos(Arrays.asList(produto1, produto2))
                .build();
        
        // When
        ResponseEntity<PedidoEntity> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/pedidos",
                pedidoDTO,
                PedidoEntity.class
        );
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        PedidoEntity pedidoCriado = response.getBody();
        assertEquals("TEST-INTEGRATION-001", pedidoCriado.getExternalId());
        assertEquals(new BigDecimal("41.49"), pedidoCriado.getTotal());
        assertNotNull(pedidoCriado.getId());
        
        // Verificar se foi salvo no banco
        assertTrue(pedidoRepository.existsByExternalId("TEST-INTEGRATION-001"));
    }
    
    @Test
    void deveBuscarPedidoPorId() {
        // Given - Criar um pedido primeiro
        ProdutoDTO produto = ProdutoDTO.builder()
                .nome("Produto Busca")
                .preco(new BigDecimal("10.00"))
                .build();
        
        PedidoDTO pedidoDTO = PedidoDTO.builder()
                .externalId("TEST-BUSCA-001")
                .produtos(Arrays.asList(produto))
                .build();
        
        ResponseEntity<PedidoEntity> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/pedidos",
                pedidoDTO,
                PedidoEntity.class
        );
        
        Long pedidoId = createResponse.getBody().getId();
        
        // When
        ResponseEntity<PedidoEntity> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/pedidos/" + pedidoId,
                PedidoEntity.class
        );
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pedidoId, response.getBody().getId());
        assertEquals("TEST-BUSCA-001", response.getBody().getExternalId());
    }
    
    @Test
    void deveRetornarHealthCheck() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/pedidos/health",
                String.class
        );
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order Service is running!", response.getBody());
    }
} 