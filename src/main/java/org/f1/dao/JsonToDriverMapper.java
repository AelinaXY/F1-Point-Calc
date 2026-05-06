package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.TeamLookup;
import org.f1.domain.openf1.Driver;
import org.f1.domain.openf1.Team;
import org.f1.exception.OpenF1IngestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.f1.domain.openf1.Driver.DriverBuilder.aDriver;

@Component
public class JsonToDriverMapper {


    public List<Driver> mapDrivers(JSONArray jsonArray) {
        List<Driver> drivers = new ArrayList<>();
        List<String> unmappedDrivers = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String teamName = jsonObject.getString("team_name");
            TeamLookup preferredTeamName = TeamLookup.apiToTeam(teamName);

            if (preferredTeamName == null) {
                unmappedDrivers.add("OpenF1 driver row has unmapped team_name '" + teamName
                                + "' for driver_number " + jsonObject.getIntValue("driver_number")
                                + " and meeting_key " + jsonObject.getIntValue("meeting_key")
                                + " and driver_name " + jsonObject.getString("full_name"));
                preferredTeamName = TeamLookup.NONEXISTENT;
            }

            drivers.add(aDriver()
                    .withBroadcastName(jsonObject.getString("broadcast_name"))
                    .withCountryCode(jsonObject.getString("country_code"))
                    .withDriverNumber(jsonObject.getIntValue("driver_number"))
                    .withFirstName(jsonObject.getString("first_name"))
                    .withFullName(cleanDriverName(jsonObject.getString("full_name")))
                    .withHeadshotUrl(jsonObject.getString("headshot_url"))
                    .withLastName(jsonObject.getString("last_name"))
                    .withNameAcronym(jsonObject.getString("name_acronym"))
                    .withTeam(new Team(preferredTeamName.getId(), preferredTeamName.getLineageName()))
                    .withMeetingId(jsonObject.getIntValue("meeting_key"))
                    .build()
            );
        }

        if(!unmappedDrivers.isEmpty()) {
            throw new OpenF1IngestException("Unmapped drivers: " + unmappedDrivers);
        }

        return drivers;
    }

    private String cleanDriverName(String name) {
        if (name != null && name.equalsIgnoreCase("Andrea Kimi ANTONELLI")) {
            return "Kimi ANTONELLI";
        }
        return name;

    }

}
