package pepepay.pepepaynative.backend.wallet2.transaction;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.encryption.EncryptionUtils;

public class Transaction implements Serializable {
    public final transient static Comparator<Transaction> comparator = new Comparator<Transaction>() {
        @Override
        public int compare(Transaction o1, Transaction o2) {
            return (int) (o1.getTime() - o2.getTime());
        }
    };


    public static final long serialVersionUID = 2L;

    private final String sender;
    private final String receiver;
    private final float amount;
    private final long time;
    private final String purpose;
    //WalletID of confirmer, time encrypted with private key of confirmer and public key of sender. only needed
    //private final ObjectMap<String, String> confirmations;

    private transient boolean verified = false;

    public Transaction(String sender, String receiver, float amount, long time, String purpose) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.time = time;
        this.purpose = purpose;
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
        if (verified) return true;
        Wallet wallet = Wallets.getWallet(sender);

        if (sender.equals(receiver)) {
            PepePay.ERROL.errol("sender and rec. are the same");
            return false;
        }

        boolean correct = false;
        while (!correct) {
            try {
                String[] demul = StringUtils.demultiplex(this.getPurpose());
                System.out.println(demul[1]);
                System.out.println(demul[0]);
                if (!EncryptionUtils.complexBase64RsaDecrypt(wallet.getPublicKey(), demul[1]).equals(time + receiver)) {
                    PepePay.ERROL.errol("not valid sender");
                    return false;
                } else {
                    correct = true;
                }

            } catch (Throwable t) {
                PepePay.ERROL.errol("error verifying sender" + t.getMessage());
            }
        }

        if (Wallets.isGodWallet(sender)) {
            //PepePay.ERROL.errol("god wallet");
            return true;
        }
        ArrayList<Transaction> transactions = wallet.getTransactionsBefore(wallet.getReceivedTransactions(), time);
        for (Transaction transaction : transactions) {
            if (!transaction.isValid()) {
                PepePay.ERROL.errol("not all prev trans are valid");
                return false;
            }
        }
        boolean b = wallet.calculateBalanceBefor(time) >= amount;
        System.out.println(b);
        verified = b;
        return b;
    }
}
