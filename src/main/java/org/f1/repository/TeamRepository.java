package org.f1.repository;

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

    public Team saveTeam(String teamName) {

        TeamRecord teamRecord = new TeamRecord();
        teamRecord.setTeamName(teamName);

        TeamRecord returnedTeamRecord = dslContext.insertInto(TEAM)
                .set(teamRecord)
                .onConflict()
                .doNothing()
                .returning()
                .fetchOne();

        if (returnedTeamRecord == null) {
            Integer teamId = getTeam(teamName);
            return new Team(teamId, teamName);
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
