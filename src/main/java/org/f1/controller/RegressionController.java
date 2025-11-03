package org.f1.controller;

import org.f1.service.RegressionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regression")
public class RegressionController {

    private final RegressionService regressionService;

    public RegressionController(RegressionService regressionService) {
        this.regressionService = regressionService;
    }


    @GetMapping("/nsad")
    public ResponseEntity<?> populateNSADRegressionData() {
        regressionService.populateNSADRegressionData();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainNSAD")
    public ResponseEntity<?> trainNSADRegressionModel() {
        regressionService.trainNSADRegressionModel();
        return ResponseEntity.ok().build();
    }


}
