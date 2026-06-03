package com.rodrigoborges.pocapi.tarefa;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaRepository repository;

    public TarefaController(TarefaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TarefaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(TarefaResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TarefaResponse buscarPorId(@PathVariable Long id) {
        Tarefa tarefa = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        return TarefaResponse.from(tarefa);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@RequestBody @Valid CriarTarefaRequest request) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());

        Tarefa salva = repository.save(tarefa);

        return TarefaResponse.from(salva);
    }

    @PatchMapping("/{id}/concluir")
    public TarefaResponse concluir(@PathVariable Long id) {
        Tarefa tarefa = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        tarefa.setConcluida(true);

        return TarefaResponse.from(repository.save(tarefa));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}