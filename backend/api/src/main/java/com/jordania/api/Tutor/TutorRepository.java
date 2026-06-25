package com.jordania.api.Tutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TutorRepository {
    /*
    Layer responsible for queries in the database 
`   Receives a Java object, transforms in SQL with JdbcTemplate and sends to DB,
    or gets the database result and transforms into java object
     */

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TutorRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public final RowMapper<Tutor> tutorRowMapper = (rs, rowNum) -> {
        /*
        Map each row of a ResultSet to a java object
        rs: ResultSet
        rowNum: current row number
        */
        Tutor tutor = new Tutor();
        tutor.setId(UUID.fromString(rs.getString("id")));

        String userIdString = rs.getString("user_id");
        tutor.setUserId(userIdString != null ? UUID.fromString(userIdString) : null);

        // colocar ainda get user id
        tutor.setName(rs.getString("name"));
        tutor.setUsername(rs.getString("username"));
        tutor.setIsPrivate(rs.getBoolean("is_private"));
        tutor.setImg(rs.getString("img_url"));

        // if (rs.getTimestamp("birthday") != null) {
        //     tutor.setBirthday(rs.getTimestamp("birthday").toLocalDateTime());
        // }
        // if (rs.getTimestamp("updated_at") != null) {
        //     tutor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        // }

        tutor.setReportsCounter(rs.getInt("reports_counter"));
        return tutor;
    };
                
    public void insertQuery(Tutor tutor){
        String sqlQuery = """
            INSERT INTO Tutors (id, user_id, name, username, is_private, img_url, birthday, updated_at, reports_counter)
            VALUES (:id, :user_id, :name, :username, :is_private, :img_url, :birthday, :updated_at, :reports_counter)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", tutor.getId())
            .addValue("user_id", tutor.getUserId())
            .addValue("name", tutor.getName())
            .addValue("username", tutor.getUsername())
            .addValue("is_private", tutor.getIsPrivate())
            .addValue("img_url", tutor.getImg())
            .addValue("birthday", tutor.getBirthday())
            .addValue("updated_at", tutor.getUpdatedAt())
            .addValue("reports_counter", tutor.getReportsCounter());

        jdbcTemplate.update(sqlQuery, params);
    }

    public Optional<Tutor> searchByIdQuery(UUID id){
        /* 
        Connects to the database and executes search by id query
        Input: tutor object
        */

        String sqlQuery = "SELECT * FROM Tutors where id= :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id);

        List<Tutor> result = jdbcTemplate.query(sqlQuery, params, tutorRowMapper);
        
        return result.stream().findFirst();
    }

    public void deleteTutorByIdQuery(UUID id){
        /* 
        Connects to the database and executes deletion by id query
        Input: tutor object
        */

        String sqlQuery = "DELETE FROM Tutors where id= :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id);

        jdbcTemplate.update(sqlQuery, params);
    }

    // TODO: verificar se ja tem username
    

}
