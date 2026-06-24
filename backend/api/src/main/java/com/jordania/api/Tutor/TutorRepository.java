package com.jordania.api.Tutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
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
        /* 
        Connects to the database and executes insertion query
        Input: tutor object
        */

        String sqlQuery = """
            INSERT INTO Tutors (id, user_id, name, username, is_private, img_url, birthday, updated_at, reports_counter)
            VALUES (:id, :user_id, :name, :username, :is_private, :img_url, :birthday, :updated_at, :reports_counter)
        """;

        // conection with database (prepared statement is used to execute sql commands)
        Connection conn;
        PreparedStatement preparedStatement;
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pocdb", "pocuser", "pocpass");

            preparedStatement = conn.prepareStatement(sqlQuery);
            preparedStatement.execute();

        } catch (SQLException exception) {
            System.out.println(exception);
            throw new RuntimeException("Error saving tutor object to the database");
        }
    }

    // TODO: encontrar por id
    // public void searchByIdQuery(Tutor tutor){
    //     /* 
    //     Connects to the database and executes insertion query
    //     Input: tutor object
    //     */

    //     String sqlQuery = """
    //         INSERT INTO Tutors (id, user_id, name, username, is_private, img_url, birthday, updated_at, reports_counter)
    //         VALUES (:id, :user_id, :name, :username, :is_private, :img_url, :birthday, :updated_at, :reports_counter)
    //     """;

    //     // conection with database (prepared statement is used to execute sql commands)
    //     Connection conn;
    //     PreparedStatement preparedStatement;
    //     try {
    //         conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pocdb", "pocuser", "pocpass");

    //         preparedStatement = conn.prepareStatement(sqlQuery);
    //         preparedStatement.execute();

    //     } catch (SQLException exception) {
    //         System.out.println(exception);
    //         throw new RuntimeException("Error saving tutor object to the database");
    //     }
    // }


        
    //TODO: deletar por id


    
    
    
    // TODO: verificar se ja tem username
    

}
