package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.openf1.Circuit;
import org.f1.domain.openf1.Country;
import org.f1.domain.openf1.Meeting;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.f1.domain.openf1.Meeting.MeetingBuilder.aMeeting;

@Component
public class JsonToMeetingMapper {


    public List<Meeting> mapMeetings(JSONArray jsonArray) {
        List<Meeting> meetings = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            meetings.add(aMeeting()
                    .withId(jsonObject.getIntValue("meeting_key"))
                    .withCode(jsonObject.getString("meeting_code"))
                    .withLocation(jsonObject.getString("location"))
                    .withName(jsonObject.getString("meeting_name"))
                    .withOfficialName(jsonObject.getString("meeting_official_name"))
                    .withGmtOffset(jsonObject.getString("gmt_offset"))
                    .withDateStart(jsonObject.getDate("date_start"))
                    .withYear(jsonObject.getIntValue("year"))
                    .withCircuit(new Circuit(jsonObject.getIntValue("circuit_key"), jsonObject.getString("circuit_short_name")))
                    .withCountry(new Country(jsonObject.getIntValue("country_key"), jsonObject.getString("country_name"), jsonObject.getString("country_code"))).build());
        }

        return meetings;
    }

}
