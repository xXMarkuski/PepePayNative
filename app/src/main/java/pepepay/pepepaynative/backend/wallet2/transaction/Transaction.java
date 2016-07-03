package pepepay.pepepaynative.backend.wallet2.transaction;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

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
        Wallet wallet = Wallets.getWallet(sender);

        //if (sender.equals(receiver)) return false;
        //System.out.println("send rec are not the same");

        try {
            String[] demul = StringUtils.demultiplex(this.getPurpose());
            if (!EncryptionUtils.complexBase64RsaDecrypt(wallet.getPublicKey(), demul[1]).equals(time + receiver))
                return false;

        } catch (Throwable t) {
            return false;
        }

        //System.out.println("came from the true owner");

        if (Wallets.isGodWallet(sender)) return true;
        //System.out.println("not god");
        ArrayList<Transaction> transactions = wallet.getTransactionsBefore(time);
        for (Transaction transaction : transactions) {
            if (!transaction.isValid()) {
                return false;
            }
        }
        //System.out.println("all prev trans are valid");

        /*ArrayList<Transaction> allTrans = wallet.getTransactionsChronologically();
        if (allTrans.size() > 0) {
            if (!Wallets.isGodWallet(allTrans.get(0).getSender())) return false;
        }*/

        boolean b = wallet.calculateBalanceBefor(time) >= amount;
        //System.out.println(b);
        return b;
    }
}
