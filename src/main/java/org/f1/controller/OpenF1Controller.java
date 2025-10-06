package org.f1.controller;

import org.f1.dao.SessionsDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openf1")
public class OpenF1Controller {

    SessionsDao sessionsDao;

    public OpenF1Controller(SessionsDao sessionsDao) {
        this.sessionsDao = sessionsDao;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> populateSessions() {
//        sessionsDao.getAllSessions();

        return new ResponseEntity<>(sessionsDao.getAllSessions(), HttpStatus.OK);
    }
}
