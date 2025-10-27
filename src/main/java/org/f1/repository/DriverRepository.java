package org.f1.repository;

import org.f1.domain.openf1.Driver;
import org.f1.generated.tables.records.DriverRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static org.f1.generated.Tables.DRIVER;
import static org.f1.generated.Tables.SESSION;


@Repository
public class DriverRepository {

    private final DSLContext dslContext;

    public DriverRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public DriverRecord saveDriver(Driver driver) {

        String previousId = dslContext.select(DRIVER.ID).from(DRIVER)
                .where(DRIVER.DRIVER_NUMBER
                        .eq(driver.driverNumber())
                        .and(DRIVER.MEETING_ID
                                .eq(driver.meetingId())))
                .fetchOneInto(String.class);

        DriverRecord driverRecord = populateDriverRecord(driver, previousId);

        return dslContext
                .insertInto(DRIVER)
                .set(driverRecord)
                .onConflict(DRIVER.ID)
                .doUpdate()
                .set(driverRecord)
                .returning()
                .fetchOne();
    }

    public String getDriverIdFromNumberAndSessionId(int driverNumber, int sessionid) {
        return dslContext.select(DRIVER.ID)
                .from(DRIVER.join(SESSION)
                        .on(DRIVER.MEETING_ID.eq(SESSION.MEETING_ID)))
                .where(DRIVER.DRIVER_NUMBER
                        .eq(driverNumber)
                        .and(SESSION.ID
                                .eq(sessionid)))
                .fetchOneInto(String.class);
    }

    private DriverRecord populateDriverRecord(Driver meeting, String previousId) {
        DriverRecord driverRecord = new DriverRecord();
        if (previousId != null) {
            driverRecord.setId(previousId);
        } else {
            driverRecord.setId(UUID.randomUUID().toString());
        }

        driverRecord.setBroadcastName(meeting.broadcastName());
        driverRecord.setFirstName(meeting.firstName());
        driverRecord.setLastName(meeting.lastName());
        driverRecord.setCountryCode(meeting.countryCode());
        driverRecord.setDriverNumber(meeting.driverNumber());
        driverRecord.setMeetingId(meeting.meetingId());
        driverRecord.setHeadshotUrl(meeting.headshotUrl());
        driverRecord.setNameAcronym(meeting.nameAcronym());
        driverRecord.setTeamName(meeting.teamName());
        driverRecord.setFullName(meeting.fullName());

        return driverRecord;
    }
}
