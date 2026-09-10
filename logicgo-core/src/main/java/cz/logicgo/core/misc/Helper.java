package cz.logicgo.core.misc;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Helper {

    public static <T, K, V> HashMap<K, V> mapBy(List<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return list.stream().collect(Collectors.toMap(
                keyMapper,
                valueMapper,
                (a, b) -> b,
                HashMap::new
        ));
    }

    public static <T, K> HashMap<K, T> mapBy(List<T> list, Function<T, K> keyMapper) {
        return list.stream().collect(Collectors.toMap(
                keyMapper,
                Function.identity(),
                (a, b) -> b,
                HashMap::new
        ));
    }

}
