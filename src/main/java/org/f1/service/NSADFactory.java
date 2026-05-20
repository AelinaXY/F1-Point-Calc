package org.f1.service;

import org.f1.calculations.ScoreCalculator;
import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.MeetingEntityReference;
import org.f1.domain.NSAD;
import org.f1.domain.Race;
import org.f1.domain.openf1.SessionResult;
import org.f1.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NSADFactory {
    private static final String FP1_SESSION_NAME = "Practice 1";
    private static final int MISSING_FP1_POSITION = 23;
    private static final double MISSING_FP1_GAP = -1d;
    private static final double MISSING_FP1_LAPS_DONE = -1d;

    private final MERService merService;
    private final MeetingService meetingService;
    private final SessionResultService sessionResultService;

    public NSADFactory(MERService merService, MeetingService meetingService, SessionResultService sessionResultService) {
        this.merService = merService;
        this.meetingService = meetingService;
        this.sessionResultService = sessionResultService;
    }

    public Optional<NSAD> createLabelled(FullPointEntity fullPointEntity, Meeting meeting, boolean isSprint) {
        String raceName = meeting.getShortName();
        List<Double> pointHistory = getPointHistory(fullPointEntity, raceName);
        if (pointHistory.isEmpty()) {
            return Optional.empty();
        }

        MeetingEntityReference meetingEntityReference =
                merService.getOrCreateMeetingEntityReference(fullPointEntity.getYear(), meeting, fullPointEntity);

        NSAD nsad = buildBaseNsad(fullPointEntity, meeting, isSprint, meetingEntityReference, pointHistory);
        nsad.setMeetingEntityReference(meetingEntityReference.getId());
        nsad.setActualPoints(getActualPoints(fullPointEntity, raceName));
        return Optional.of(nsad);
    }

    public NSAD createUnlabelled(FullPointEntity fullPointEntity, Meeting meeting, boolean isSprint) {
        MeetingEntityReference meetingEntityReference =
                merService.getOrCreateMeetingEntityReference(fullPointEntity.getYear(), meeting, fullPointEntity);
        List<Double> pointHistory = getPointHistory(fullPointEntity, meeting.getShortName());
        return buildBaseNsad(fullPointEntity, meeting, isSprint, meetingEntityReference, pointHistory);
    }

    private NSAD buildBaseNsad(
            FullPointEntity fullPointEntity,
            Meeting meeting,
            boolean isSprint,
            MeetingEntityReference meetingEntityReference,
            List<Double> pointHistory
    ) {
        SessionSummary fp1Summary = getFp1Summary(meetingEntityReference);

        NSAD nsad = new NSAD();
        nsad.setAvgPoints(ScoreCalculator.calcAveragePoints(pointHistory));
        nsad.setAvg4d1Points(ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointHistory)));
        nsad.setStdev(MathUtils.stdev(pointHistory));
        nsad.setIsTeam(booleanToDouble(fullPointEntity.isTeam()));
        nsad.setIsSprint(booleanToDouble(isSprint));
        nsad.setTeamId((double) meetingEntityReference.getTeamId());
        nsad.setDaysSinceFirstRace(meetingService.getDaysSinceFirstRace(fullPointEntity.getYear(), meeting.getFullNames()));
        nsad.setFp1Available(booleanToDouble(fp1Summary.available()));
        nsad.setFp1Pos(fp1Summary.position() == null ? MISSING_FP1_POSITION : Math.toIntExact(fp1Summary.position()));
        nsad.setFp1Gap(fp1Summary.gap() == null ? MISSING_FP1_GAP : fp1Summary.gap());
        nsad.setFp1LapsDone(fp1Summary.lapsDone() == null ? MISSING_FP1_LAPS_DONE : fp1Summary.lapsDone());
        return nsad;
    }

    private SessionSummary getFp1Summary(MeetingEntityReference meetingEntityReference) {
        List<SessionResult> fp1Results = sessionResultService.getSessionResultsFromName(meetingEntityReference, FP1_SESSION_NAME);
        if (fp1Results.isEmpty()
                || fp1Results.stream().anyMatch(result ->
                result.getPosition() == null || result.getGapToLeader() == null || result.getNumberOfLaps() == null)) {
            return new SessionSummary(false, null, null, null);
        }

        if (fp1Results.size() == 1) {
            SessionResult fp1Result = fp1Results.getFirst();
            return new SessionSummary(
                    true,
                    Long.valueOf(fp1Result.getPosition()),
                    fp1Result.getGapToLeader(),
                    (double) fp1Result.getNumberOfLaps()
            );
        }

        double totalPosition = 0;
        double totalGap = 0;
        double totalLaps = 0;

        for (SessionResult sessionResult : fp1Results) {
            totalPosition += sessionResult.getPosition();
            totalGap += sessionResult.getGapToLeader();
            totalLaps += sessionResult.getNumberOfLaps();
        }

        return new SessionSummary(
                true,
                Math.round(totalPosition / fp1Results.size()),
                totalGap / fp1Results.size(),
                totalLaps / fp1Results.size()
        );
    }

    private int getActualPoints(FullPointEntity fullPointEntity, String raceName) {
        return fullPointEntity.getRaceList()
                .stream()
                .filter(race -> race.name().equals(raceName))
                .findFirst()
                .orElseThrow()
                .totalPoints()
                .intValue();
    }

    private List<Double> getPointHistory(FullPointEntity fullPointEntity, String raceName) {
        return fullPointEntity.getRaceList()
                .stream()
                .takeWhile(race -> !race.name().equals(raceName))
                .map(Race::totalPoints)
                .toList();
    }

    private double booleanToDouble(boolean value) {
        return value ? 1d : 0d;
    }

    private record SessionSummary(boolean available, Long position, Double gap, Double lapsDone) {
    }
}
