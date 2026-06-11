package com.jordania.api.User;

import com.jordania.api.Auth.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(length = 120)
    private String nome;

    @Column(length = 255)
    private String email;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Usuario() {
    }

    public Usuario(AuthProvider provider, String providerSubject, String nome, String email) {
        this.id = UUID.randomUUID();
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.nome = nome;
        this.email = email;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public void updateProfile(String nome, String email) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome.trim();
        }
        if (email != null && !email.isBlank()) {
            this.email = email.trim();
        }
        this.atualizadoEm = LocalDateTime.now();
    }
}
