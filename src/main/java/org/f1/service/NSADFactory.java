package org.f1.service;

import org.f1.calculations.ScoreCalculator;
import org.f1.domain.*;
import org.f1.domain.openf1.SessionResult;
import org.f1.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NSADFactory {
    private static final String FP1_SESSION_NAME = "Practice 1";
    private static final String FP2_SESSION_NAME = "Practice 2";
    private static final String SPRINT_QUALIFYING_NAME = "Sprint Qualifying";
    private static final String FP3_SESSION_NAME = "Practice 3";
    private static final String QUALIFYING_SESSION_NAME = "Qualifying";


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
        SessionSummary fp1Summary = getSessionSummary(meetingEntityReference, FP1_SESSION_NAME);
        SessionSummary fp2Summary = getSessionSummary(meetingEntityReference, FP2_SESSION_NAME);
        SessionSummary sqSummary = getSessionSummary(meetingEntityReference, SPRINT_QUALIFYING_NAME);
        SessionSummary fp3Summary = getSessionSummary(meetingEntityReference, FP3_SESSION_NAME);

        NSAD nsad = new NSAD();
        nsad.setMeetingEntityReference(meetingEntityReference);
        nsad.setAvgPoints(ScoreCalculator.calcAveragePoints(pointHistory));
        nsad.setAvg4d1Points(ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointHistory)));
        nsad.setStdev(MathUtils.stdev(pointHistory));
        nsad.setIsTeam(booleanToDouble(fullPointEntity.isTeam()));
        nsad.setTeamId((double) meetingEntityReference.getTeamId());
        nsad.setDaysSinceFirstRace(meetingService.getDaysSinceFirstRace(fullPointEntity.getYear(), meeting.getFullNames()));
        nsad.setFp1Pos(fp1Summary.getPosition());
        nsad.setFp2Pos(fp2Summary.getPosition());
        nsad.setSqPos(sqSummary.getPosition());
        nsad.setFp3Pos(fp3Summary.getPosition());
        nsad.setQualiConversionDelta(getQualiConversionDelta(meetingEntityReference));
        nsad.setPreviousQualiPos(getPreviousQualiPerformance(meetingEntityReference, 1));
        nsad.setAvg4d1QualiPos(getPreviousQualiPerformance(meetingEntityReference, 4));
        return nsad;
    }

    private double getQualiConversionDelta(MeetingEntityReference meetingEntityReference) {
        List<SessionResultsSummary> sessionResultsSummaryList = sessionResultService.findMappedResultsForDriverOrTeam(meetingEntityReference, List.of(FP1_SESSION_NAME, FP2_SESSION_NAME, FP3_SESSION_NAME), List.of(QUALIFYING_SESSION_NAME));
        double lambda = 0.35;

        double totalQualiDelta = 0;
        double totalQualiDeltaCount = 0;

        for (SessionResultsSummary summary : sessionResultsSummaryList) {
            double avgQualiPos = 0d;
            double avgPracticePos = 0d;

            for (SessionResult result : summary.qualiSessionResults()) {
                avgQualiPos += result.getPosition();
            }
            avgQualiPos /= summary.qualiSessionResults().size();

            for (SessionResult result : summary.practiseSessionResults()) {
                avgPracticePos += result.getPosition();
            }
            avgPracticePos /= summary.practiseSessionResults().size();

            double currentExponent = Math.exp(-lambda * totalQualiDeltaCount);
            totalQualiDelta += (avgQualiPos - avgPracticePos) * currentExponent;
            totalQualiDeltaCount+= currentExponent;
        }

        return totalQualiDelta / totalQualiDeltaCount;
    }

    private double getPreviousQualiPerformance(MeetingEntityReference meetingEntityReference, int numberOfMeetings) {
        return Optional.ofNullable(sessionResultService.getPreviousSessionAvgPos(meetingEntityReference, numberOfMeetings, QUALIFYING_SESSION_NAME)).orElse(-1d);
    }

    private SessionSummary getSessionSummary(MeetingEntityReference meetingEntityReference, String sessionName) {
        List<SessionResult> sessionResults = sessionResultService.getSessionResultsFromName(meetingEntityReference, sessionName);
        if (sessionResults.isEmpty()
                || sessionResults.stream().anyMatch(result ->
                result.getPosition() == null || result.getGapToLeader() == null || result.getNumberOfLaps() == null)) {
            return new SessionSummary(false, null, null, null);
        }

        if (sessionResults.size() == 1) {
            SessionResult fp1Result = sessionResults.getFirst();
            return new SessionSummary(
                    true,
                    fp1Result.getPosition(),
                    fp1Result.getGapToLeader(),
                    (double) fp1Result.getNumberOfLaps()
            );
        }

        double totalPosition = 0;
        double totalGap = 0;
        double totalLaps = 0;

        for (SessionResult sessionResult : sessionResults) {
            totalPosition += sessionResult.getPosition();
            totalGap += sessionResult.getGapToLeader();
            totalLaps += sessionResult.getNumberOfLaps();
        }

        return new SessionSummary(
                true,
                (int) Math.round(totalPosition / sessionResults.size()),
                totalGap / sessionResults.size(),
                totalLaps / sessionResults.size()
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


}
