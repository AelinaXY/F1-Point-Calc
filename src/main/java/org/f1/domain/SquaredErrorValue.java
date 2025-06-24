package org.f1.domain;

public class SquaredErrorValue {

    Double value;
    int count;

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

    public Double getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "SquaredErrorValue{" +
                "value=" + value +
                ", count=" + count +
                '}';
    }
}
