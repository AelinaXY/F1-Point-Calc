package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
import org.f1.generated.tables.records.MeetingEntityReferenceRecord;
import org.f1.utils.DatabaseUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static org.f1.generated.tables.MeetingEntityReference.MEETING_ENTITY_REFERENCE;
import static org.f1.utils.DatabaseUtils.equalOrIsNull;


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
                .onConflict()
                .doNothing()
                .returning()
                .fetchOneInto(MeetingEntityReferenceRecord.class);


        if (returnedMeetingReferenceRecord == null) {
            return getMeetingEntityReference(meetingEntityReference);
        }

        return map(returnedMeetingReferenceRecord);
    }

    private MeetingEntityReference getMeetingEntityReference(MeetingEntityReference meetingEntityReference) {
        MeetingEntityReferenceRecord meetingEntityReferenceRecord = dslContext.selectFrom(MEETING_ENTITY_REFERENCE)
                .where(equalOrIsNull(MEETING_ENTITY_REFERENCE.DRIVER_ID,  meetingEntityReference.getDriverId())
                        .and(MEETING_ENTITY_REFERENCE.MEETING_ID.eq(meetingEntityReference.getMeetingId()))
                        .and(MEETING_ENTITY_REFERENCE.TEAM_ID.eq(meetingEntityReference.getTeamId())))
                .fetchOneInto(MeetingEntityReferenceRecord.class);

        return map(Objects.requireNonNull(meetingEntityReferenceRecord));
    }

    private MeetingEntityReference map(MeetingEntityReferenceRecord meetingEntityReferenceRecord) {

        return new MeetingEntityReference(meetingEntityReferenceRecord.getId(),
                meetingEntityReferenceRecord.getDriverId(),
                meetingEntityReferenceRecord.getTeamId(),
                meetingEntityReferenceRecord.getMeetingId());
    }
}
