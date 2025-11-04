package org.f1.regression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HyperParameters implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numIterations;
    private int maxDepth;
    private double learningRate;
    private int minInstancesPerNode;
    private double subsamplingRate;

    public String toString() {
        return String.format("iter=%d, depth=%d, lr=%f, min=%d, sub=%f",
                numIterations, maxDepth, learningRate, minInstancesPerNode, subsamplingRate);
    }

}
