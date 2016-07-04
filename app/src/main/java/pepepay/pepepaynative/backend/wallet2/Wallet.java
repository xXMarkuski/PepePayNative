package pepepay.pepepaynative.backend.wallet2;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function2;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.encryption.EncryptionUtils;
import pepepay.pepepaynative.utils.loader.Loader;
import pepepay.pepepaynative.utils.loader.LoaderManager;

public class Wallet {
    private final PublicKey publicKey;
    private final ArrayList<Transaction> receivedTransactions;
    private final ArrayList<Transaction> sendTransactions;
    private final ArrayList<Function2<Void, Wallet, Transaction>> transactionListeners;
    private final String identifier;
    private float balance;
    private boolean transactionChanged = true;

    private ArrayList<Transaction> scheduledTransactions;
    private ArrayList<Transaction> transactionsChron;

    public Wallet(PublicKey publicKey, ArrayList<Transaction> transactions) {
        this.publicKey = publicKey;
        this.receivedTransactions = new ArrayList<Transaction>();
        this.sendTransactions = new ArrayList<Transaction>();
        this.transactionListeners = new ArrayList<Function2<Void, Wallet, Transaction>>();
        this.scheduledTransactions = transactions;
        this.balance = 0;
        this.identifier = StringUtils.encode(publicKey.getEncoded());
    }

    public Transaction getSendTransaction(Wallet receiver, PrivateKey key, float amount, String purpose) {
        long time = System.currentTimeMillis();
        return new Transaction(this.getIdentifier(), receiver.getIdentifier(), amount, time, StringUtils.multiplex(purpose, EncryptionUtils.complexBase64RsaEncrypt(key, time + receiver.getIdentifier())));
    }

    public void addTransaction(final Transaction transaction) {
        if (!transaction.isValid()) return;
        if (this.getTransactionAt(transaction.getTime()) != null) {
            return;
        }
        System.out.println("has not sended a transaction at the same time");

        if (transaction.getReceiver().equals(this.getIdentifier())) {
            receivedTransactions.add(transaction);
            balance += transaction.getAmount();
        }
        if (transaction.getSender().equals(this.getIdentifier())) {
            sendTransactions.add(transaction);
            balance -= transaction.getAmount();
        }
        Wallets.notifyBalanceChange(getIdentifier(), transaction);
        if (!transactionListeners.isEmpty()) {
            for (Function2<Void, Wallet, Transaction> listener : transactionListeners) {
                listener.eval(Wallet.this, transaction);
            }
        }
        transactionChanged = true;
        Wallets.saveWallet(this);
    }

    public void addTransactions(final ArrayList<Transaction> transactions) {
        for (final Transaction transaction : transactions) {
            Wallet.this.addTransaction(transaction);
        }
    }

    public float calculateBalance() {
        float balance = 0;
        for (Transaction transaction : receivedTransactions) {
            balance += transaction.getAmount();
        }
        for (Transaction transaction : sendTransactions) {
            balance -= transaction.getAmount();
        }
        return balance;
    }

    public float calculateBalanceAt(long time) {
        float balance = 0;
        for (Transaction transaction : getTransactionsBeforeOrAt(receivedTransactions, time)) {
            balance += transaction.getAmount();
        }
        for (Transaction transaction : getTransactionsBeforeOrAt(sendTransactions, time)) {
            balance -= transaction.getAmount();
        }
        return balance;
    }

    public float calculateBalanceBefor(long time) {
        float balance = 0;
        for (Transaction transaction : getTransactionsBefore(receivedTransactions, time)) {
            balance += transaction.getAmount();
        }
        for (Transaction transaction : getTransactionsBefore(sendTransactions, time)) {
            balance -= transaction.getAmount();
        }
        return balance;
    }

    public Transaction getTransactionAt(long time) {
        for (Transaction transaction : getTranactions()) {
            if (transaction.getTime() == time) return transaction;
        }
        return null;
    }

    public int getReceivedTransactionCount() {
        return receivedTransactions.size();
    }

    public int getSendTransactionCount() {
        return sendTransactions.size();
    }

    public int getTransactionCount() {
        return getReceivedTransactionCount() + getSendTransactionCount();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ArrayList<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }

    public ArrayList<Transaction> getSendTransactions() {
        return sendTransactions;
    }

    public ArrayList<Transaction> getTransactionsBeforeOrAt(ArrayList<Transaction> array, long time) {
        ArrayList<Transaction> result = new ArrayList<Transaction>();
        for (Transaction transaction : array) {
            if (transaction.getTime() <= time) result.add(transaction);
        }
        return result;
    }

    public ArrayList<Transaction> getTransactionsBefore(long time) {
        return getTransactionsBefore(getTransactionsChronologically(), time);
    }

    public ArrayList<Transaction> getTransactionsBeforeOrAt(long time) {
        return getTransactionsBeforeOrAt(getTransactionsChronologically(), time);
    }

    public ArrayList<Transaction> getTransactionsChronologically() {
        if (transactionChanged) {
            transactionsChron = new ArrayList<Transaction>(sendTransactions.size() + receivedTransactions.size());
            transactionsChron.addAll(sendTransactions);
            transactionsChron.addAll(receivedTransactions);

            Collections.sort(transactionsChron, Transaction.comparator);
            return transactionsChron;
        }
        return transactionsChron;
    }

    public ArrayList<Transaction> getTransactionsBefore(ArrayList<Transaction> array, long time) {
        ArrayList<Transaction> result = new ArrayList<Transaction>();
        for (Transaction transaction : array) {
            if (transaction.getTime() < time) result.add(transaction);
        }
        return result;
    }

    public float getBalance() {
        return balance;
    }

    public void addTransactionListener(Function2<Void, Wallet, Transaction> listener, boolean shoudNotifyOld) {
        transactionListeners.add(listener);
        if (shoudNotifyOld) {
            for (Transaction transaction : getTransactionsChronologically()) {
                listener.eval(this, transaction);
            }
        }
    }

    public ArrayList<Transaction> getScheduledTransactions() {
        return scheduledTransactions;
    }

    public void removeTransactionsListener(Function2<Void, Wallet, Transaction> listener) {
        transactionListeners.remove(listener);
    }

    public void addScheduledTransactions() {
        addTransactions(scheduledTransactions);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Wallet && ((Wallet) obj).getPublicKey().equals(this.getPublicKey());
    }

    public ArrayList<Transaction> getTransactionsAfter(ArrayList<Transaction> array, long time) {
        ArrayList<Transaction> result = new ArrayList<Transaction>();
        for (Transaction transaction : array) {
            if (transaction.getTime() > time) result.add(transaction);
        }
        return result;
    }

    public ArrayList<Transaction> getTransactionsAfter(long time) {
        return getTransactionsAfter(this.getTranactions(), time);
    }

    public ArrayList<Transaction> getTranactions() {
        ArrayList<Transaction> result = new ArrayList<>(receivedTransactions.size() + sendTransactions.size());
        result.addAll(receivedTransactions);
        result.addAll(sendTransactions);
        return result;
    }

    public Transaction getLastTransaction() {
        ArrayList<Transaction> tr = getTransactionsChronologically();
        return tr.get(tr.size() - 1);
    }

    public ArrayList<Transaction> getTransactionsAfterBefore(long after, long befor) {
        ArrayList<Transaction> result = new ArrayList<Transaction>();
        for (Transaction transaction : getTranactions()) {
            if (transaction.getTime() > after && transaction.getTime() < befor)
                result.add(transaction);
        }
        return result;
    }

    public static class WalletLoader implements Loader<Wallet> {

        public WalletLoader() {
        }

        @Override
        public String save(Wallet wallet) {
            LoaderManager manager = PepePay.LOADER_MANAGER;
            String key = manager.save(wallet.getPublicKey());
            String transactions = manager.save(wallet.getTransactionsChronologically());

            return StringUtils.multiplex(key, transactions);
        }

        @Override
        public Wallet load(String data) {
            LoaderManager manager = PepePay.LOADER_MANAGER;

            String[] parts = StringUtils.demultiplex(data);

            PublicKey key = (PublicKey) manager.load(parts[0]);
            ArrayList transactions = (ArrayList) manager.load(parts[1]);

            return new Wallet(key, transactions);
        }

        @Override
        public Wallet unsaveLoad(String data) throws Exception {
            return load(data);
        }

        @Override
        public Class<Wallet> getHandledType() {
            return Wallet.class;
        }

        @Override
        public String id() {
            return "w";
        }
    }
}
