package org.f1.repository;

import org.f1.domain.MeetingEntityReference;
import org.f1.domain.NSAD;
import org.f1.generated.tables.records.NonSprintAggregateDataRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.f1.generated.Tables.NON_SPRINT_AGGREGATE_DATA;


@Repository
public class NSADRepository {

    DSLContext dslContext;
    private final MERRepository merRepository;

    public NSADRepository(DSLContext dslContext, MERRepository merRepository) {
        this.dslContext = dslContext;
        this.merRepository = merRepository;
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
                .fetch()
                .map(record -> fromRecord(
                        record,
                        merRepository.getMeetingEntityReference(record.getMeetingEntityReference())
                ));
    }

    private NonSprintAggregateDataRecord toRecord(NSAD nsadRecord) {
        NonSprintAggregateDataRecord record = new NonSprintAggregateDataRecord();
        record.setMeetingEntityReference(Objects.requireNonNull(nsadRecord.getMeetingEntityReference()).getId());
        record.setActualPoints(nsadRecord.getActualPoints());
        record.setAvgPoints(nsadRecord.getAvgPoints());
        record.setAvg_4d1Points(nsadRecord.getAvg4d1Points());
        record.setStdev(nsadRecord.getStdev());
        record.setIsTeam(nsadRecord.getIsTeam());
        record.setTeamId(nsadRecord.getTeamId());
        record.setDaysSinceFirstRace(nsadRecord.getDaysSinceFirstRace());
        record.setFp1Pos(nsadRecord.getFp1Pos());
        record.setFp2Pos(nsadRecord.getFp2Pos());
        record.setSqPos(nsadRecord.getSqPos());
        record.setFp3Pos(nsadRecord.getFp3Pos());
        return record;
    }

    private NSAD fromRecord(NonSprintAggregateDataRecord record, MeetingEntityReference meetingEntityReference) {
        NSAD result = new NSAD();
        result.setId(record.getId());
        result.setMeetingEntityReference(meetingEntityReference);
        result.setActualPoints(record.getActualPoints());
        result.setAvgPoints(record.getAvgPoints());
        result.setAvg4d1Points(record.getAvg_4d1Points());
        result.setStdev(record.getStdev());
        result.setIsTeam(record.getIsTeam());
        result.setTeamId(record.getTeamId());
        result.setDaysSinceFirstRace(record.getDaysSinceFirstRace());
        result.setFp1Pos(record.getFp1Pos());
        result.setFp2Pos(record.getFp2Pos());
        result.setSqPos(record.getSqPos());
        result.setFp3Pos(record.getFp3Pos());
        return result;
    }
}
