package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
import org.f1.domain.openf1.SessionResult;
import org.f1.generated.tables.records.SessionResultRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.f1.generated.Tables.DRIVER;
import static org.f1.generated.Tables.SESSION;
import static org.f1.generated.tables.SessionResult.SESSION_RESULT;


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
        return dslContext.select(SESSION_RESULT.fields())
                .select(DRIVER.DRIVER_NUMBER.as("driver_number"))
                .from(SESSION_RESULT
                        .join(SESSION).on(SESSION_RESULT.SESSION_ID.eq(SESSION.ID))
                        .join(DRIVER).on(SESSION_RESULT.DRIVER_ID.eq(DRIVER.ID)))
                .where(SESSION.MEETING_ID.eq(meetingEntityReference.getMeetingId()))
                .and(SESSION.SESSION_NAME.eq(sessionName))
                .and(DRIVER.TEAM_ID.eq(meetingEntityReference.getTeamId()))
                .fetchInto(SessionResult.class);
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
