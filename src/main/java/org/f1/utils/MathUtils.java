package org.f1.utils;

import java.util.List;

public class MathUtils {

    public static double stdev(List<Double> list) {
        double sum = 0.0;
        double standard_deviation = 0.0;
        int array_length = list.size();

        if (array_length == 1 && list.getFirst().equals(0d)) {
            return 0;
        }
        for (double temp : list) {
            sum += temp;
        }
        double mean = sum / array_length;
        for (double temp : list) {
            standard_deviation += Math.pow(temp - mean, 2);
        }
        return Math.sqrt(standard_deviation / array_length);
    }
}
