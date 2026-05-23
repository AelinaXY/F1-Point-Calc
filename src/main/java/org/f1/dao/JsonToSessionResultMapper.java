package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.f1.domain.openf1.SessionResult;
import org.f1.exception.OpenF1IngestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;


@Component
public class JsonToSessionResultMapper {


    public List<SessionResult> mapSessionResults(JSONArray jsonArray) {
        List<SessionResult> sessionResults = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            try {
                Double duration = getDoubleFromArray(jsonObject.get("duration"));
                Double gapToLeader = getDoubleFromArray(jsonObject.get("gap_to_leader"));

                sessionResults.add(SessionResult.builder()
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
                throw new OpenF1IngestException("OpenF1 session_result row could not be mapped at index " + i
                        + " for session_key " + jsonObject.getIntValue("session_key")
                        + " and driver_number " + jsonObject.getIntValue("driver_number")
                        + ". Row: " + jsonObject, e);
            }
        }

        return sessionResults;
    }

    private Double getDoubleFromArray(Object potentialArray) {
        if (Objects.isNull(potentialArray)) {
            return null;
        } else if (potentialArray instanceof JSONArray) {
            List<Double> qualiTimeList = ((JSONArray) potentialArray).toJavaList(Double.class);
            int size = qualiTimeList.size();

            for (int i = size - 1; i >= 0; i--) {
                if (qualiTimeList.get(i) != null) {
                    return qualiTimeList.get(i);
                }
            }
            return null;

        } else if (potentialArray instanceof String) {
            return null;
        } else {
            return Double.parseDouble(potentialArray.toString());
        }
    }
}
