package com.jordania.api.post;

import com.jordania.api.tutor.Tutor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    private UUID id;

    //define relacionamento entre entidadaes
    //cada post possui 1 tutor
    @ManyToOne(fetch = FetchType.LAZY) //LAZY ou EAGER -> lazy evita carregar dados desnecessarios
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "counter_1", nullable = false)
    @Builder.Default
    private Integer counter1 = 0;

    @Column(name = "counter_2", nullable = false)
    @Builder.Default
    private Integer counter2 = 0;

    @Column(name = "counter_3", nullable = false)
    @Builder.Default
    private Integer counter3 = 0;

    @Column(name = "counter_4", nullable = false)
    @Builder.Default
    private Integer counter4 = 0;

    @Column(name = "counter_5", nullable = false)
    @Builder.Default
    private Integer counter5 = 0;

    @Column(name = "reports_counter", nullable = false)
    @Builder.Default
    private Integer reportsCounter = 0;

    //Entity Lifecycle
    @PrePersist //preenche automaticamente o timestamp
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        postedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public Integer getCounter1() {
        return counter1;
    }

    public Integer getCounter2() {
        return counter2;
    }

    public Integer getCounter3() {
        return counter3;
    }

    public Integer getCounter4() {
        return counter4;
    }

    public Integer getCounter5() {
        return counter5;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCounter1(Integer counter1) {
        this.counter1 = counter1;
    }

    public void setCounter2(Integer counter2) {
        this.counter2 = counter2;
    }

    public void setCounter3(Integer counter3) {
        this.counter3 = counter3;
    }

    public void setCounter4(Integer counter4) {
        this.counter4 = counter4;
    }

    public void setCounter5(Integer counter5) {
        this.counter5 = counter5;
    }
   }