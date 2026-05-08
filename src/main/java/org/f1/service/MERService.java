package org.f1.service;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.MeetingEntityReference;
import org.f1.domain.TeamLookup;
import org.f1.repository.MERRepository;
import org.f1.repository.TeamRepository;
import org.springframework.stereotype.Service;

@Service
public class MERService {

    private final MERRepository merRepository;
    private final DriverService driverService;
    private final MeetingService meetingService;
    private final TeamRepository teamRepository;

    public MERService(MERRepository merRepository, DriverService driverService, MeetingService meetingService, TeamRepository teamRepository) {
        this.merRepository = merRepository;
        this.driverService = driverService;
        this.meetingService = meetingService;
        this.teamRepository = teamRepository;
    }

    public MeetingEntityReference getOrCreateMeetingEntityReference(int year, Meeting meeting, FullPointEntity entity) {
        MeetingEntityReference currentMeetingEntityReference = merRepository.findMeetingEntityReference(year, meeting, entity);

        if (currentMeetingEntityReference != null) {
            return currentMeetingEntityReference;
        }
        Integer meetingId = meetingService.getMeeting(year, meeting.getFullNames());

        if(entity.isDriver())
        {
            MeetingEntityReference meetingEntityReference = driverService.getDriverMRFromYearAndMeetingName(entity.getName(), year, meetingId);

            return merRepository.saveMeetingReference(meetingEntityReference);
        }
        else {
            Integer teamId = teamRepository.getTeam(TeamLookup.csvToPreferred(entity.getName()));

            return merRepository.saveMeetingReference(new MeetingEntityReference(null, null, teamId, meetingId));
        }



    }
}
