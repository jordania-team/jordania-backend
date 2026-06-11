package com.jordania.api.Tarefa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findAllByUsuarioIdOrderByCriadaEmDesc(UUID usuarioId);

    Optional<Tarefa> findByIdAndUsuarioId(Long id, UUID usuarioId);
}
