package com.example.OOP_FitConnect.repository;

import com.example.OOP_FitConnect.model.SessionBooking;
import com.example.OOP_FitConnect.model.TrainingSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TrainingSessionRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<TrainingSession> sessionMapper = (rs, rowNum) -> {
        TrainingSession s = new TrainingSession();
        s.setId(rs.getLong("id"));
        s.setInstructorId(rs.getInt("instructor_id"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setSessionType(rs.getString("session_type"));
        s.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        s.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        s.setCapacity(rs.getInt("capacity"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        try { s.setInstructorName(rs.getString("instructor_name")); } catch (Exception ignored) {}
        try { s.setCurrentBookings(rs.getInt("current_bookings")); } catch (Exception ignored) {}
        try { s.setBookedByCurrentUser(rs.getBoolean("is_booked")); } catch (Exception ignored) {}
        return s;
    };

    private final RowMapper<SessionBooking> bookingMapper = (rs, rowNum) -> {
        SessionBooking b = new SessionBooking();
        b.setId(rs.getLong("id"));
        b.setSessionId(rs.getLong("session_id"));
        b.setMemberId(rs.getInt("member_id"));
        b.setStatus(rs.getString("status"));
        b.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        
        try { b.setMemberName(rs.getString("member_name")); } catch (Exception ignored) {}
        return b;
    };

    // --- Training Sessions ---

    public void saveSession(TrainingSession session) {
        String sql = "INSERT INTO training_sessions (instructor_id, title, description, session_type, start_time, end_time, capacity, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, session.getInstructorId(), session.getTitle(), session.getDescription(), 
                            session.getSessionType(), Timestamp.valueOf(session.getStartTime()), Timestamp.valueOf(session.getEndTime()), 
                            session.getCapacity(), session.getStatus());
    }

    public void updateSession(TrainingSession session) {
        String sql = "UPDATE training_sessions SET title = ?, description = ?, session_type = ?, start_time = ?, end_time = ?, capacity = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, session.getTitle(), session.getDescription(), session.getSessionType(), 
                            Timestamp.valueOf(session.getStartTime()), Timestamp.valueOf(session.getEndTime()), session.getCapacity(), session.getStatus(), session.getId());
    }

    public void deleteSession(Long id) {
        jdbcTemplate.update("DELETE FROM session_bookings WHERE session_id = ?", id);
        jdbcTemplate.update("DELETE FROM training_sessions WHERE id = ?", id);
    }

    public TrainingSession findSessionById(Long id) {
        String sql = "SELECT ts.*, u.name as instructor_name, " +
                     "(SELECT COUNT(*) FROM session_bookings sb WHERE sb.session_id = ts.id AND sb.status = 'BOOKED') as current_bookings " +
                     "FROM training_sessions ts " +
                     "LEFT JOIN users u ON ts.instructor_id = u.id " +
                     "WHERE ts.id = ?";
        List<TrainingSession> results = jdbcTemplate.query(sql, sessionMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<TrainingSession> findSessionsByInstructorId(int instructorId) {
        String sql = "SELECT ts.*, u.name as instructor_name, " +
                     "(SELECT COUNT(*) FROM session_bookings sb WHERE sb.session_id = ts.id AND sb.status = 'BOOKED') as current_bookings " +
                     "FROM training_sessions ts " +
                     "LEFT JOIN users u ON ts.instructor_id = u.id " +
                     "WHERE ts.instructor_id = ? ORDER BY ts.start_time DESC";
        return jdbcTemplate.query(sql, sessionMapper, instructorId);
    }

    public List<TrainingSession> findAllUpcomingSessions(int memberId) {
        String sql = "SELECT ts.*, u.name as instructor_name, " +
                     "(SELECT COUNT(*) FROM session_bookings sb WHERE sb.session_id = ts.id AND sb.status = 'BOOKED') as current_bookings, " +
                     "CASE WHEN EXISTS (SELECT 1 FROM session_bookings sb2 WHERE sb2.session_id = ts.id AND sb2.member_id = ? AND sb2.status = 'BOOKED') THEN true ELSE false END as is_booked " +
                     "FROM training_sessions ts " +
                     "LEFT JOIN users u ON ts.instructor_id = u.id " +
                     "WHERE ts.start_time > CURRENT_TIMESTAMP AND ts.status = 'SCHEDULED' " +
                     "ORDER BY ts.start_time ASC";
        return jdbcTemplate.query(sql, sessionMapper, memberId);
    }

    public List<TrainingSession> findAllSessions() {
        String sql = "SELECT ts.*, u.name as instructor_name, " +
                     "(SELECT COUNT(*) FROM session_bookings sb WHERE sb.session_id = ts.id AND sb.status = 'BOOKED') as current_bookings " +
                     "FROM training_sessions ts " +
                     "LEFT JOIN users u ON ts.instructor_id = u.id " +
                     "ORDER BY ts.start_time DESC";
        return jdbcTemplate.query(sql, sessionMapper);
    }

    // --- Session Bookings ---

    public void bookSession(Long sessionId, int memberId) {
        String sql = "INSERT INTO session_bookings (session_id, member_id, status) VALUES (?, ?, 'BOOKED')";
        jdbcTemplate.update(sql, sessionId, memberId);
    }

    public void cancelBookingByMember(Long sessionId, int memberId) {
        String sql = "UPDATE session_bookings SET status = 'CANCELLED' WHERE session_id = ? AND member_id = ?";
        jdbcTemplate.update(sql, sessionId, memberId);
    }

    public List<SessionBooking> findBookingsBySessionId(Long sessionId) {
        String sql = "SELECT sb.*, u.name as member_name FROM session_bookings sb LEFT JOIN users u ON sb.member_id = u.id WHERE sb.session_id = ?";
        return jdbcTemplate.query(sql, bookingMapper, sessionId);
    }

    public List<TrainingSession> findBookedSessionsByMemberId(int memberId) {
        String sql = "SELECT ts.*, u.name as instructor_name, " +
                     "(SELECT COUNT(*) FROM session_bookings sb WHERE sb.session_id = ts.id AND sb.status = 'BOOKED') as current_bookings, " +
                     "true as is_booked " +
                     "FROM training_sessions ts " +
                     "JOIN session_bookings sb ON ts.id = sb.session_id " +
                     "LEFT JOIN users u ON ts.instructor_id = u.id " +
                     "WHERE sb.member_id = ? AND sb.status = 'BOOKED' " +
                     "ORDER BY ts.start_time ASC";
        return jdbcTemplate.query(sql, sessionMapper, memberId);
    }
}
