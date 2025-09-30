package org.f1.domain.response;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public record OptimalTeamResponse(List<JSONObject> scoreCards, List<JSONObject> differenceEntities) {
}
