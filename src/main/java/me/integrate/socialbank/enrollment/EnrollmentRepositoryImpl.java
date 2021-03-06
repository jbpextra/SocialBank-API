package me.integrate.socialbank.enrollment;

import me.integrate.socialbank.enrollment.exceptions.EnrollmentAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository{

    private static String ENROLLMENT_TABLE ="user_event_enrollment";
    private static String USER_EMAIL = "user_email";
    private static String EVENT_ID = "event_id";

    private final SimpleJdbcInsert simpleJdbcInsert;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EnrollmentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName(ENROLLMENT_TABLE)
                .usingColumns(EVENT_ID, USER_EMAIL);
    }

    @Override
    public Enrollment saveEnrollment(int id, String email) {
        try {
            jdbcTemplate.update("INSERT INTO " + ENROLLMENT_TABLE + " VALUES (?, ?)", email, id);
        } catch (DuplicateKeyException ex) {
            throw new EnrollmentAlreadyExistsException();
        }

        return new Enrollment(id, email);
    }

    @Override
    public List<String> getEnrollmentsOfEvent(int id) {
        return jdbcTemplate.queryForList("SELECT " + USER_EMAIL + " FROM " + ENROLLMENT_TABLE + " WHERE " + EVENT_ID + "= ?",
                new Object[]{id}, String.class);
    }

    @Override
    public int getNumberOfUsersEnrolledInEvent(int id) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + ENROLLMENT_TABLE + " WHERE " + EVENT_ID + "= ?",
                new Object[]{id}, Integer.class);
    }

    @Override
    public List<Integer> getEnrollmentsOfUser(String email) {
        return jdbcTemplate.queryForList("SELECT " + EVENT_ID + " FROM " + ENROLLMENT_TABLE + " WHERE " + USER_EMAIL + "= ?",
                new Object[]{email},Integer.class);
    }

    @Override
    public void deleteEnrollment(int id, String email) {
        jdbcTemplate.update("DELETE FROM " + ENROLLMENT_TABLE + " WHERE " + USER_EMAIL + "=? AND "+ EVENT_ID + "=?", email, id);
    }

}
