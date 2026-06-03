package com.rodrigoborges.pocapi.tarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarTarefaRequest(
        @NotBlank
        @Size(max = 120)
        String titulo,

        String descricao
) {
}