package com.flowapp.TextToMathML.Models;

public class Tuple2<X,Y>  {
    private final X first;
    private final Y second;

    public Tuple2(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public static <X,Y> Tuple2<X,Y> of (X first, Y second) {
        return new Tuple2<X,Y>(first, second);
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Tuple2{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
