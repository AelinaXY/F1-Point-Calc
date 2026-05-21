package org.f1.repository;

import org.f1.domain.NSAD;
import org.f1.generated.tables.records.NonSprintAggregateDataRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static org.f1.generated.Tables.NON_SPRINT_AGGREGATE_DATA;


@Repository
public class NSADRepository {

    DSLContext dslContext;

    public NSADRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void saveNSAD(Set<NSAD> nsadSet) {

        for (NSAD nsad : nsadSet) {
            NonSprintAggregateDataRecord record = toRecord(nsad);
            dslContext.insertInto(NON_SPRINT_AGGREGATE_DATA)
                    .set(record)
                    .onConflict(NON_SPRINT_AGGREGATE_DATA.MEETING_ENTITY_REFERENCE)
                    .doUpdate()
                    .set(record)
                    .execute();
        }
    }

    public List<NSAD> getAll() {
        return dslContext.selectFrom(NON_SPRINT_AGGREGATE_DATA)
                .fetch().map(NSAD::fromRecord);
    }

    private NonSprintAggregateDataRecord toRecord(NSAD nsadRecord) {
        NonSprintAggregateDataRecord record = new NonSprintAggregateDataRecord();
        record.setMeetingEntityReference(nsadRecord.getMeetingEntityReference());
        record.setActualPoints(nsadRecord.getActualPoints());
        record.setAvgPoints(nsadRecord.getAvgPoints());
        record.setAvg_4d1Points(nsadRecord.getAvg4d1Points());
        record.setStdev(nsadRecord.getStdev());
        record.setIsTeam(nsadRecord.getIsTeam());
        record.setIsSprint(nsadRecord.getIsSprint());
        record.setTeamId(nsadRecord.getTeamId());
        record.setDaysSinceFirstRace(nsadRecord.getDaysSinceFirstRace());
        record.setFp1Available(nsadRecord.getFp1Available() == 1 ? Boolean.TRUE : Boolean.FALSE);
        record.setFp1Pos(nsadRecord.getFp1Pos());
        record.setFp1Gap(nsadRecord.getFp1Gap());
        record.setFp1LapsDone(nsadRecord.getFp1LapsDone().intValue());
        record.setFp2Available(nsadRecord.getFp2Available() == 1 ? Boolean.TRUE : Boolean.FALSE);
        record.setFp2Pos(nsadRecord.getFp2Pos());
        record.setFp2Gap(nsadRecord.getFp2Gap());
        record.setFp2LapsDone(nsadRecord.getFp2LapsDone().intValue());
        return record;
    }
}
