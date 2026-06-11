package com.jordania.api.User;

import com.jordania.api.Auth.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
