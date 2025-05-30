package org.f1.calculations;

import org.f1.domain.FullPointEntity;

import java.util.Set;

public class RegressionDataCalculations extends AbstractCalculation {


    public RegressionDataCalculations(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit) {
        super(driverSet, teamSet, costCap, transferLimit);
    }


    public void calculate(String race){
    }

    //TODO:
    //Section 0: Update FullPointEntity to have a score variable created from average and 4d1 average weighted

    //Method 1: Take in a FullPointEntity and a race name and output the error value

    //Method 2: Take in a Set<FullPointEntity> and calculate the mean square error for each race prediction using the scoring algo
    //Output: Set of mean square errors

    //detect outlayers?


    //Method 3: Iterate on method 2 to find the lowest error value for weight combinations for scoring algorithm
    //Figure out how to optimally calculate the weight combination
    //Linear optimization


}
