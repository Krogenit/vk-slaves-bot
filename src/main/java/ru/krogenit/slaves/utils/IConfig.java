package ru.krogenit.slaves.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Primitives;
import ru.krogenit.slaves.Configurable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public interface IConfig {

    Gson GSON_BUILDER = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().getAnnotation(Configurable.class) == null && f.getAnnotation(Configurable.class) == null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).setPrettyPrinting()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    default void load() {
        try {
            loadExceptionally();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    default void loadExceptionally() throws NoSuchFieldException, IllegalAccessException, IOException {
        if(getConfigFile().exists()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(getConfigFile()), StandardCharsets.UTF_8));
            StringBuilder source = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                source.append(line).append("\n");
            }
            bufferedReader.close();
            IConfig configGson;
            try {
                configGson = GSON_BUILDER.fromJson(source.toString(), this.getClass());
            } catch (Exception t) {
                throw new IOException("An error has occurred during loading of config: " + getConfigFile().getName(), t);
            }

            List<Class<?>> list = Utils.getClassesHierarchy(this.getClass());
            if(configGson != null) {
                for (Class<?> aClass : list) {
                    for (Field field : aClass.getDeclaredFields()) {
                        if (!Modifier.isTransient(field.getModifiers()) && (aClass.getAnnotation(Configurable.class) != null || field.getAnnotation(Configurable.class) != null)) {
                            Field field1 = aClass.getDeclaredField(field.getName());
                            field.setAccessible(true);
                            field1.setAccessible(true);
                            Object value = field1.get(configGson);
                            if(value != null || setNulls()) {
                                field.set(this, value);
                            }
                        }
                    }
                }
            }
        } else {
            save();
            load();
        }
    }

    default void save() {
        if(getConfigFile().exists()) {
            if (!getConfigFile().delete()) {
                throw new RuntimeException(getConfigFile().getAbsolutePath());
            }
        }
        if(getConfigFile().getParentFile() != null) {
            //noinspection ResultOfMethodCallIgnored
            getConfigFile().getParentFile().mkdirs();
        }
        String json = GSON_BUILDER.toJson(this);
        Set<Class<?>> list = new HashSet<>();
        list.add(getClass());
        int preSize = 0;
        while(preSize != list.size()) {
            preSize = list.size();
            List<Class<?>> temp = new ArrayList<>();
            for(Class<?> aClass : list) {
                for (Field declaredField : aClass.getDeclaredFields()) {
                    if (declaredField.getType().isAssignableFrom(List.class) || declaredField.getType().isAssignableFrom(Map.class)) {
                        if(declaredField.getGenericType() instanceof ParameterizedType) {
                            Type[] actualTypeArguments = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments();
                            Utils.forEach(actualTypeArguments, type -> {
                                if(type instanceof Class) {
                                    temp.add((Class<?>) type);
                                }
                                return true;
                            });
                        }
                    } else {
                        temp.add(declaredField.getType());
                        temp.addAll(Utils.getClassesHierarchy(declaredField.getType()));
                    }
                }
            }
            list.addAll(temp);
            list.removeIf(aClass -> aClass == File.class || aClass == Class.class || aClass == Object.class || aClass == String.class || Primitives.isPrimitive(aClass) || Map.class.isAssignableFrom(aClass) || List.class.isAssignableFrom(aClass));
        }
        String[] split = json.split("\n");
        try {
            FileWriter fileWriter = new FileWriter(getConfigFile());
            for (String s : split) {
                fileWriter.write(s);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    File getConfigFile();

    default boolean setNulls() {
        return true;
    }
}
