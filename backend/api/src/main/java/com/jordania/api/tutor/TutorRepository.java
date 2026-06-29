package com.jordania.api.tutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TutorRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TutorRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final RowMapper<Tutor> tutorRowMapper = (rs, rowNum) -> {
        Tutor tutor = new Tutor();

        tutor.setId(UUID.fromString(rs.getString("id")));

        String userIdString = rs.getString("user_id");
        tutor.setUserId(userIdString != null ? UUID.fromString(userIdString) : null);

        tutor.setName(rs.getString("name"));
        tutor.setUsername(rs.getString("username"));
        tutor.setIsPrivate(rs.getBoolean("is_private"));
        tutor.setImg(rs.getString("img_url"));

        if (rs.getTimestamp("birthday") != null) {
            tutor.setBirthday(rs.getTimestamp("birthday").toLocalDateTime());
        }

        if (rs.getTimestamp("created_at") != null) {
            tutor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        if (rs.getTimestamp("updated_at") != null) {
            tutor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        tutor.setReportsCounter(rs.getInt("reports_counter"));

        return tutor;
    };

    public void insertQuery(Tutor tutor) {
        String sqlQuery = """
            INSERT INTO tutors (
                id,
                user_id,
                name,
                username,
                is_private,
                img_url,
                birthday,
                created_at,
                updated_at,
                reports_counter
            )
            VALUES (
                :id,
                :user_id,
                :name,
                :username,
                :is_private,
                :img_url,
                :birthday,
                :created_at,
                :updated_at,
                :reports_counter
            )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", tutor.getId())
                .addValue("user_id", tutor.getUserId())
                .addValue("name", tutor.getName())
                .addValue("username", tutor.getUsername())
                .addValue("is_private", tutor.getIsPrivate())
                .addValue("img_url", tutor.getImg())
                .addValue("birthday", tutor.getBirthday())
                .addValue("created_at", tutor.getCreatedAt())
                .addValue("updated_at", tutor.getUpdatedAt())
                .addValue("reports_counter", tutor.getReportsCounter());

        jdbcTemplate.update(sqlQuery, params);
    }

    public Optional<Tutor> findById(UUID id) {
        String sqlQuery = """
            SELECT *
            FROM tutors
            WHERE id = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<Tutor> result = jdbcTemplate.query(sqlQuery, params, tutorRowMapper);

        return result.stream().findFirst();
    }

    public Optional<Tutor> searchByIdQuery(UUID id) {
        return findById(id);
    }

    public void deleteTutorByIdQuery(UUID id) {
        String sqlQuery = """
            DELETE FROM tutors
            WHERE id = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        jdbcTemplate.update(sqlQuery, params);
    }
}