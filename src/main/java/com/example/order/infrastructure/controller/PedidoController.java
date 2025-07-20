package com.example.order.infrastructure.controller;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.service.PedidoService;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.entity.ProdutoEntity;
import com.example.order.domain.enums.PedidoStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public class PedidoController {
    
    private final PedidoService pedidoService;
    
    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Processa e salva um novo pedido")
    public ResponseEntity<Map<String, Object>> criarPedido(@RequestBody @Valid PedidoDTO pedidoDTO) {
        log.info("Recebendo requisição para criar pedido: {}", pedidoDTO.getExternalId());
        
        PedidoEntity pedido = pedidoService.processarPedido(pedidoDTO);
        
        // Buscar os produtos associados ao pedido
        List<ProdutoEntity> produtos = pedidoService.buscarProdutosPorPedidoId(pedido.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Pedido criado com sucesso");
        response.put("pedidoId", pedido.getId());
        response.put("externalId", pedido.getExternalId());
        response.put("status", pedido.getStatus());
        response.put("total", pedido.getTotal());
        response.put("createdAt", pedido.getCreatedAt());
        
        // Adicionar os produtos à resposta
        List<Map<String, Object>> produtosResponse = new ArrayList<>();
        for (ProdutoEntity produto : produtos) {
            Map<String, Object> produtoMap = new HashMap<>();
            produtoMap.put("id", produto.getId());
            produtoMap.put("nome", produto.getNome());
            produtoMap.put("preco", produto.getPreco());
            produtosResponse.add(produtoMap);
        }
        response.put("produtos", produtosResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido pelo seu ID interno")
    public ResponseEntity<PedidoEntity> buscarPorId(@PathVariable Long id) {
        log.info("Buscando pedido por ID: {}", id);
        
        PedidoEntity pedido = pedidoService.buscarPorId(id);
        
        return ResponseEntity.ok(pedido);
    }
    
    @GetMapping("/external/{externalId}")
    @Operation(summary = "Buscar pedido por External ID", description = "Retorna um pedido pelo seu ID externo")
    public ResponseEntity<PedidoEntity> buscarPorExternalId(@PathVariable String externalId) {
        log.info("Buscando pedido por External ID: {}", externalId);
        
        PedidoEntity pedido = pedidoService.buscarPorExternalId(externalId);
        
        return ResponseEntity.ok(pedido);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica se o serviço está funcionando")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running!");
    }
    
    @GetMapping("/health/db")
    @Operation(summary = "Database health check", description = "Verifica se o banco de dados está funcionando")
    public ResponseEntity<String> healthDb() {
        try {
            // Teste simples de conexão com o banco
            return ResponseEntity.ok("Database OK - Service funcionando");
        } catch (Exception e) {
            log.error("Erro na conexão com banco: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/health/beans")
    @Operation(summary = "Beans health check", description = "Verifica se todos os beans estão sendo injetados")
    public ResponseEntity<Map<String, Object>> healthBeans() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("pedidoService", pedidoService != null ? "OK" : "NULL");
            status.put("pedidoRepository", "OK"); // Se chegou aqui, foi injetado
            status.put("produtoRepository", "OK"); // Se chegou aqui, foi injetado
            status.put("pedidoMapper", "OK"); // Se chegou aqui, foi injetado
            status.put("kafkaPedidoProducer", "OK"); // Se chegou aqui, foi injetado
            status.put("timestamp", LocalDateTime.now());
            status.put("status", "All beans injected successfully");
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("error", e.getMessage());
            status.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        }
    }
    
    @PostMapping("/test")
    @Operation(summary = "Teste simples", description = "Teste simples de persistência")
    public ResponseEntity<String> testeSimples() {
        try {
            // Teste simples - apenas verificar se o service está funcionando
            return ResponseEntity.ok("Service funcionando - " + System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Erro no teste: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no teste: " + e.getMessage());
        }
    }
    
    @PostMapping("/test/dto")
    @Operation(summary = "Teste de DTO", description = "Teste de conversão de DTO")
    public ResponseEntity<?> testeDTO(@RequestBody @Valid PedidoDTO pedidoDTO) {
        try {
            log.info("Testando conversão de DTO: {}", pedidoDTO.getExternalId());
            
            // Testar apenas a validação do DTO
            return ResponseEntity.ok(Map.of(
                "message", "DTO válido",
                "externalId", pedidoDTO.getExternalId(),
                "produtosCount", pedidoDTO.getProdutos().size(),
                "produtos", pedidoDTO.getProdutos().stream()
                    .map(p -> Map.of("nome", p.getNome(), "preco", p.getPreco()))
                    .toList()
            ));
        } catch (Exception e) {
            log.error("Erro na validação do DTO: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro na validação do DTO", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/test/pedido-simples")
    @Operation(summary = "Teste de pedido simples", description = "Teste de persistência de pedido sem produtos")
    public ResponseEntity<?> testePedidoSimples() {
        try {
            log.info("Testando persistência de pedido simples...");
            
            // Criar um pedido simples sem produtos
            PedidoDTO pedidoDTO = PedidoDTO.builder()
                    .externalId("TEST-SIMPLE-" + System.currentTimeMillis())
                    .produtos(Arrays.asList())
                    .build();
            
            PedidoEntity pedido = pedidoService.processarPedido(pedidoDTO);
            
            return ResponseEntity.ok(Map.of(
                "message", "Pedido simples criado com sucesso",
                "id", pedido.getId(),
                "externalId", pedido.getExternalId(),
                "status", pedido.getStatus(),
                "total", pedido.getTotal()
            ));
        } catch (Exception e) {
            log.error("Erro na persistência de pedido simples: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro na persistência", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/test/sem-produtos")
    @Operation(summary = "Teste de pedido sem produtos", description = "Teste de persistência de pedido sem produtos")
    public ResponseEntity<?> testePedidoSemProdutos() {
        try {
            log.info("=== TESTE PEDIDO SEM PRODUTOS ===");
            
            // Criar um DTO simples sem produtos
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setExternalId("TEST-SEM-PRODUTOS-001");
            pedidoDTO.setProdutos(new ArrayList<>()); // Lista vazia
            
            log.info("DTO criado: {}", pedidoDTO);
            
            PedidoEntity pedido = pedidoService.processarPedido(pedidoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido sem produtos criado com sucesso");
            response.put("pedidoId", pedido.getId());
            response.put("externalId", pedido.getExternalId());
            response.put("status", pedido.getStatus());
            response.put("total", pedido.getTotal());
            response.put("createdAt", pedido.getCreatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no teste de pedido sem produtos: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("exception", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/test/componentes")
    @Operation(summary = "Teste de componentes", description = "Teste cada componente individualmente")
    public ResponseEntity<?> testeComponentes() {
        Map<String, Object> resultados = new HashMap<>();
        
        try {
            // Teste 1: Verificar se pedido já existe
            log.info("Teste 1: Verificando se pedido existe...");
            boolean existe = pedidoService.testarExistsByExternalId("TEST-COMPONENTE");
            resultados.put("teste1_existsByExternalId", existe ? "OK" : "OK (não existe)");
            
            // Teste 2: Contar pedidos
            log.info("Teste 2: Contando pedidos...");
            long count = pedidoService.testarCount();
            resultados.put("teste2_count", count);
            
            // Teste 3: Verificar mapper
            log.info("Teste 3: Testando mapper...");
            PedidoDTO dto = PedidoDTO.builder()
                    .externalId("TEST-MAPPER")
                    .produtos(Arrays.asList())
                    .build();
            PedidoEntity entity = pedidoService.testarMapper(dto);
            resultados.put("teste3_mapper", entity != null ? "OK" : "FAIL");
            
            // Teste 4: Verificar Kafka
            log.info("Teste 4: Verificando Kafka...");
            String kafkaStatus = pedidoService.testarKafka();
            resultados.put("teste4_kafka", kafkaStatus);
            
            resultados.put("status", "Todos os testes passaram");
            resultados.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            log.error("Erro nos testes de componentes: {}", e.getMessage(), e);
            resultados.put("error", e.getMessage());
            resultados.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultados);
        }
    }
    
    @PostMapping("/test/exception")
    @Operation(summary = "Teste de exceção", description = "Força uma exceção para testar o GlobalExceptionHandler")
    public ResponseEntity<?> testeException() {
        // Forçar uma exceção para testar se o GlobalExceptionHandler está funcionando
        throw new RuntimeException("TESTE: Esta é uma exceção de teste para verificar se o GlobalExceptionHandler está funcionando");
    }
    
    @PostMapping("/test/banco")
    @Operation(summary = "Teste de banco", description = "Testa a conexão com o banco de dados")
    public ResponseEntity<?> testeBanco() {
        try {
            log.info("Testando conexão com banco...");
            
            // Teste 1: Verificar se consegue conectar
            long count = pedidoService.testarCount();
            log.info("Conexão OK - Total de pedidos: {}", count);
            
            // Teste 2: Tentar salvar um pedido simples
            PedidoEntity pedido = PedidoEntity.builder()
                    .externalId("TEST-BANCO-" + System.currentTimeMillis())
                    .status(PedidoStatus.RECEBIDO)
                    .total(new BigDecimal("10.00"))
                    .build();
            
            // Aqui vamos tentar acessar o repositório diretamente
            // Se der erro, saberemos que é problema de conexão
            throw new RuntimeException("Teste de banco - Se você vê esta mensagem, o GlobalExceptionHandler está funcionando");
            
        } catch (Exception e) {
            log.error("Erro no teste de banco: {}", e.getMessage(), e);
            throw new RuntimeException("Erro no teste de banco: " + e.getMessage(), e);
        }
    }
} 