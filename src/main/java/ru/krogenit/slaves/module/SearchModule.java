package ru.krogenit.slaves.module;

import lombok.Getter;
import ru.krogenit.slaves.config.SearchConfig;
import ru.krogenit.slaves.result.BuySlaveResult;
import ru.krogenit.slaves.RequestHandler;
import ru.krogenit.slaves.objects.Slave;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Getter
public class SearchModule extends Module {

    private long[] friends;
    private long[] exclusions;

    private final Map<Long, Slave> processedUsers = new HashMap<>(0);
    private final Random rand = new Random();
    private boolean checkMySlaves;
    private boolean checkFriends;
    private boolean checkTops;
    private boolean checkRandomUsers;

    public SearchModule() {
        SearchConfig config = new SearchConfig();
        config.load();
        config.save();
        running = config.isEnabled();
        if(running) {
            requestHandler = new RequestHandler(config.getMinSleepTime(), config.getMaxSleepTime(), config.getToken());
            checkMySlaves = config.isCheckMySlaves();
            checkFriends = config.isCheckFriends();
            checkTops = config.isCheckTops();
            checkRandomUsers = config.isCheckRandomUsers();
            waitBeforeNextRun = config.getWaitBeforeNextRunInMilliseconds();
            minPrice = config.getMinBuyPrice();
            maxBuyPrice = config.getMaxBuyPrice();
            jobName = config.getJobName();
            exclusions = config.getExclusions();
            friends = config.getFriends();
            me = requestHandler.get();
        }
    }

    @Override
    public void start() {
        thread = new Thread(() -> {
            try {
                while (running) {
                    System.out.println("[SEARCH MODULE] Started search");

                    if(checkMySlaves) {
                        System.out.println("[SEARCH MODULE] Checking my slaves...");
                        List<Slave> slaves = me.getSlaves();
                        for (int i = 0; i < slaves.size(); i++) {
                            Slave slave = slaves.get(i);
                            checkUser(slave);
                            System.out.println("[SEARCH MODULE] " + (i / (float) slaves.size() * 100) + "% completed checking my slaves");
                        }
                    }

                    if(checkFriends) {
                        System.out.println("[SEARCH MODULE] Checking friends...");
                        List<Slave> friends = requestHandler.getFriends(this.friends);
                        for (int i = 0; i < friends.size(); i++) {
                            Slave slave = friends.get(i);
                            checkUser(slave);
                            System.out.println("[SEARCH MODULE] " + (i / (float) friends.size() * 100) + "% completed checking friends");
                        }
                    }

                    if(checkTops) {
                        System.out.println("[SEARCH MODULE] Checking tops...");
                        List<Slave> topUsers = requestHandler.getTop();
                        for (int i = 0; i < topUsers.size(); i++) {
                            Slave topUser = topUsers.get(i);
                            checkUser(topUser);
                            System.out.println("[SEARCH MODULE] " + (i / (float) topUsers.size() * 100) + "% completed checking tops");
                        }
                    }

                    if(checkRandomUsers) {
                        System.out.println("[SEARCH MODULE] Checking random users...");
                        int count = 1000;
                        for (int i = 0; i < count; i++) {
                            long randomId = rand.nextInt(650_000_000) + 1;
                            System.out.println("[SEARCH MODULE] Checking " + randomId + "...");
                            Slave slave = requestHandler.get(randomId);
                            if (slave != null) {
                                checkUser(slave);
                            } else {
                                System.out.println("[SEARCH MODULE] Random user not found");
                            }
                            System.out.println("[SEARCH MODULE] " + (i / (float) count * 100) + "% completed checking random users");
                        }
                    }

                    processedUsers.clear();

                    try {
                        System.out.println("[SEARCH MODULE] Waiting before next search...");
                        Thread.sleep(waitBeforeNextRun);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            requestHandler.clear();
        });
        thread.start();
    }

    private void checkUser(Slave user) {
        System.out.println("[SEARCH MODULE] Checking user " + user.getId());
        long ownerId = user.getOwnerId();
        if(ownerId > 0) {
            if(ownerId != me.getId()) {
                System.out.println("[SEARCH MODULE] Has owner");
                if (!processedUsers.containsKey(ownerId)) {
                    System.out.println("[SEARCH MODULE] Getting info...");
                    Slave owner = requestHandler.get(ownerId);
                    processedUsers.put(ownerId, owner);
                    if (owner != null) {
                        checkUser(owner);
                    }
                } else {
                    System.out.println("[SEARCH MODULE] Already processed");
                }
            } else {
                System.out.println("[SEARCH MODULE] Owner of this slave is you");
            }
        }

        checkSlaves(user);
    }

    private void checkSlaves(Slave user) {
        System.out.println("[SEARCH MODULE] Check " + user.getId() + " for slaves");
        if (user.getSlavesCount() > 0) {
            System.out.println("[SEARCH MODULE] Getting slaves...");
            List<Slave> slaves = requestHandler.getSlaves(user.getId());
            user.setSlaves(slaves);
            if(slaves != null) {
                for (int i1 = 0; i1 < slaves.size(); i1++) {
                    Slave slave = slaves.get(i1);
                    checkUser(slave);
                    if(!hasExclusion(user.getId())) {
                        System.out.println("[SEARCH MODULE] Trying buy " + slave.getId() + "...");
                        BuySlaveResult buySlaveResult = buySlave(slave);
                        if (buySlaveResult.isSuccess()) {
                            System.out.println("[SEARCH MODULE] Success");
                            slaves.remove(i1--);
                        } else if(buySlaveResult == BuySlaveResult.NOT_ENOUGH_MONEY) {
                            updateBalance();
                            if (buySlave(slave).isSuccess()) {
                                slaves.remove(i1--);
                            }
                        }
                    } else {
                        System.out.println("[SEARCH MODULE] Skip buying from " + user.getId());
                    }
                }
            }
        } else {
            System.out.println("[SEARCH MODULE] No slaves found");
        }
    }

    private boolean hasExclusion(long id) {
        for (int i = 0; i < exclusions.length; i++) {
            long exclusion = exclusions[i];
            if(exclusion == id) return true;
        }

        return false;
    }
}
