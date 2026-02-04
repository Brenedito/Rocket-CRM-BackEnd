package com.rocket.crm.repositories;

import com.rocket.crm.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {


    @Query("SELECT e FROM Empresa e JOIN User u ON e.empresa_id = u.tenant_id WHERE u.email = :email")
    Optional<Empresa> findByEmailUsuario(@Param("email") String email);

    @Query("SELECT e FROM Empresa e WHERE e.empresa_plano = :plano AND e.dataExpiracao < :agora")
    List<Empresa> findAllByPlanoAndDataExpiracaoBefore(
            @Param("plano") String plano,
            @Param("agora") LocalDateTime agora
    );

}