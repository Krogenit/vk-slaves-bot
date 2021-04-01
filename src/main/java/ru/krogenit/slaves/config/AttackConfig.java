package ru.krogenit.slaves.config;

import lombok.Getter;
import ru.krogenit.slaves.Configurable;

import java.io.File;

@Configurable
@Getter
public class AttackConfig extends ModuleConfig {

    private long[] targets = new long[] {};

    @Override
    public File getConfigFile() {
        return new File("attack_config.json");
    }
}
