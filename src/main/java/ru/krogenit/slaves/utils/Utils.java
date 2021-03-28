package ru.krogenit.slaves.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Utils {

    public static <T> boolean forEach(T[] array, Function<T, Boolean> function) {
        for (T t : array) {
            if (!function.apply(t)) {
                return false;
            }
        }
        return true;
    }

    public static List<Class<?>> getClassesHierarchy(Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();
        Class<?> c = clazz;
        while(c != null) {
            list.add(c);
            c = c.getSuperclass();
        }
        return list;
    }
}
