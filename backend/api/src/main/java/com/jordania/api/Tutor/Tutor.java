package com.jordania.api.Tutor;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.UUID;


public class Tutor {
    private UUID id;
    private UUID user_id;
    private String name;
    private String username;
    private Boolean is_private;
    private String img_url;
    private Date birthday;
    private Integer reports_counter;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public Tutor() {
    }

    public Tutor(UUID id, UUID user_id, String name, String username, 
        Boolean is_private, String img_url, Date birthday, Integer reports_counter, 
        LocalDateTime created_at, LocalDateTime updated_at) {
            this.id = id;
            this.user_id = user_id;
            this.name = name;
            this.username = username;
            this.is_private = is_private;
            this.img_url = img_url;
            this.birthday = birthday;
            this.reports_counter = reports_counter;
            this.created_at = created_at;
            this.updated_at = updated_at;
    }

    public UUID getId() {
        return this.id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return this.user_id;
    }
    public void setUserId(UUID user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsPrivate() {
        return this.is_private;
    }
    public void setIsPrivate(Boolean is_private) {
        this.is_private = is_private;
    }

    public String getImg() {
        return this.img_url;
    }
    public void setImg(String img_url) {
        this.img_url = img_url;
    }

    public Date getBirthday() {
        return this.birthday;
    }
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getReportsCounter() {
        return this.reports_counter;
    }
    public void setReportsCounter(Integer reports_counter) {
        this.reports_counter = reports_counter;
    }

    public LocalDateTime getCreatedAt() {
        return this.created_at;
    }
    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updated_at;
    }
    public void setUpdatedAt(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
