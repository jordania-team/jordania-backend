package com.rodrigoborges.pocapi.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
