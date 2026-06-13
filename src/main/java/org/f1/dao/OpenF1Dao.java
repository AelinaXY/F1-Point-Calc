package org.f1.dao;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.apache.hc.core5.http.HttpHeaders;
import org.f1.domain.openf1.Driver;
import org.f1.domain.openf1.Meeting;
import org.f1.domain.openf1.Session;
import org.f1.domain.openf1.SessionResult;
import org.f1.exception.OpenF1IngestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.traverson.Traverson;
import uk.co.autotrader.traverson.http.Response;

import java.util.List;
import java.util.function.Function;

@Repository
public class OpenF1Dao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenF1Dao.class);

    private final JsonToSessionResultMapper jsonToSessionResultMapper;
    private final JsonToDriverMapper jsonToDriverMapper;
    private final JsonToSessionMapper jsonToSessionMapper;
    private final Traverson traverson;
    private final String baseUrl;
    private final JsonToMeetingMapper jsonToMeetingMapper;
    private final String openF1BearerToken;


    OpenF1Dao(Traverson traverson, @Value("${openf1.url}") String url, JsonToMeetingMapper jsonToMeetingMapper, JsonToSessionMapper jsonToSessionMapper, JsonToSessionResultMapper jsonToSessionResultMapper, JsonToDriverMapper jsonToDriverMapper, @Value("${openf1.bearer-token}") String openF1BearerToken) {
        this.traverson = traverson;
        baseUrl = url;
        this.jsonToMeetingMapper = jsonToMeetingMapper;
        this.jsonToSessionMapper = jsonToSessionMapper;
        this.jsonToSessionResultMapper = jsonToSessionResultMapper;
        this.jsonToDriverMapper = jsonToDriverMapper;
        this.openF1BearerToken = openF1BearerToken;
    }

    public List<Session> getAllSessions() {
        return getOpenF1List("sessions", jsonToSessionMapper::mapSessions);
    }

    public List<Meeting> getAllMeetings() {
        return getOpenF1List("meetings", jsonToMeetingMapper::mapMeetings);
    }

    public List<Driver> getAllDrivers() {
        return getOpenF1List("drivers", jsonToDriverMapper::mapDrivers);
    }

    public List<SessionResult> getAllSessionResults() {
        return getOpenF1List("session_result", jsonToSessionResultMapper::mapSessionResults);
    }

    private <T> List<T> getOpenF1List(String endpoint, Function<JSONArray, List<T>> mapper) {
        String url = baseUrl + endpoint;
        Response<String> response;

        response = traverson.from(url).withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openF1BearerToken).get(String.class);

        if (response.isFailure()) {
            String message = "OpenF1 request failed for " + endpoint
                    + " with status " + response.getStatusCode()
                    + " and response body: " + response.getResource();
            LOGGER.error(message);
            throw new OpenF1IngestException(message);
        }

        JSONArray responseArray = JSON.parseArray(response.getResource());
        List<T> mappedResults = mapper.apply(responseArray);
        LOGGER.info("Ingested {} records from OpenF1 endpoint {}", mappedResults.size(), endpoint);
        return mappedResults;
    }
}
