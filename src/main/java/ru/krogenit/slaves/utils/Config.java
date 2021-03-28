package ru.krogenit.slaves.utils;

import lombok.Getter;
import ru.krogenit.slaves.Configurable;

import java.io.File;

@Getter
public class Config implements IConfig {

    public static final Config INSTANCE = new Config();

    static {
        INSTANCE.load();
    }

    @Configurable
    private String token = "";

    @Configurable
    private int maxBuyPrice = 400_000;
    @Configurable
    private int minBuyPrice = 1_000;

    @Override
    public File getConfigFile() {
        return new File("config.json");
    }
}
