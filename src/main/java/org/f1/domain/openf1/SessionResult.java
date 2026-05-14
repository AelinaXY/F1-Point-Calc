package org.f1.domain.openf1;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true, setterPrefix = "with")
@AllArgsConstructor
public class SessionResult {
    private String id;
    private Integer sessionId;
    private String driverId;
    private Double duration;
    private Double gapToLeader;
    private Integer numberOfLaps;
    private Integer position;
    private Boolean dnf;
    private Boolean dns;
    private Boolean dsq;
    private Integer driverNumber;
}
