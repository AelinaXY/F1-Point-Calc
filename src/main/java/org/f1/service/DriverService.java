package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.openf1.Driver;
import org.f1.repository.DriverRepository;
import org.f1.utils.StreamUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    OpenF1Dao openF1Dao;

    public DriverService(OpenF1Dao openF1Dao, DriverRepository driverRepository) {
        this.openF1Dao = openF1Dao;
        this.driverRepository = driverRepository;
    }

    public List<Driver> populateDrivers() {
        List<Driver> drivers = openF1Dao.getAllDrivers();

        Set<Driver> driverSet = drivers.stream().filter(StreamUtils.distinctByDualKey(Driver::meetingId, Driver::driverNumber)).collect(Collectors.toSet());
        driverSet.forEach(driverRepository::saveDriver);
        return drivers;
    }

    public String getDriverId(int driverNumber, int sessionId) {
        return driverRepository.getDriverIdFromNumberAndSessionId(driverNumber, sessionId);
    }
}
