package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.PedidoEntity;
import com.example.order.domain.enums.PedidoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    
    Optional<PedidoEntity> findByExternalId(String externalId);
    
    List<PedidoEntity> findByStatus(PedidoStatus status);
    
    @Query("SELECT p FROM PedidoEntity p WHERE p.status = :status AND p.createdAt >= :dataInicio")
    List<PedidoEntity> findByStatusAndDataInicio(@Param("status") PedidoStatus status, 
                                                @Param("dataInicio") java.time.LocalDateTime dataInicio);
    
    boolean existsByExternalId(String externalId);
} 