package com.jordania.api.tutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;
import com.jordania.api.User.*;

@Entity
@Table(name = "tutors")
public class Tutor {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username;

    @Column(name = "is_private", nullable = false)
    private boolean is_private;

    @Column(name = "img_url")
    private String img_url;

    @Column(nullable = false)
    private LocalDateTime birthday;

    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Column(nullable = false)
    private int reports_counter;

    protected Tutor() {
    }

    public Tutor(
            Users user,
            String name,
            String username,
            boolean is_private,
            String img_url,
            LocalDateTime birthday
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.reports_counter = 0;
        update(name, username, is_private, img_url, birthday);
    }

    public UUID getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public boolean isIs_private() {
        return is_private;
    }

    public String getImg_url() {
        return img_url;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public int getReports_counter() {
        return reports_counter;
    }

    public void update(
            String name,
            String username,
            boolean is_private,
            String img_url,
            LocalDateTime birthday
    ) {
        this.name = name;
        this.username = username;
        this.is_private = is_private;
        this.img_url = normalizedOptional(img_url);
        this.birthday = birthday;
        this.updated_at = LocalDateTime.now();
    }

    private String normalizedOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
