package com.example.OOP_FitConnect.repository;

import com.example.OOP_FitConnect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class DBController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setVerificationCode(rs.getInt("verificationCode"));
            user.setBranch(rs.getString("branch"));
            return user;
        }
    };

    public User saveUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (name, email, password, verificationCode, branch) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getVerificationCode());
            ps.setString(5, user.getBranch());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    public User updateUser(User user) {
        jdbcTemplate.update(
                "UPDATE users SET name = ?, email = ?, password = ?, verificationCode = ?, branch = ? WHERE id = ?",
                user.getName(), user.getEmail(), user.getPassword(),
                user.getVerificationCode(), user.getBranch(), user.getId()
        );
        return user;
    }

    public User getUserById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getUserByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", userRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getUserByVerificationCode(int code) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE verificationCode = ? AND verificationCode != 0",
                    userRowMapper, code
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteUser(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }
}