package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.openf1.Circuit;
import org.f1.domain.openf1.Country;
import org.f1.domain.openf1.Driver;
import org.f1.domain.openf1.Meeting;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.f1.domain.openf1.Driver.DriverBuilder.aDriver;
import static org.f1.domain.openf1.Meeting.MeetingBuilder.aMeeting;

@Component
public class JsonToDriverMapper {


    public List<Driver> mapDrivers(JSONArray jsonArray) {
        List<Driver> drivers = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            drivers.add(aDriver()
                    .withBroadcastName(jsonObject.getString("broadcast_name"))
                    .withCountryCode(jsonObject.getString("country_code"))
                    .withDriverNumber(jsonObject.getIntValue("driver_number"))
                    .withFirstName(jsonObject.getString("first_name"))
                    .withFullName(jsonObject.getString("full_name"))
                    .withHeadshotUrl(jsonObject.getString("headshot_url"))
                    .withLastName(jsonObject.getString("last_name"))
                    .withNameAcronym(jsonObject.getString("name_acronym"))
                    .withTeamName(jsonObject.getString("team_name"))
                    .withMeetingId(jsonObject.getIntValue("meeting_key"))
                    .build()
            );
        }

        return drivers;
    }

}
