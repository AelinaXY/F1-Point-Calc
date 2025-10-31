package org.f1.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {

    public static <T> Predicate<T> distinctByDualKey(Function<? super T, ?> keyExtractor, Function<? super T, ?> secondaryKeyExtractor) {
        Set<ComparisonObject> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(new ComparisonObject(keyExtractor.apply(t), secondaryKeyExtractor.apply(t)));
    }

    private record ComparisonObject(Object first, Object second) {
    }

}
