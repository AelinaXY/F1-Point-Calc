package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
import org.f1.domain.SessionResultsSummary;
import org.f1.domain.openf1.SessionResult;
import org.f1.generated.tables.records.SessionResultRecord;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.f1.generated.Tables.*;
import static org.f1.generated.tables.SessionResult.SESSION_RESULT;
import static org.jooq.impl.DSL.*;


@Repository
public class SessionResultRepository {

    DSLContext dslContext;

    public SessionResultRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public SessionResultRecord saveSessionResult(SessionResult sessionResult) {

        String previousId = dslContext.select(SESSION_RESULT.ID).from(SESSION_RESULT)
                .where(SESSION_RESULT.SESSION_ID
                        .eq(sessionResult.getSessionId())
                        .and(SESSION_RESULT.DRIVER_ID
                                .eq(sessionResult.getDriverId())))
                .fetchOneInto(String.class);

        SessionResultRecord sessionResultRecord = buildSessionResult(sessionResult, previousId);

        return dslContext.insertInto(SESSION_RESULT)
                .set(sessionResultRecord)
                .onConflict(SESSION_RESULT.ID)
                .doUpdate()
                .set(sessionResultRecord)
                .returning()
                .fetchOne();
    }

    public List<SessionResult> getSessionResults(MeetingEntityReference meetingEntityReference, String sessionName) {
        Condition whereClause = SESSION.MEETING_ID.eq(meetingEntityReference.getMeetingId());
        whereClause = whereClause.and(SESSION.SESSION_NAME.eq(sessionName));
        whereClause = whereClause.and(DRIVER.TEAM_ID.eq(meetingEntityReference.getTeamId()));
        if (meetingEntityReference.getDriverId() != null) {
            whereClause = whereClause.and(SESSION_RESULT.DRIVER_ID.eq(meetingEntityReference.getDriverId()));
        }


        return dslContext.select(SESSION_RESULT.fields())
                .select(DRIVER.DRIVER_NUMBER.as("driver_number"))
                .from(SESSION_RESULT
                        .join(SESSION).on(SESSION_RESULT.SESSION_ID.eq(SESSION.ID))
                        .join(DRIVER).on(SESSION_RESULT.DRIVER_ID.eq(DRIVER.ID)))
                .where(whereClause)
                .fetchInto(SessionResult.class);
    }

    public List<SessionResultsSummary> findMappedToQualiResultsForDriverOrTeam(MeetingEntityReference meetingEntityReference, List<String> valueSessionNames, List<String> keySessionNames) {
        Table<Record> valueTable = getRecordTable(meetingEntityReference, valueSessionNames, "valueTable");
        Table<Record> keyTable = getRecordTable(meetingEntityReference, keySessionNames, "keyTable");

        Field<Integer> keyMeetingId = keyTable.field("meetingId", MEETING.ID.getType());
        Field<Integer> valueMeetingId = valueTable.field("meetingId", MEETING.ID.getType());

        return dslContext.select(
                        multiset(
                                selectDistinct(
                                        sessionResultsFieldsFrom(keyTable)
                                )
                                        .from(keyTable)
                                        .where(keyMeetingId.eq(MEETING.ID)))
                                .convertFrom(r -> r.into(SessionResult.class))
                                .as("qualiSessionResults"),
                        multiset(
                                selectDistinct(
                                        sessionResultsFieldsFrom(valueTable)
                                )
                                        .from(valueTable)
                                        .where(valueMeetingId.eq(MEETING.ID))
                        )
                                .convertFrom(r -> r.into(SessionResult.class))
                                .as("practiseSessionResults"))
                .from(MEETING)
                .where(MEETING.DATE_START.lessOrEqual(dslContext.select(MEETING.DATE_START).from(MEETING).where(MEETING.ID.eq(meetingEntityReference.getMeetingId()))))
                .and(hasDataFor(valueTable, valueMeetingId))
                .and(hasDataFor(keyTable, keyMeetingId))
                .orderBy(MEETING.DATE_START.desc())
                .fetchInto(SessionResultsSummary.class);
    }

    private Condition hasDataFor(Table<Record> keyTable, Field<Integer> keyMeetingId) {
        return exists(
                selectOne()
                        .from(keyTable)
                        .where(keyMeetingId.eq(MEETING.ID))
        );
    }

    private Table<Record> getRecordTable(MeetingEntityReference meetingEntityReference, List<String> sessionNames, String tableName) {
        Condition whereClause = SESSION.SESSION_NAME.in(sessionNames);
        whereClause = whereClause.and(DRIVER.TEAM_ID.eq(meetingEntityReference.getTeamId()));
        if (meetingEntityReference.getDriverId() != null) {
            whereClause = whereClause.and(DRIVER.FULL_NAME.eq(dslContext.select(DRIVER.FULL_NAME).from(DRIVER).where(DRIVER.ID.eq(meetingEntityReference.getDriverId())).fetchOneInto(String.class)));
        }

        return dslContext.select(SESSION_RESULT.fields())
                .select(SESSION.MEETING_ID.as("meetingId"))
                .select(DRIVER.DRIVER_NUMBER.as("driver_number"))
                .from(SESSION_RESULT)
                .join(SESSION).on(SESSION_RESULT.SESSION_ID.eq(SESSION.ID))
                .join(DRIVER).on(SESSION_RESULT.DRIVER_ID.eq(DRIVER.ID))
                .where(whereClause)
                .asTable(tableName);
    }

    private Field<?>[] sessionResultsFieldsFrom(Table<Record> table) {
        List<Field<?>> fieldList = new ArrayList<>(List.of(SESSION_RESULT.fields()));
        fieldList.add(DRIVER.DRIVER_NUMBER.as("driver_number"));

        return fieldList.stream()
                .map(field -> table.field(field.getName(), field.getDataType()).as(field.getName()))
                .toArray(Field<?>[]::new);
    }

    private SessionResultRecord buildSessionResult(SessionResult sessionResult, String previousId) {
        SessionResultRecord sessionResultRecord = new SessionResultRecord();
        if (previousId != null) {
            sessionResultRecord.setId(previousId);
        } else {
            sessionResultRecord.setId(UUID.randomUUID().toString());
        }
        sessionResultRecord.setDriverId(sessionResult.getDriverId());
        sessionResultRecord.setSessionId(sessionResult.getSessionId());
        sessionResultRecord.setDuration(sessionResult.getDuration());
        sessionResultRecord.setGapToLeader(sessionResult.getGapToLeader());
        sessionResultRecord.setNumberOfLaps(sessionResult.getNumberOfLaps());
        sessionResultRecord.setPosition(sessionResult.getPosition());
        sessionResultRecord.setDnf(sessionResult.getDnf());
        sessionResultRecord.setDns(sessionResult.getDns());
        sessionResultRecord.setDsq(sessionResult.getDsq());
        return sessionResultRecord;
    }
}
