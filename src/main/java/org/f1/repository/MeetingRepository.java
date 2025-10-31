package org.f1.repository;

import org.f1.domain.openf1.Meeting;
import org.f1.generated.tables.records.MeetingRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;

import static org.f1.generated.tables.Meeting.MEETING;


@Repository
public class MeetingRepository {

    private final DSLContext dslContext;

    public MeetingRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public MeetingRecord saveMeeting(Meeting meeting) {

        MeetingRecord meetingRecord = populateMeetingRecord(meeting);

        return dslContext
                .insertInto(MEETING)
                .set(meetingRecord)
                .onConflict(MEETING.ID)
                .doUpdate()
                .set(meetingRecord)
                .returning()
                .fetchOne();
    }

    private MeetingRecord populateMeetingRecord(Meeting meeting) {
        MeetingRecord meetingRecord = new MeetingRecord();
        meetingRecord.setId(meeting.id());
        meetingRecord.setName(meeting.name());
        meetingRecord.setLocation(meeting.location());
        meetingRecord.setCircuitId(meeting.circuit().id());
        meetingRecord.setCountryId(meeting.country().id());
        meetingRecord.setDateStart(meeting.startDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
        meetingRecord.setYear(meeting.year());
        meetingRecord.setOfficialName(meeting.officialName());
        return meetingRecord;
    }

    public Integer getMeeting(int year, List<String> fullNames) {

        return dslContext.select(MEETING.ID)
                .from(MEETING)
                .where(MEETING.YEAR.eq(year))
                .and(MEETING.NAME.in(fullNames))
                .limit(1)
                .fetchOneInto(Integer.class);
    }
}
