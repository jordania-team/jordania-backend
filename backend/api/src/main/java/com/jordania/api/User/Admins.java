package com.jordania.api.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "admins")
public class Admins {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    protected Admins() {
    }

    public Admins(Users user) {
        this.id = UUID.randomUUID();
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }
}
