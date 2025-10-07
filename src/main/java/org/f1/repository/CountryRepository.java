package org.f1.repository;

import org.f1.domain.openf1.Country;
import org.f1.generated.tables.records.CountryRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.f1.generated.tables.Country.COUNTRY;


@Repository
public class CountryRepository {

    DSLContext dslContext;

    public CountryRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Country saveCountry(Country country) {

        CountryRecord countryRecord = new CountryRecord(country.id(), country.code(), country.name());

        CountryRecord returnedCountryRecord = dslContext.insertInto(COUNTRY)
                .set(countryRecord)
                .onConflict(COUNTRY.ID)
                .doUpdate()
                .set(countryRecord)
                .returning()
                .fetchOne();

        return new Country(returnedCountryRecord.getId(), returnedCountryRecord.getCode(), returnedCountryRecord.getName());
    }
}
