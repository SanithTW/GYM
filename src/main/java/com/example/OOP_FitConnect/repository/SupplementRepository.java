package com.example.OOP_FitConnect.repository;

import com.example.OOP_FitConnect.model.Supplement;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SupplementRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Supplement> supplementRowMapper = new RowMapper<Supplement>() {
        @Override
        public Supplement mapRow(ResultSet rs, int rowNum) throws SQLException {
            Supplement supplement = new Supplement();
            supplement.setId(rs.getLong("id"));
            supplement.setName(rs.getString("name"));
            supplement.setDescription(rs.getString("description"));
            supplement.setPrice(rs.getDouble("price"));
            supplement.setCategory(rs.getString("category"));
            supplement.setImagePath(rs.getString("image_path"));
            return supplement;
        }
    };

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS supplements (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "price DOUBLE NOT NULL, " +
                "category VARCHAR(100), " +
                "image_path VARCHAR(255))");

        // Add sample data if table is empty
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM supplements", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO supplements (name, description, price, category, image_path) VALUES (?, ?, ?, ?, ?)",
                    "Whey Protein", "High-quality protein for muscle recovery.", 49.99, "Protein",
                    "/uploads/supplements/whey.jpg");
            jdbcTemplate.update(
                    "INSERT INTO supplements (name, description, price, category, image_path) VALUES (?, ?, ?, ?, ?)",
                    "Creatine Monohydrate", "Enhances strength and power.", 29.99, "Performance",
                    "/uploads/supplements/creatine.jpg");
            jdbcTemplate.update(
                    "INSERT INTO supplements (name, description, price, category, image_path) VALUES (?, ?, ?, ?, ?)",
                    "BCAA Powder", "Helps reduce muscle soreness.", 34.99, "Recovery", "/uploads/supplements/bcaa.jpg");
        }
    }

    public List<Supplement> findAll() {
        return jdbcTemplate.query("SELECT * FROM supplements", supplementRowMapper);
    }

    public Supplement findById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM supplements WHERE id = ?", supplementRowMapper, id);
    }

    public Supplement save(Supplement supplement) {
        if (supplement.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO supplements (name, description, price, category, image_path) VALUES (?, ?, ?, ?, ?)",
                    supplement.getName(), supplement.getDescription(), supplement.getPrice(), supplement.getCategory(),
                    supplement.getImagePath());
        } else {
            jdbcTemplate.update(
                    "UPDATE supplements SET name = ?, description = ?, price = ?, category = ?, image_path = ? WHERE id = ?",
                    supplement.getName(), supplement.getDescription(), supplement.getPrice(), supplement.getCategory(),
                    supplement.getImagePath(), supplement.getId());
        }
        return supplement;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM supplements WHERE id = ?", id);
    }
}
