package com.example.OOP_FitConnect.service;

import com.example.OOP_FitConnect.model.SessionBooking;
import com.example.OOP_FitConnect.model.TrainingSession;
import com.example.OOP_FitConnect.repository.TrainingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingSessionService {

    @Autowired
    private TrainingSessionRepository repository;

    public void createSession(TrainingSession session) {
        repository.saveSession(session);
    }

    public void updateSession(TrainingSession session) {
        repository.updateSession(session);
    }

    public void deleteSession(Long id) {
        repository.deleteSession(id);
    }

    public TrainingSession getSessionById(Long id) {
        return repository.findSessionById(id);
    }

    public List<TrainingSession> getSessionsByInstructor(int instructorId) {
        return repository.findSessionsByInstructorId(instructorId);
    }

    public List<TrainingSession> getAllUpcomingSessions(int memberId) {
        return repository.findAllUpcomingSessions(memberId);
    }

    public List<TrainingSession> getAllSessions() {
        return repository.findAllSessions();
    }

    // --- Bookings ---

    public boolean bookSession(Long sessionId, int memberId) {
        TrainingSession session = getSessionById(sessionId);
        if (session != null && session.getCurrentBookings() < session.getCapacity()) {
            repository.bookSession(sessionId, memberId);
            return true;
        }
        return false;
    }

    public void cancelBooking(Long sessionId, int memberId) {
        repository.cancelBookingByMember(sessionId, memberId);
    }

    public List<SessionBooking> getBookingsForSession(Long sessionId) {
        return repository.findBookingsBySessionId(sessionId);
    }

    public List<TrainingSession> getBookedSessionsForMember(int memberId) {
        return repository.findBookedSessionsByMemberId(memberId);
    }
}
