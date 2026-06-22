package com.jordania.api.Following;

import java.sql.Date;
import java.util.UUID;

import com.jordania.api.Tutor.Tutor;

import jakarta.persistence.Entity;
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
@Table(name = "Guardianships")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Following {
    @Id
    @GeneratedValue
    private UUID id;

    private FollowStatus request_status;
    private Date requested_at;
    private Date updated_at;

    @OneToOne
    @JoinColumn(name = "follower_id")
    private Tutor follower_id;

    @OneToOne
    @JoinColumn(name = "follows_id")
    private Tutor follows_id;
}
