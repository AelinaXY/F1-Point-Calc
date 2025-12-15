package org.f1.service;

import org.springframework.stereotype.Service;

@Service
public class OpenF1Service {

    private final DriverService driverService;
    private final MeetingService meetingService;
    private final SessionService sessionService;
    private final SessionResultService sessionResultService;
    private final RegressionService regressionService;

    public OpenF1Service(DriverService driverService, MeetingService meetingService, SessionService sessionService, SessionResultService sessionResultService, RegressionService regressionService) {
        this.driverService = driverService;
        this.meetingService = meetingService;
        this.sessionService = sessionService;
        this.sessionResultService = sessionResultService;
        this.regressionService = regressionService;
    }


    public void populate() {
        meetingService.populateMeetings();
        sessionService.populateSessions();
        driverService.populateDrivers();
        sessionResultService.populateSessionResults();
        regressionService.populateNSADRegressionData();

    }
}
