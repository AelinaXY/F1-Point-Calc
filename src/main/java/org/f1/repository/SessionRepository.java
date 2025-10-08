package org.f1.repository;

import org.f1.domain.openf1.Circuit;
import org.f1.domain.openf1.Session;
import org.f1.generated.tables.records.CircuitRecord;
import org.f1.generated.tables.records.SessionRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;

import static org.f1.generated.tables.Session.SESSION;


@Repository
public class SessionRepository {

    DSLContext dslContext;

    public SessionRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public SessionRecord saveSession(Session session) {

        SessionRecord sessionRecord = buildSession(session);

        return dslContext.insertInto(SESSION)
                .set(sessionRecord)
                .onConflict(SESSION.ID)
                .doUpdate()
                .set(sessionRecord)
                .returning()
                .fetchOne();
    }

    private SessionRecord buildSession(Session session) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setId(session.id());
        sessionRecord.setMeetingId(session.meetingId());
        sessionRecord.setDateStart(session.startDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
        sessionRecord.setDateEnd(session.endDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
        sessionRecord.setSessionName(session.sessionName());
        sessionRecord.setSessionType(session.sessionType());
        return sessionRecord;

    }
}
