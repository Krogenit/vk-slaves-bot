package ru.krogenit.slaves.config;

import lombok.Getter;
import ru.krogenit.slaves.Configurable;

import java.io.File;

@Configurable
@Getter
public class SearchConfig extends ModuleConfig {

    private boolean checkMySlaves;
    private boolean checkFriends;
    private boolean checkTops;
    private boolean checkRandomUsers;
    private int randomUsersCount;

    private long[] exclusions = new long[] {};

    private long[] friends = new long[] {};

    @Override
    public File getConfigFile() {
        return new File("search_config.json");
    }
}
