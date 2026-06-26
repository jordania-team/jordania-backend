package com.jordania.api.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "providers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_providers_name_subject",
                columnNames = {"provider_name", "provider_subject"}
        )
)
public class Providers {

    @Id
    @Column(name = "provider_subject", nullable = false, length = 255)
    private String provider_subject;

    @Column(name = "provider_name", nullable = false, length = 20)
    private String provider_name;

    protected Providers() {
    }

    public Providers(String provider_subject, String provider_name) {
        this.provider_subject = provider_subject;
        this.provider_name = provider_name;
    }

    public String getProvider_subject() {
        return provider_subject;
    }

    public String getProvider_name() {
        return provider_name;
    }
}
