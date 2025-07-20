package com.example.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    
    @NotBlank(message = "External ID é obrigatório")
    private String externalId;
    
    @NotEmpty(message = "Lista de produtos não pode estar vazia")
    private List<@Valid ProdutoDTO> produtos;
} 