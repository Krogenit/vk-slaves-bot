package ru.krogenit.slaves.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.krogenit.slaves.Configurable;
import ru.krogenit.slaves.utils.Utils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        File configFile = getConfigFile();
        if(configFile.exists()) {
            if (!configFile.delete()) {
                throw new RuntimeException(configFile.getAbsolutePath());
            }
        }
        if(configFile.getParentFile() != null) {
            configFile.getParentFile().mkdirs();
        }
        String json = GSON_BUILDER.toJson(this);

        try {
            FileWriter fileWriter = new FileWriter(configFile);
            fileWriter.write(json);
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
