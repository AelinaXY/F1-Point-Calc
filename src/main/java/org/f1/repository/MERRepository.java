package org.f1.repository;

import org.f1.domain.DriverMeetingReference;
import org.f1.generated.tables.records.MeetingEntityReferenceRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.f1.generated.tables.MeetingEntityReference.MEETING_ENTITY_REFERENCE;


@Repository
public class MERRepository {

    DSLContext dslContext;

    public MERRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Integer saveMeetingReference(DriverMeetingReference driverMeetingReference) {

        MeetingEntityReferenceRecord meetingEntityReferenceRecord = new MeetingEntityReferenceRecord();
        meetingEntityReferenceRecord.setDriverId(driverMeetingReference.driverId());
        meetingEntityReferenceRecord.setMeetingId(driverMeetingReference.meetingId());
        meetingEntityReferenceRecord.setTeamId(driverMeetingReference.teamId());

        MeetingEntityReferenceRecord returnedMeetingReferenceRecord = dslContext.insertInto(MEETING_ENTITY_REFERENCE)
                .set(meetingEntityReferenceRecord)
                .returning()
                .fetchOneInto(MeetingEntityReferenceRecord.class);

        return returnedMeetingReferenceRecord.getId();
    }
}
