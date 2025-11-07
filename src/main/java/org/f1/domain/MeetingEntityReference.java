package org.f1.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MeetingEntityReference {

    Integer id;
    String driverId;
    int teamId;
    int meetingId;


    public MeetingEntityReference(String driverId, int teamId, int meetingId) {
        this.driverId = driverId;
        this.teamId = teamId;
        this.meetingId = meetingId;
        id = null;
    }
}
