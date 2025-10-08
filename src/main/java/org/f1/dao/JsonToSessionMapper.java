package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.openf1.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.f1.domain.openf1.Session.SessionBuilder.aSession;

@Component
public class JsonToSessionMapper {

    public List<Session> mapSessions(JSONArray jsonArray) {
        List<Session> sessions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            sessions.add(aSession()
                    .withId(jsonObject.getIntValue("session_key"))
                    .withMeetingId(jsonObject.getIntValue("meeting_key"))
                    .withStartDate(jsonObject.getDate("date_start"))
                    .withEndDate(jsonObject.getDate("date_end"))
                    .withSessionName(jsonObject.getString("session_name"))
                    .withSessionType(jsonObject.getString("session_type"))
                    .build());
        }

        return sessions;
    }

}
