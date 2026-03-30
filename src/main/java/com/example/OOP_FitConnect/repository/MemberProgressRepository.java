package com.example.OOP_FitConnect.repository;

import com.example.OOP_FitConnect.model.MemberProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class MemberProgressRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<MemberProgress> progressMapper = (rs, rowNum) -> {
        MemberProgress mp = new MemberProgress();
        mp.setId(rs.getLong("id"));
        mp.setMemberId(rs.getInt("member_id"));
        mp.setInstructorId(rs.getInt("instructor_id"));
        mp.setLogDate(rs.getDate("log_date").toLocalDate());
        
        Double weight = rs.getDouble("weight");
        mp.setWeight(rs.wasNull() ? null : weight);
        
        Double bodyFat = rs.getDouble("body_fat_percentage");
        mp.setBodyFatPercentage(rs.wasNull() ? null : bodyFat);
        
        Double muscleMass = rs.getDouble("muscle_mass");
        mp.setMuscleMass(rs.wasNull() ? null : muscleMass);
        
        mp.setNotes(rs.getString("notes"));
        mp.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        try { mp.setMemberName(rs.getString("member_name")); } catch (Exception ignored) {}
        try { mp.setInstructorName(rs.getString("instructor_name")); } catch (Exception ignored) {}

        return mp;
    };

    public void saveProgress(MemberProgress progress) {
        String sql = "INSERT INTO member_progress (member_id, instructor_id, log_date, weight, body_fat_percentage, muscle_mass, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, progress.getMemberId(), progress.getInstructorId(), Date.valueOf(progress.getLogDate()),
                            progress.getWeight(), progress.getBodyFatPercentage(), progress.getMuscleMass(), progress.getNotes());
    }

    public void deleteProgress(Long id) {
        jdbcTemplate.update("DELETE FROM member_progress WHERE id = ?", id);
    }

    public List<MemberProgress> findProgressByMemberId(int memberId) {
        String sql = "SELECT mp.*, m.name as member_name, i.name as instructor_name " +
                     "FROM member_progress mp " +
                     "LEFT JOIN users m ON mp.member_id = m.id " +
                     "LEFT JOIN users i ON mp.instructor_id = i.id " +
                     "WHERE mp.member_id = ? ORDER BY mp.log_date DESC";
        return jdbcTemplate.query(sql, progressMapper, memberId);
    }
}
