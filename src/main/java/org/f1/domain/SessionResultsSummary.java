package org.f1.domain;

import org.f1.domain.openf1.SessionResult;

import java.util.List;


public record SessionResultsSummary(List<SessionResult> qualiSessionResults, List<SessionResult> practiseSessionResults) {
}
