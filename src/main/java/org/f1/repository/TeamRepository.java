package org.f1.repository;

import org.f1.domain.TeamLookup;
import org.f1.domain.openf1.Team;
import org.f1.generated.tables.records.TeamRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.f1.generated.Tables.TEAM;


@Repository
public class TeamRepository {

    DSLContext dslContext;

    public TeamRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Team saveTeam(TeamLookup team) {
        TeamRecord teamRecord = new TeamRecord(team.getId(), team.getLineageName());

        TeamRecord returnedTeamRecord = dslContext.insertInto(TEAM)
                .set(teamRecord)
                .onConflict()
                .doNothing()
                .returning()
                .fetchOne();

        if (returnedTeamRecord == null) {
            return null;
        }

        return new Team(returnedTeamRecord.getId(), returnedTeamRecord.getTeamName());

    }

    public Integer getTeam(String teamName) {

        return dslContext.select(TEAM.ID)
                .from(TEAM)
                .where(TEAM.TEAM_NAME.eq(teamName))
                .limit(1)
                .fetchOneInto(Integer.class);
    }
}
