package org.f1.regression;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class HyperParameters implements Serializable {
    private final int numIterations;
    private final int maxDepth;
    private final double learningRate;
    private final int minInstancesPerNode;
    private final int subsamplingRate;

    public String toString() {
        return String.format("iter=%d, depth=%d, lr=%f, min=%d, sub=%d",
                numIterations, maxDepth, learningRate, minInstancesPerNode, subsamplingRate);
    }

}
