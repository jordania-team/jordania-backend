package com.jordania.api.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminsRepository extends JpaRepository<Admins, UUID> {
}
