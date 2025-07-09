package org.f1.calculations;

import org.f1.domain.FullPointEntity;

public interface ScoreCalculatorInterface {

    Double calculateScore(FullPointEntity fullPointEntity, String race, boolean isSprint);
}
