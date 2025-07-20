package com.example.order.infrastructure.controller;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.service.PedidoService;
import com.example.order.domain.entity.PedidoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PedidoEntity> criarPedido(@RequestBody @Valid PedidoDTO pedidoDTO) {
        log.info("Recebendo requisição para criar pedido: {}", pedidoDTO.getExternalId());
        
        PedidoEntity pedido = pedidoService.processarPedido(pedidoDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
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
} 