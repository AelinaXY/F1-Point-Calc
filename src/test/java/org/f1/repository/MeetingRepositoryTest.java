package org.f1.repository;

import org.f1.generated.tables.records.MeetingRecord;
import org.jooq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.f1.domain.openf1.Meeting.MeetingBuilder.aMeeting;
import static org.f1.generated.Tables.MEETING;
import static org.junit.jupiter.api.Assertions.*;

@RepositoryTest
class MeetingRepositoryTest {
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(MEETING).execute();
    }

    @Test
    void find_days_since_first_race_returns_correct_value() {
        insertMeeting(1, "meeting1", 2022, OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        insertMeeting(2, "meeting2", 2022, OffsetDateTime.of(2022, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC));
        insertMeeting(3, "meeting3", 2022, OffsetDateTime.of(2022, 1, 20, 0, 0, 0, 0, ZoneOffset.UTC));

        long daysSince = meetingRepository.getDaysSinceFirstRace(2022, List.of("meeting3"));

        assertEquals(19, daysSince);
    }

    public void insertMeeting(int id, String name, int year, OffsetDateTime date) {
        final MeetingRecord meetingRecord = new MeetingRecord()
                .setId(id)
                .setYear(year)
                .setName(name)
                .setDateStart(date);

        dslContext.insertInto(MEETING).set(meetingRecord).execute();

        aMeeting()
                .withId(id)
                .withName(name)
                .withStartDate(date)
                .withYear(year).build();
    }
}


