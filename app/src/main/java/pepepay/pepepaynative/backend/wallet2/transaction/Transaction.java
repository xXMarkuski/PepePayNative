package pepepay.pepepaynative.backend.wallet2.transaction;


import java.io.Serializable;

import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.loader.Loader;

public class Transaction implements Serializable {
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
        if (Wallets.isGodWallet(sender)) return true;
        if (Wallets.isGodWallet(receiver)) return false;
        return Wallets.getWallet(sender).calculateBalanceAt(time) >= amount;

    }

    public int getID() {
        return id;
    }

    public static class TransactionLoader implements Loader<Transaction> {

        @Override
        public String save(Transaction transaction) {
            return StringUtils.multiplex(transaction.sender, transaction.receiver, transaction.amount + "", transaction.time + "", transaction.purpose, transaction.id + "");
        }

        @Override
        public Transaction load(String data) {
            String[] parts = StringUtils.demultiplex(data);
            String sender = parts[0];
            String receiver = parts[1];
            float amount = Float.parseFloat(parts[2]);
            long time = Long.parseLong(parts[3]);
            String purpose = parts[4];
            int id = Integer.parseInt(parts[5]);
            return new Transaction(sender, receiver, amount, time, purpose, id);
        }

        @Override
        public Transaction unsaveLoad(String data) throws Exception {
            return load(data);
        }

        @Override
        public Class<Transaction> getHandledType() {
            return Transaction.class;
        }

        @Override
        public String id() {
            return "t";
        }
    }
}
