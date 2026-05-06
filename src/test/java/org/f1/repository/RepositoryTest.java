package org.f1.repository;

import org.f1.config.DatabaseConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
import org.springframework.boot.test.autoconfigure.jooq.JooqTypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ResourceLock("postgres")
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import({DatabaseConfiguration.class})
@ComponentScan(value = {"org.f1.repository"})
@TypeExcludeFilters(value = JooqTypeExcludeFilter.class)
@TestPropertySource(locations = {"classpath:application.properties"})
@AutoConfigureJooq
public @interface RepositoryTest {

}
