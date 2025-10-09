package org.f1.controller;

import org.f1.dao.OpenF1Dao;
import org.f1.service.MeetingService;
import org.f1.service.SessionResultService;
import org.f1.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openf1")
public class OpenF1Controller {

    private final SessionService sessionService;
    private final SessionResultService sessionResultService;
    OpenF1Dao openF1Dao;
    MeetingService meetingService;

    public OpenF1Controller(OpenF1Dao openF1Dao, MeetingService meetingService, SessionService sessionService, SessionResultService sessionResultService) {
        this.openF1Dao = openF1Dao;
        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultService = sessionResultService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> populateSessions() {

        return new ResponseEntity<>(sessionService.populateSessions(), HttpStatus.OK);
    }

    @GetMapping("/meetings")
    public ResponseEntity<?> populateMeetings() {

        return new ResponseEntity<>(meetingService.populateMeetings(), HttpStatus.OK);
    }

    @GetMapping("/sessionResults")
    public ResponseEntity<?> populateSessionResults() {
        return new ResponseEntity<>(sessionResultService.populateSessions(), HttpStatus.OK);
    }
}
