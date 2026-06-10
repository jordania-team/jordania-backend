package com.rodrigoborges.pocapi.tarefa;

import com.rodrigoborges.pocapi.auth.Usuario;
import com.rodrigoborges.pocapi.auth.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public TarefaController(TarefaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<TarefaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return repository.findAllByUsuarioIdOrderByCriadaEmDesc(userId(jwt))
                .stream()
                .map(TarefaResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TarefaResponse buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Tarefa tarefa = ownedTask(id, jwt);
        return TarefaResponse.from(tarefa);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(
            @RequestBody @Valid CriarTarefaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Usuario usuario = usuarioRepository.findById(userId(jwt))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());
        tarefa.setUsuario(usuario);

        Tarefa salva = repository.save(tarefa);

        return TarefaResponse.from(salva);
    }

    @PatchMapping("/{id}/concluir")
    public TarefaResponse concluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Tarefa tarefa = ownedTask(id, jwt);
        tarefa.setConcluida(true);

        return TarefaResponse.from(repository.save(tarefa));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        repository.delete(ownedTask(id, jwt));
    }

    private Tarefa ownedTask(Long id, Jwt jwt) {
        return repository.findByIdAndUsuarioId(id, userId(jwt))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tarefa não encontrada"
                ));
    }

    private UUID userId(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
