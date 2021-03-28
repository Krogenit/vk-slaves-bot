package ru.krogenit.slaves;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Slave {
    private final long id;
    private Job job;
    private long ownerId;
    private int fetterPrice;
    private int salePrice;
    private int price;
    private List<Slave> slaves;
    private int balance;
    private int profitPerMin;
    private int slavesCount;
    private long fetterTo;

    public void consumeBalance(int value) {
        balance -= value;
    }
}
