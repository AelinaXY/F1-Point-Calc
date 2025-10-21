package org.f1.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {

    public static <T> Predicate<T> distinctByDualKey(Function<? super T, ?> keyExtractor, Function<? super T, ?> secondaryKeyExtractor) {
        Set<ComparisionObject> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(new ComparisionObject(keyExtractor.apply(t), secondaryKeyExtractor.apply(t)));
    }

    private record ComparisionObject(Object first, Object second) {
    }

}
