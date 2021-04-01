package ru.krogenit.slaves.config;

import lombok.Getter;
import ru.krogenit.slaves.Configurable;

@Configurable
@Getter
public abstract class ModuleConfig implements IConfig {
    private String token;
    private boolean enabled;
    private int minSleepTime;
    private int maxSleepTime;
    private int maxBuyPrice = 400_000;
    private int minBuyPrice = 1_000;
    private String jobName;
    private long waitBeforeNextRunInMilliseconds;
}
