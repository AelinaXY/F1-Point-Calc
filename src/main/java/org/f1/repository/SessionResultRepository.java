package org.f1.repository;

import com.google.crypto.tink.subtle.Random;
import org.f1.domain.openf1.Session;
import org.f1.domain.openf1.SessionResult;
import org.f1.generated.tables.records.SessionRecord;
import org.f1.generated.tables.records.SessionResultRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.UUID;

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
                        .eq(sessionResult.sessionId())
                        .and(SESSION_RESULT.DRIVER_NUMBER
                                .eq(sessionResult.driverNumber())))
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

    private SessionResultRecord buildSessionResult(SessionResult sessionResult, String previousId) {
        SessionResultRecord sessionResultRecord = new SessionResultRecord();
        if (previousId != null) {
            sessionResultRecord.setId(previousId);
        } else {
            sessionResultRecord.setId(UUID.randomUUID().toString());
        }
        sessionResultRecord.setDriverNumber(sessionResult.driverNumber());
        sessionResultRecord.setSessionId(sessionResult.sessionId());
        sessionResultRecord.setDuration(sessionResult.duration());
        sessionResultRecord.setGapToLeader(sessionResult.gapToLeader());
        sessionResultRecord.setNumberOfLaps(sessionResult.numberOfLaps());
        sessionResultRecord.setPosition(sessionResult.position());
        sessionResultRecord.setDnf(sessionResult.dnf());
        sessionResultRecord.setDns(sessionResult.dns());
        sessionResultRecord.setDsq(sessionResult.dsq());
        return sessionResultRecord;

    }
}
