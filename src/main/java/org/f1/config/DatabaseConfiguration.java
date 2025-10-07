package org.f1.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.tools.jdbc.DefaultConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jooq.DSLContext;


@Configuration
public class DatabaseConfiguration {
    @Bean
    public DSLContext dslContext(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String userName,
            @Value("${spring.datasource.password}") String password) {

        DSLContext dslContext = DSL.using(url, userName, password);
        dslContext.setSchema("f1");

        return dslContext;
    }
}

