package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import org.f1.domain.openf1.Meeting;
import org.f1.domain.openf1.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.traverson.Traverson;
import uk.co.autotrader.traverson.http.Response;

import java.util.List;

@Repository
public class OpenF1Dao {

    private JsonToSessionMapper jsonToSessionMapper;
    private Traverson traverson;
    private String baseUrl;
    private JsonToMeetingMapper jsonToMeetingMapper;


    OpenF1Dao(Traverson traverson, @Value("${openf1.url}") String url, JsonToMeetingMapper jsonToMeetingMapper, JsonToSessionMapper jsonToSessionMapper) {
        this.traverson = traverson;
        baseUrl = url;
        this.jsonToMeetingMapper = jsonToMeetingMapper;
        this.jsonToSessionMapper = jsonToSessionMapper;
    }

    public List<Session> getAllSessions() {
        Response<String> response = traverson.from(baseUrl + "sessions")
                .get(String.class);


        if (response.isSuccessful()) {
            JSONArray responseArray = JSONArray.parse(response.getResource());
            return jsonToSessionMapper.mapSessions(responseArray);

        }
        return null;
    }

    public List<Meeting> getAllMeetings() {
        Response<String> response = traverson.from(baseUrl + "meetings")
                .get(String.class);

        if (response.isSuccessful()) {
            JSONArray responseArray = JSONArray.parse(response.getResource());
            return jsonToMeetingMapper.mapMeetings(responseArray);
        }
        return null;
    }

}
