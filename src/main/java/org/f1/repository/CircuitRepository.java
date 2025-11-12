package org.f1.repository;

import org.f1.domain.openf1.Circuit;
import org.f1.generated.tables.records.CircuitRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.f1.generated.tables.Circuit.CIRCUIT;


@Repository
public class CircuitRepository {

    DSLContext dslContext;

    public CircuitRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Circuit saveCircuit(Circuit circuit) {

        CircuitRecord circuitRecord = new CircuitRecord(circuit.id(), circuit.shortName());

        CircuitRecord returnedCircuitRecord = dslContext.insertInto(CIRCUIT)
                .set(circuitRecord)
                .onConflict(CIRCUIT.ID)
                .doUpdate()
                .set(circuitRecord)
                .returning()
                .fetchOne();

        return new Circuit(returnedCircuitRecord.getId(), returnedCircuitRecord.getShortName());
    }
}
