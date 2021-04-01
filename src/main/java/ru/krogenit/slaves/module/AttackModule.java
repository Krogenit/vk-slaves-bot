package ru.krogenit.slaves.module;

import lombok.Getter;
import ru.krogenit.slaves.RequestHandler;
import ru.krogenit.slaves.config.AttackConfig;
import ru.krogenit.slaves.objects.Slave;

import java.util.List;

@Getter
public class AttackModule extends Module {

    private long[] targets;

    public AttackModule() {
        AttackConfig config = new AttackConfig();
        config.load();
        config.save();
        running = config.isEnabled();
        if(running) {
            requestHandler = new RequestHandler(config.getMinSleepTime(), config.getMaxSleepTime(), config.getToken());
            minPrice = config.getMinBuyPrice();
            maxBuyPrice = config.getMaxBuyPrice();
            jobName = config.getJobName();
            targets = config.getTargets();
            waitBeforeNextRun = config.getWaitBeforeNextRunInMilliseconds();
            me = requestHandler.get();
        }
    }

    public void start() {
        thread = new Thread(() -> {
            while (running) {
                System.out.println("[ATTACK MODULE] Start attacking...");

                for (int i = 0; i < targets.length; i++) {
                    long target = targets[i];
                    attackTarget(target);
                }

                try {
                    System.out.println("[ATTACK MODULE] Waiting before next attack...");
                    Thread.sleep(waitBeforeNextRun);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            requestHandler.clear();
        });
        thread.start();
    }

    private void attackTarget(long id) {
        System.out.println("[ATTACK MODULE] Attacking " + id);
        Slave target = requestHandler.get(id);
        if(target.getSlavesCount() > 0) {
            System.out.println("[ATTACK MODULE] Target has " + target.getSlavesCount() + " slaves");
            List<Slave> slaves = requestHandler.getSlaves(target.getId());
            for (int i = 0; i < slaves.size(); i++) {
                Slave slave = slaves.get(i);
                System.out.println("[ATTACK MODULE] Buying " + slave.getId() + "...");
                if (requestHandler.buySlave(slave)) {
                    System.out.println("[ATTACK MODULE] Success");
                    System.out.println("[ATTACK MODULE] Setting job...");
                    if (requestHandler.setJob(slave.getId(), "р")) {
                        System.out.println("[ATTACK MODULE] Success");
                    }
                }
            }
        } else {
            System.out.println("[ATTACK MODULE] Target hasn't slaves");
        }
    }
}
