package org.f1.domain.openf1;

public class Team {

    Integer id;
    String teamName;

    public Team(Integer id, String teamName) {
        this.id = id;
        this.teamName = teamName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
