package ru.krogenit.slaves.result;

public enum BuySlaveResult {
    SUCCESS, UNKNOWN_ERROR, NOT_ENOUGH_MONEY, GROUP, FETTER, BAD_PRICE;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
