package ru.krogenit.slaves;

import lombok.Getter;

@Getter
public class Me extends Slave {

    public Me(Slave slave) {
        super(slave.getId(), slave.getJob(), slave.getOwnerId(), slave.getFetterPrice(), slave.getSalePrice(), slave.getPrice(), slave.getSlaves(), slave.getBalance(), slave.getProfitPerMin(), slave.getSlavesCount(), slave.getFetterTo());
    }
}
