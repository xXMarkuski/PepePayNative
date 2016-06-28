package pepepay.pepepaynative.backend.wallet2.transaction;


import java.io.Serializable;

import pepepay.pepepaynative.backend.wallet2.Wallets;

public class Transaction implements Serializable {

    public static final long serialVersionUID = 1L;

    private final String sender;
    private final String receiver;
    private final float amount;
    private final long time;
    private final String purpose;
    //unique for reviver and sender combination
    private final int id;
    //WalletID of confirmer, time encrypted with private key of confirmer and public key of sender. only needed
    //private final ObjectMap<String, String> confirmations;

    public Transaction(String sender, String receiver, float amount, long time, String purpose, int id) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.time = time;
        this.purpose = purpose;
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getPurpose() {
        return purpose;
    }

    public long getTime() {
        return time;
    }

    public boolean isValid() {
        if (sender.equals(receiver)) return false;
        if (Wallets.isGodWallet(sender)) return true;
        return Wallets.getWallet(sender).calculateBalanceAt(time) >= amount;

    }

    public int getID() {
        return id;
    }
}
