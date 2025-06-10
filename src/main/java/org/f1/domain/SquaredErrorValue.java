package org.f1.domain;

public class SquaredErrorValue {

    Double value;
    int count;

    public SquaredErrorValue() {
        value = 0.0;
        count = 0;
    }

    public SquaredErrorValue(Double value) {
        this.value = value * value;
        this.count = 1;
    }

    public SquaredErrorValue increment(Double value) {
        this.value += value * value;
        count++;
        return this;
    }

    public Double getMean() {
        return value / count;
    }

    @Override
    public String toString() {
        return "SquaredErrorValue{" +
                "value=" + value +
                ", count=" + count +
                '}';
    }
}
