package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.openf1.Circuit;
import org.f1.domain.openf1.Country;
import org.f1.domain.openf1.Meeting;
import org.f1.domain.openf1.SessionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;

import static org.f1.domain.openf1.Meeting.MeetingBuilder.aMeeting;
import static org.f1.domain.openf1.SessionResult.SessionResultBuilder.aSessionResult;

@Component
public class JsonToSessionResultMapper {


    public List<SessionResult> mapSessionResults(JSONArray jsonArray) {
        List<SessionResult> sessionResults = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            //FOR QUALI DURATION AND GAP TO LEADER ARE ARRAYS
            try {
                Double duration = getDoubleFromArray(jsonObject.get("duration"));
                Double gapToLeader = getDoubleFromArray(jsonObject.get("gap_to_leader"));

                sessionResults.add(aSessionResult()
                        .withDriverNumber(jsonObject.getIntValue("driver_number"))
                        .withSessionId(jsonObject.getIntValue("session_key"))
                        .withDuration(duration)
                        .withGapToLeader(gapToLeader)
                        .withNumberOfLaps(jsonObject.getIntValue("number_of_laps"))
                        .withPosition(jsonObject.getIntValue("position"))
                        .withDnf(jsonObject.getBoolean("dnf"))
                        .withDns(jsonObject.getBoolean("dns"))
                        .withDsq(jsonObject.getBoolean("dsq"))
                        .build()
                );
            } catch (Exception e) {
                System.out.println(jsonObject);
            }
        }

        return sessionResults;
    }

    private Double getDoubleFromArray(Object potentialArray) {
        if (Objects.isNull(potentialArray)) {
            return null;
        } else if (potentialArray instanceof JSONArray) {
            return ((JSONArray) potentialArray).toJavaList(Double.class)
                    .stream()
                    .filter(Objects::nonNull)
                    .reduce(BinaryOperator.maxBy(Comparator.naturalOrder()))
                    .orElse(null);

        } else if (potentialArray instanceof String) {
            return null;
        } else {
            return Double.parseDouble(potentialArray.toString());
        }
    }
}
