package ru.krogenit.slaves;

import ru.krogenit.slaves.utils.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlavesClient {

    public static SlavesClient INSTANCE;

    private final int maxBuyPrice = Config.INSTANCE.getMaxBuyPrice();
    private final int minPrice = Config.INSTANCE.getMinBuyPrice();
    private boolean running;

    private Me me;

    public SlavesClient() {
        INSTANCE = this;
    }

    public void start() {
        running = true;

        while(running) {
            me = RequestHandler.INSTANCE.get();
            List<Slave> friends = RequestHandler.INSTANCE.getFriends();

            for (int i = 0; i < friends.size(); i++) {
                Slave slave = friends.get(i);
                checkUser(slave);
            }

            List<Slave> topUsers = getTop();

            for (int i = 0; i < topUsers.size(); i++) {
                Slave topUser = topUsers.get(i);
                checkUser(topUser);
            }

            processedUsers.clear();

            try {
                System.out.println("Waiting before next");
                Thread.sleep(30000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RequestHandler.INSTANCE.clear();
    }

    private Map<Long, Slave> processedUsers = new HashMap<>(0);

    private void checkUser(Slave user) {
        System.out.println("Checking user " + user.getId());
        long ownerId = user.getOwnerId();
        if(ownerId > 0) {
            System.out.println("Has owner");
            if(!processedUsers.containsKey(ownerId)) {
                System.out.println("Getting info...");
                Slave owner = RequestHandler.INSTANCE.get(ownerId);
                processedUsers.put(ownerId, owner);
                if(owner != null) {
                    checkUser(owner);
                }
            } else {
                System.out.println("Already processed");
            }
        }

        checkSlaves(user);
    }

    private void checkSlaves(Slave user) {
        System.out.println("Check slaves " + user.getId());
        if (user.getSlavesCount() > 0) {
            System.out.println("Getting slaves...");
            List<Slave> slaves = getSlaves(user.getId());
            user.setSlaves(slaves);
            if(slaves != null) {
                for (int i1 = 0; i1 < slaves.size(); i1++) {
                    Slave slave = slaves.get(i1);
                    checkUser(slave);
                    System.out.println("Trying buy " + slave.getId() + "...");
                    if(tryBuySlave(slave)) {
                        slaves.remove(i1--);
                    }
                }
            }
        } else {
            System.out.println("No slaves found");
        }
    }

    private boolean tryBuySlave(Slave slave) {
        if (slave.getId() < 0) return false;
        if (slave.getFetterTo() != 0) return false;
        if (slave.getPrice() < minPrice) return false;
        if (slave.getPrice() > maxBuyPrice) return false;
        if (me.getBalance() < slave.getPrice()) return false;

        if (buySlave(slave)) {
            me.consumeBalance(slave.getPrice());
            me.getSlaves().add(slave);
            if (setJob(slave.getId(), "п")) {
                slave.setJob(new Job("п"));
            }

            return true;
        }

        return false;
    }

    public List<Slave> getSlaves(long id) {
        return RequestHandler.INSTANCE.getSlaves(id);
    }

    public boolean buySlave(Slave slave) {
        return RequestHandler.INSTANCE.buySlave(slave);
    }

    public List<Slave> getTop() {
        return RequestHandler.INSTANCE.getTop();
    }

    public boolean setJob(long id, String name) {
        return RequestHandler.INSTANCE.setJob(id, name);
    }
}
