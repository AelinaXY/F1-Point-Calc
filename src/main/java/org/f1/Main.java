package org.f1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication()
@EnableCaching
@ConfigurationPropertiesScan(value = "org.f1.config")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    //V2 Score Calculator should use https://openf1.org/?shell#introduction
    //Rather than the CSV data in order to populate FP1,FP2,FP3 data from the weekend of the session

//    Test

//    After Spain
//    [0.5900000000000003, 0.3200000000000001, 0.08999999999999958]=262.2117625477216

//    After Canada
//    [0.635, 0.2800000000000001, 0.08499999999999991]=266.8458486748592

//    With Track Similarity Mapping
//    [0.5800000000000003, 0.36000000000000015, 0.059999999999999554]=264.6386895301757

//    After Austria
//    [0.5600000000000003, 0.38000000000000017, 0.059999999999999554]=269.9495371224135

//    With improved regression
//    [0.54, 0.46, 0]=257.85154744896784

//    After Britain
//    Average: 0.49
//    4d1Avg: 0.44
//    Forecast: 0.01
//    Track Similarity: 0.134
//    Sprint Mult: 1.15
//    MSE: 242.4

//    After Belgium
//    Average: 0.48
//    4d1Avg: 0.44
//    Forecast: 0.01
//    Track Similarity: 0.114
//    Sprint Mult: 1.18
//    MSE: 241.3

//    After Hungary
//    Average: 0.47
//    4d1Avg: 0.45
//    Forecast: 0.01
//    Track Similarity: 0.114
//    Sprint Mult: 1.17
//    MSE: 239.9
}