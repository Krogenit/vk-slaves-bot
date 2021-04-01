package ru.krogenit.slaves;

import ru.krogenit.slaves.module.AttackModule;
import ru.krogenit.slaves.module.Module;
import ru.krogenit.slaves.module.SearchModule;

import java.util.ArrayList;
import java.util.List;

public class SlavesClient {

    private final List<Module> modules = new ArrayList<>();

    public SlavesClient() {
        modules.add(new SearchModule());
        modules.add(new AttackModule());
    }

    public void start() {
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if(module.isRunning()) module.start();
        }

        for (int i = 0; i < modules.size(); i++) {
            try {
                modules.get(i).getThread().join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
