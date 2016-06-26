package pepepay.pepepaynative.backend.wallet2;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function2;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.loader.Loader;
import pepepay.pepepaynative.utils.loader.LoaderManager;

public class Wallet {
    private final static Comparator<Transaction> comparator = new Comparator<Transaction>() {
        @Override
        public int compare(Transaction o1, Transaction o2) {
            return (int) (o1.getTime() - o2.getTime());
        }
    };
    private final PublicKey publicKey;
    private final ArrayList<Transaction> receivedTransactions;
    private final ArrayList<Transaction> sendTransactions;
    private final ArrayList<Function2<Void, Wallet, Transaction>> transactionListeners;
    private HashMap<String, Integer> numberTransaction;
    private float balance;

    public Wallet(PublicKey publicKey, ArrayList<Transaction> transactions) {
        this.publicKey = publicKey;
        this.receivedTransactions = new ArrayList<Transaction>();
        this.sendTransactions = new ArrayList<Transaction>();
        this.balance = this.calculateBalance();
        this.numberTransaction = this.calcNumberTransaction();
        this.addTransactions(transactions);
        this.transactionListeners = new ArrayList<Function2<Void, Wallet, Transaction>>();
    }

    private HashMap<String, Integer> calcNumberTransaction() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (Transaction transaction : sendTransactions) {
            result.put(transaction.getReceiver(), result.get(transaction.getReceiver()) + 1);
        }
        for (Transaction transaction : receivedTransactions) {
            result.put(transaction.getSender(), result.get(transaction.getSender()) + 1);
        }

        return result;
    }

    public Transaction getSendTransaction(Wallet receiver, PrivateKey key, float amount, String purpose) {
        return new Transaction(this.getIdentifier(), receiver.getIdentifier(), amount, System.currentTimeMillis(), purpose, numberTransaction.get(receiver.getIdentifier()));
    }

    public void addTransaction(Transaction transaction) {
        System.out.println(numberTransaction);
        //if (!transaction.isValid()) return;
        if (transaction.getReceiver().equals(this.getIdentifier())) {
            String sender = transaction.getSender();
            receivedTransactions.add(transaction);
            balance += transaction.getAmount();
            Integer number = numberTransaction.get(sender);
            if (number == null) {
                number = 0;
            }
            numberTransaction.put(sender, number + 1);
            Wallets.notifyBalanceChange(this.getIdentifier(), balance);
        }
        if (transaction.getSender().equals(this.getIdentifier())) {
            String receiver = transaction.getReceiver();
            sendTransactions.add(transaction);
            balance -= transaction.getAmount();
            Integer number = numberTransaction.get(receiver);
            if (number == null) {
                number = 0;
            }
            numberTransaction.put(receiver, number + 1);
        }
        Wallets.saveWallet(this);
    }

    public void addTransactions(ArrayList<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            this.addTransaction(transaction);
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
        return StringUtils.encode(publicKey.getEncoded());
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

    public ArrayList<Transaction> getTransactionsChronologically() {
        ArrayList<Transaction> result = new ArrayList<Transaction>(sendTransactions.size() + receivedTransactions.size());
        result.addAll(sendTransactions);
        result.addAll(receivedTransactions);
        Collections.sort(result, comparator);
        return result;
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

    public void addTransactionListener(Function2<Void, Wallet, Transaction> listener) {
        transactionListeners.add(listener);
    }

    public void removeTransactionsListener(Function2<Void, Wallet, Transaction> listener) {
        transactionListeners.remove(listener);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Wallet && ((Wallet) obj).getPublicKey().equals(this.getPublicKey());
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
