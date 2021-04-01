package ru.krogenit.slaves.module;

import lombok.Getter;
import lombok.Setter;
import ru.krogenit.slaves.*;
import ru.krogenit.slaves.objects.Job;
import ru.krogenit.slaves.objects.Me;
import ru.krogenit.slaves.objects.Slave;
import ru.krogenit.slaves.result.BuySlaveResult;

@Getter
@Setter
public abstract class Module {

    protected RequestHandler requestHandler;
    protected Me me;
    protected int minPrice;
    protected int maxBuyPrice;
    protected String jobName;
    protected long waitBeforeNextRun;
    protected Thread thread;
    protected boolean running;

    public abstract void start();

    protected void updateBalance() {
        me = requestHandler.get();
    }

    protected BuySlaveResult buySlave(Slave slave) {
        if (slave.getId() < 0) return BuySlaveResult.GROUP;
        if (slave.getFetterTo() != 0) return BuySlaveResult.FETTER;
        if (slave.getPrice() < minPrice || slave.getPrice() > maxBuyPrice) return BuySlaveResult.BAD_PRICE;
        if (me.getBalance() < slave.getPrice()) return BuySlaveResult.NOT_ENOUGH_MONEY;

        if (requestHandler.buySlave(slave)) {
            me.consumeBalance(slave.getPrice());
            me.getSlaves().add(slave);
            System.out.println("[BASE MODULE] Setting job...");
            if (requestHandler.setJob(slave.getId(), jobName)) {
                slave.setJob(new Job(jobName));
            }

            return BuySlaveResult.SUCCESS;
        }

        return BuySlaveResult.UNKNOWN_ERROR;
    }
}
