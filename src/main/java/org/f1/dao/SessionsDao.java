package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.traverson.Traverson;
import uk.co.autotrader.traverson.http.Response;

@Repository
public class SessionsDao {

    private Traverson traverson;
    private String baseUrl;


    SessionsDao(Traverson traverson, @Value("${openf1.url}") String url) {
        this.traverson = traverson;
        baseUrl = url;
    }

    public JSONArray getAllSessions() {
        Response<String> response = traverson.from(baseUrl + "sessions")
                .get(String.class);


        if (response.isSuccessful()) {
            JSONArray responseArray = JSONArray.parse(response.getResource());
            return responseArray;

        }
        return null;
    }

}
