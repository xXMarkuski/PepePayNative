package pepepay.pepepaynative.backend.social31.connection;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.social3.connection.processor.ConnectionProcessor;
import pepepay.pepepaynative.backend.social3.connection.processor.DefaultConnectionProcessor;
import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.social31.receive.ReceiveHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function;
import pepepay.pepepaynative.utils.StringUtils;

public class Connection implements ReceiveHandler {
    public static final String requestWalletIDs = "getWalletIds";
    public static final String getWallet = "getWallet";
    public static final String getName = "getName";
    public static final String getNames = "getNames";
    public static final String getWalletNoTransaction = "getWalletNoTransactions";
    public static final String getTransactions = "getTransactions";
    public static final String REQ = "req";
    public static final String ANS = "ans";
    public static String TAG = "connection";
    private IDevice target;
    private ArrayList<Parcel> toSend;
    private ArrayList<ReceiveHandler> receiveHandlers;
    private ConnectionProcessor processor;
    private ConnectionManager manager;
    private HashMap<Parcel, Function<Void, String>> parcelCallback;

    public Connection(IDevice device, ConnectionManager manager) {
        target = device;
        toSend = new ArrayList<Parcel>();
        receiveHandlers = new ArrayList<ReceiveHandler>();
        this.addReceiveHandler(this);
        this.processor = new DefaultConnectionProcessor();
        this.manager = manager;
        this.parcelCallback = new HashMap<>();
    }

    public void send(Parcel parcel, Function<Void, String> callback) {
        toSend.add(parcel);
        if (callback != null) {
            parcelCallback.put(parcel, callback);
        }
    }

    public void send(Parcel parcel) {
        this.send(parcel, null);
    }

    public void update() {
        if (manager.canSend(this)) {
            ArrayList<Parcel> copy = new ArrayList(toSend);
            for (Parcel parcel : copy) {
                manager.send(target, processor.send(PepePay.LOADER_MANAGER.save(parcel)), this);
                toSend.remove(parcel);
            }
        }
    }

    public void receive(String data) {
        Parcel ans = (Parcel) PepePay.LOADER_MANAGER.load(processor.receive(data));
        for (ReceiveHandler handler : receiveHandlers) {
            handler.eval(ans, this);
        }

        ArrayList<Parcel> copy = new ArrayList<>(parcelCallback.keySet());

        for (Parcel parcel : copy) {
            if (ans.isAnswerOf(parcel)) {
                parcelCallback.get(parcel).eval(ans.getData());
                parcelCallback.remove(parcel);
            }
        }
    }

    public void addReceiveHandler(ReceiveHandler receiveHandler) {
        this.receiveHandlers.add(receiveHandler);
    }


    @Override
    public Void eval(Parcel parcel, Connection connection) {
        String data = parcel.getData();
        System.out.println(data);
        try {
            Object obj = PepePay.LOADER_MANAGER.load(data);
            if (obj instanceof Transaction) {
                Log.d(TAG, "Transaction");
                handleTransaction(connection, (Transaction) obj, new Function<Void, Void>() {
                    @Override
                    public Void eval(Void aVoid) {
                        return null;
                    }
                });
            }
        } catch (Throwable throwable) {

        }

        try {
            String[] str = StringUtils.demultiplex(data);
            if (str[0].equals(Connection.getWallet)) {
                Log.d(TAG, "getWallet: " + str[1]);
                Wallet wallet = Wallets.getWallet(str[1]);
                connection.send(parcel.getAnswer(PepePay.LOADER_MANAGER.save(wallet)));
            } else if (str[0].equals("pros")) {

            } else if (str[0].equals(Connection.getName)) {
                Log.d(TAG, "getName: " + str[1]);
                connection.send(parcel.getAnswer(Wallets.getName(str[1])));
            } else if (str[0].equals(Connection.getWalletNoTransaction)) {
                Log.d(TAG, "getWalletNoTransaction: " + str[1]);
                String key = PepePay.LOADER_MANAGER.save(Wallets.getWallet(str[1]).getPublicKey());
                String transaction = PepePay.LOADER_MANAGER.save(new ArrayList<Transaction>());
                connection.send(parcel.getAnswer(StringUtils.multiplex("w", StringUtils.multiplex(key, transaction))));
            } else if (str[0].equals(Connection.getTransactions)) {
                Log.d(TAG, "getTransactions: " + str[1]);
                Wallet wallet = Wallets.getWallet(str[1]);
                if (Wallets.isGodWallet(wallet)) {
                    connection.send(parcel.getAnswer(new ArrayList<Transaction>()));
                } else {
                    if (str.length < 2) {
                        connection.send(parcel.getAnswer(wallet.getTransactionsChronologically()));
                    } else {
                        connection.send(parcel.getAnswer(wallet.getTransactionsBefore(Long.parseLong(str[2]))));
                    }
                }
            }

        } catch (Throwable throwable) {

        }

        if (data.equals(Connection.requestWalletIDs)) {
            Log.d(TAG, "requestWalletIDs");
            Parcel answer = parcel.getAnswer(Wallets.getOwnWalletIds());
            connection.send(answer);
        } else if (data.equals(Connection.getNames)) {
            Log.d(TAG, "getNames");
            connection.send(parcel.getAnswer(Wallets.getNames(Wallets.getOwnWallets())));
        }
        return null;
    }

    private void handleTransaction(final Connection connection, final Transaction transaction, final Function<Void, Void> callback) {
        if (Wallets.getWallet(transaction.getSender()) != null) {
            Wallets.getWallet(transaction.getReceiver()).addTransaction(transaction);
        } else {
            connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getWalletNoTransaction, transaction.getSender()), Connection.REQ), new Function<Void, String>() {
                @Override
                public Void eval(String s) {
                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getTransactions, transaction.getSender(), transaction.getTime() + ""), Connection.REQ), new Function<Void, String>() {
                        @Override
                        public Void eval(String s) {
                            ArrayList<Transaction> transactions = (ArrayList<Transaction>) PepePay.LOADER_MANAGER.load(s);
                            for (Transaction trans : transactions) {
                                handleTransaction(connection, transaction, new Function<Void, Void>() {
                                    @Override
                                    public Void eval(Void aVoid) {
                                        Wallets.getWallet(transaction.getReceiver()).addTransaction(transaction);
                                        return null;
                                    }
                                });
                            }

                            callback.eval(null);

                            return null;
                        }
                    });
                    return null;
                }
            });
        }
    }
}
