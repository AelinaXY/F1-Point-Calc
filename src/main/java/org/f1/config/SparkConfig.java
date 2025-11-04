package org.f1.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SparkConfig {

    @Primary
    @Bean
    public JavaSparkContext getJavaSparkContext() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("F1PointCalc")
                .setMaster("local")
                .set("spark.ui.enabled", "false")
                .set("spark.driver.host", "localhost")
                .set("spark.executor.userClassPathFirst", "true")
                .set("spark.driver.userClassPathFirst", "true")
                .set("spark.serializer", "org.apache.spark.serializer.JavaSerializer");

        return new JavaSparkContext(sparkConf);
    }
}
