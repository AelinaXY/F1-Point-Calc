package org.f1.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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

    public void getAllSessions() {
        Response<String> reponse = traverson.from(baseUrl + "sessions")
                .get(String.class);

        System.out.println(reponse.getResource());
    }

}
