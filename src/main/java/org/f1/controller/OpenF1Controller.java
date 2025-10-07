package org.f1.controller;

import org.f1.dao.OpenF1Dao;
import org.f1.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openf1")
public class OpenF1Controller {

    OpenF1Dao openF1Dao;
    MeetingService meetingService;

    public OpenF1Controller(OpenF1Dao openF1Dao, MeetingService meetingService) {
        this.openF1Dao = openF1Dao;
        this.meetingService = meetingService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> populateSessions() {
//        sessionsDao.getAllSessions();

        return new ResponseEntity<>(openF1Dao.getAllSessions(), HttpStatus.OK);
    }

    @GetMapping("/meetings")
    public ResponseEntity<?> populateMeetings() {

        return new ResponseEntity<>(meetingService.populateMeetings(), HttpStatus.OK);
    }
}
