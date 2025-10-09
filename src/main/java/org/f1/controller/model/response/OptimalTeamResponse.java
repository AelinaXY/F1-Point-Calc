package org.f1.controller.model.response;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public record OptimalTeamResponse(JSONObject originalTeam, List<JSONObject> scoreCards, List<JSONObject> differenceEntities) {
}
