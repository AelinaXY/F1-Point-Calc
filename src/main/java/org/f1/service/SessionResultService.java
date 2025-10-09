package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.openf1.SessionResult;
import org.f1.repository.SessionResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionResultService {

    SessionResultRepository sessionResultRepository;
    OpenF1Dao openF1Dao;

    public SessionResultService(OpenF1Dao openF1Dao, SessionResultRepository sessionResultRepository) {
        this.openF1Dao = openF1Dao;
        this.sessionResultRepository = sessionResultRepository;
    }

    public List<SessionResult> populateSessionResults() {
        List<SessionResult> sessionResults = openF1Dao.getAllSessionResults();

        sessionResults.forEach(session -> sessionResultRepository.saveSessionResult(session));

        return sessionResults;
    }
}
