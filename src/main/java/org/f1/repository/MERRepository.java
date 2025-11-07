package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
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

    public MeetingEntityReference saveMeetingReference(MeetingEntityReference meetingEntityReference) {

        MeetingEntityReferenceRecord meetingEntityReferenceRecord = new MeetingEntityReferenceRecord();
        meetingEntityReferenceRecord.setDriverId(meetingEntityReference.getDriverId());
        meetingEntityReferenceRecord.setMeetingId(meetingEntityReference.getMeetingId());
        meetingEntityReferenceRecord.setTeamId(meetingEntityReference.getTeamId());

        MeetingEntityReferenceRecord returnedMeetingReferenceRecord = dslContext.insertInto(MEETING_ENTITY_REFERENCE)
                .set(meetingEntityReferenceRecord)
                .returning()
                .fetchOneInto(MeetingEntityReferenceRecord.class);

        return map(returnedMeetingReferenceRecord);
    }

    private MeetingEntityReference map(MeetingEntityReferenceRecord meetingEntityReferenceRecord) {

        return new MeetingEntityReference(meetingEntityReferenceRecord.getId(),
                meetingEntityReferenceRecord.getDriverId(),
                meetingEntityReferenceRecord.getTeamId(),
                meetingEntityReferenceRecord.getMeetingId());
    }
}
