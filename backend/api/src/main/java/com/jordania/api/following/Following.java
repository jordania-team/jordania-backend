package com.jordania.api.following;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jordania.api.tutor.Tutor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

enum FollowStatus {
    PENDING,
    BLOCKED,
    ACCEPTED,
    CANCELLED,
    REJECTED
}

@Entity
@Table(name = "Followings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Following {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private FollowStatus request_status;

    private LocalDateTime requested_at;
    private LocalDateTime updated_at;

    @OneToOne
    @JoinColumn(name = "follower_id")
    private Tutor follower_id;

    @OneToOne
    @JoinColumn(name = "followed_id")
    private Tutor followed_id;
}
