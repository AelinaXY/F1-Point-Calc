package org.f1.repository;

import org.f1.domain.NSAD;
import org.f1.generated.tables.records.NonSprintAggregateDataRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.f1.generated.Tables.*;


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
                    .execute();
        }
    }

    private NonSprintAggregateDataRecord toRecord(NSAD nsadRecord) {
        NonSprintAggregateDataRecord record = new NonSprintAggregateDataRecord();

        record.setMeetingEntityReference(nsadRecord.meetingEntityReference());
        record.setActualPoints(nsadRecord.actualPoints());
        record.setAvgPoints(nsadRecord.avgPoints());
        record.setAvg_4d1Points(nsadRecord.avg4d1Points());
        record.setStdev(nsadRecord.stdev());
        return record;
    }
}
