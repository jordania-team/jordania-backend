package com.jordania.api.Tarefa.TarefaDTO;

import com.jordania.api.Tarefa.Tarefa;
import java.time.LocalDateTime;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        boolean concluida,
        LocalDateTime criadaEm
) {
    public static TarefaResponse from(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.isConcluida(),
                tarefa.getCriadaEm()
        );
    }
}
