package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.openf1.Session;
import org.f1.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    SessionRepository sessionRepository;
    OpenF1Dao openF1Dao;

    public SessionService(OpenF1Dao openF1Dao, SessionRepository sessionRepository) {
        this.openF1Dao = openF1Dao;
        this.sessionRepository = sessionRepository;
    }

    public List<Session> populateSessions() {
        List<Session> sessions = openF1Dao.getAllSessions();

        sessions.forEach(session -> sessionRepository.saveSession(session));

        return sessions;
    }
}
