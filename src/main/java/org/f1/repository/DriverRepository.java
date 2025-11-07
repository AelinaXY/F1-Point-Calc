package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
import org.f1.domain.openf1.Driver;
import org.f1.generated.tables.records.DriverRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.f1.generated.Tables.*;


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

    private DriverRecord populateDriverRecord(Driver driver, String previousId) {
        DriverRecord driverRecord = new DriverRecord();
        if (previousId != null) {
            driverRecord.setId(previousId);
        } else {
            driverRecord.setId(UUID.randomUUID().toString());
        }

        driverRecord.setBroadcastName(driver.broadcastName());
        driverRecord.setFirstName(driver.firstName());
        driverRecord.setLastName(driver.lastName());
        driverRecord.setCountryCode(driver.countryCode());
        driverRecord.setDriverNumber(driver.driverNumber());
        driverRecord.setMeetingId(driver.meetingId());
        driverRecord.setHeadshotUrl(driver.headshotUrl());
        driverRecord.setNameAcronym(driver.nameAcronym());
        driverRecord.setTeamId(driver.team().getId());
        driverRecord.setFullName(driver.fullName());

        return driverRecord;
    }

    public MeetingEntityReference getDriverMRFromYearAndMeetingNames(String fullName, int year, List<String> meetingNames) {
        return dslContext.select(DRIVER.ID, DRIVER.TEAM_ID, MEETING.ID)
                .from(DRIVER.join(MEETING)
                        .on(DRIVER.MEETING_ID.eq(MEETING.ID)))
                .where(MEETING.YEAR.eq(year))
                .and(DRIVER.FULL_NAME
                        .equalIgnoreCase(fullName))
                .and(MEETING.NAME.in(meetingNames))
                .limit(1)
                .fetchOneInto(MeetingEntityReference.class);
    }

    public Integer getLatestTeam(String fullName) {
        return dslContext.select(DRIVER.TEAM_ID)
                .from(DRIVER.join(MEETING)
                        .on(DRIVER.MEETING_ID.eq(MEETING.ID)))
                .where(DRIVER.FULL_NAME.equalIgnoreCase(fullName))
                .orderBy(MEETING.DATE_START.desc())
                .limit(1)
                .fetchOneInto(Integer.class);
    }
}
