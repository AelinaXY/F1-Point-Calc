package org.f1.repository;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.MeetingEntityReference;
import org.f1.domain.TeamLookup;
import org.f1.generated.Tables;
import org.f1.generated.tables.records.MeetingEntityReferenceRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.TableOnConditionStep;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static org.f1.generated.Tables.*;
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
    public MeetingEntityReference findMeetingEntityReference(int year, Meeting meeting, FullPointEntity entity) {
        TableOnConditionStep<Record> baseStep = MEETING_ENTITY_REFERENCE.leftJoin(TEAM).on(MEETING_ENTITY_REFERENCE.TEAM_ID.eq(TEAM.ID));
        String name = entity.getName();
        if(entity.isDriver())
        {
            baseStep.leftJoin(DRIVER).on(MEETING_ENTITY_REFERENCE.DRIVER_ID.eq(DRIVER.ID));
        }
        else {
            name = TeamLookup.csvToPreferred(name);
        }

        List<MeetingEntityReferenceRecord> meetingEntityReferenceRecord = dslContext.
                select(MEETING_ENTITY_REFERENCE.ID, MEETING_ENTITY_REFERENCE.DRIVER_ID, MEETING_ENTITY_REFERENCE.TEAM_ID, MEETING_ENTITY_REFERENCE.MEETING_ID)
                .from(MEETING_ENTITY_REFERENCE.leftJoin(DRIVER)
                        .on(MEETING_ENTITY_REFERENCE.DRIVER_ID.eq(DRIVER.ID))
                        .leftJoin(TEAM)
                        .on(MEETING_ENTITY_REFERENCE.TEAM_ID.eq(TEAM.ID))
                        .leftJoin(MEETING)
                        .on(MEETING_ENTITY_REFERENCE.MEETING_ID.eq(MEETING.ID)))
                .where(MEETING.NAME.in(meeting.getFullNames())
                        .and(MEETING.YEAR.eq(year))
                        .and(DRIVER.FULL_NAME.equalIgnoreCase(name)
                                .or(TEAM.TEAM_NAME.eq(name).
                                        and(MEETING_ENTITY_REFERENCE.DRIVER_ID.isNull()))))
                .fetchInto(MeetingEntityReferenceRecord.class);

        if(meetingEntityReferenceRecord.size() > 1) {
            throw new RuntimeException("More than one MER found for %s in %d at %s".formatted(entity.getName(), year, meeting.getShortName()));
        }

        if(meetingEntityReferenceRecord == null || meetingEntityReferenceRecord.isEmpty()) {
            return null;
        }

        return map(Objects.requireNonNull(meetingEntityReferenceRecord.getFirst()));
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
