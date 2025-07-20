package com.example.order.application.mapper;

import com.example.order.application.dto.PedidoDTO;
import com.example.order.application.dto.ProdutoDTO;
import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.entity.ProdutoEntity;
import com.example.order.domain.enums.PedidoStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "defaultStatus")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "produtos", source = "produtos")
    PedidoEntity toEntity(PedidoDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    ProdutoEntity toEntity(ProdutoDTO dto);
    
    List<ProdutoEntity> toEntityList(List<ProdutoDTO> dtos);
    
    @Named("defaultStatus")
    default PedidoStatus defaultStatus(PedidoStatus status) {
        return PedidoStatus.RECEBIDO;
    }
} 