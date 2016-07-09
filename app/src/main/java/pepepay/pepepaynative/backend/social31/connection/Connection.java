package pepepay.pepepaynative.backend.social31.connection;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.social3.connection.processor.ConnectionProcessor;
import pepepay.pepepaynative.backend.social3.connection.processor.DefaultConnectionProcessor;
import pepepay.pepepaynative.backend.social3.connection.processor.WalletConnectionProcessor;
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
    public static final String getTransactionsAfter = "getTransactionsAfter";
    public static final String getWalletIDForSimple = "getWalletIDForSimple";
    public static final String beginTransCheck = "beginTransCheck";

    public static final String connectionProcessor = "pros";
    public static final String connectionProcessorWallet = "wallet";

    public static final String REQ = "req";
    public static final String ANS = "ans";
    public static String TAG = "connection";
    private IDevice target;
    private ArrayList<Parcel> toSend;
    private ArrayList<ReceiveHandler> receiveHandlers;
    private ConnectionProcessor processor;
    private ConnectionManager manager;
    private HashMap<Parcel, Function<Void, String>> parcelCallback;

    private String targetWalletID;

    private Function<Void, Void> negotiateTransactionCallback = null;
    private boolean negotiong = false;

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
    public Void eval(Parcel parcel, final Connection connection) {
        String data = parcel.getData();
        try {
            final Object obj = PepePay.LOADER_MANAGER.load(data);
            if (obj instanceof Transaction) {
                Log.d(TAG, "Transaction");
                if (negotiong) {
                    negotiateTransactionCallback = new Function<Void, Void>() {
                        @Override
                        public Void eval(Void aVoid) {
                            handleTransaction(connection, (Transaction) obj, new Function<Void, Void>() {
                                @Override
                                public Void eval(Void aVoid) {
                                    return null;
                                }
                            });
                            negotiateTransactionCallback = null;
                            return null;
                        }
                    };
                } else {
                    handleTransaction(connection, (Transaction) obj, new Function<Void, Void>() {
                        @Override
                        public Void eval(Void aVoid) {
                            return null;
                        }
                    });
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        try {
            String[] str = StringUtils.demultiplex(data);
            if (str[0].equals(Connection.getWallet)) {
                Log.d(TAG, "getWallet: " + str[1]);
                Wallet wallet = Wallets.getWallet(str[1]);
                connection.send(parcel.getAnswer(PepePay.LOADER_MANAGER.save(wallet)));
            } else if (str[0].equals(Connection.connectionProcessor)) {
                if (str[1].equals(Connection.connectionProcessorWallet)) {
                    Wallet ownWallet = Wallets.getOwnWallet(0);
                    processor = new WalletConnectionProcessor(manager, Wallets.getWallet(str[2]), Wallets.getPrivateKey(ownWallet), ownWallet);
                    connection.send(parcel.getAnswer(ownWallet.getPublicKey()));
                }
            } else if (str[0].equals(Connection.getName)) {
                Log.d(TAG, "getName: " + str[1]);
                connection.send(parcel.getAnswer(Wallets.getName(str[1])));
            } else if (str[0].equals(Connection.getWalletNoTransaction)) {
                Log.d(TAG, "getWalletNoTransaction: " + str[1]);
                final Wallet wallet = Wallets.getWallet(str[1]);
                String key = PepePay.LOADER_MANAGER.save(wallet.getPublicKey());
                String transaction = PepePay.LOADER_MANAGER.save(new ArrayList<Transaction>());
                connection.send(parcel.getAnswer(StringUtils.multiplex("w", StringUtils.multiplex(key, transaction))));
                /*if(Wallets.isOwnWallet(wallet)){
                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.connectionProcessor, Connection.connectionProcessorWallet, str[1]), Connection.REQ), new Function<Void, String>() {
                        @Override
                        public Void eval(String s) {
                            processor = new WalletConnectionProcessor(manager, Wallets.getWallet(s), Wallets.getPrivateKey(wallet), wallet);
                            return null;
                        }
                    });
                }*/
            } else if (str[0].equals(Connection.getTransactions)) {
                Log.d(TAG, "getTransactions: " + str[1]);
                Wallet wallet = Wallets.getWallet(str[1]);
                /*if (Wallets.isGodWallet(wallet)) {
                    connection.send(parcel.getAnswer(new ArrayList<Transaction>()));
                } else*/
                {
                    if (str.length < 3) {
                        connection.send(parcel.getAnswer(wallet.getTransactionsChronologically()));
                    } else {
                        connection.send(parcel.getAnswer(wallet.getTransactionsBefore(Long.parseLong(str[2]))));
                    }
                }
            } else if (str[0].equals(Connection.getTransactionsAfter)) {
                Log.d(TAG, "getTransactionsAfter: " + str[1]);
                Wallet wallet = Wallets.getWallet(str[1]);
                if (str.length < 4) {
                    connection.send(parcel.getAnswer(wallet.getTransactionsAfter(Long.parseLong(str[2]))));
                } else {
                    connection.send(parcel.getAnswer(wallet.getTransactionsAfterBefore(Long.parseLong(str[2]), Long.parseLong(str[3]))));
                }
            } else if (str[0].equals(Connection.getWalletIDForSimple)) {
                connection.send(parcel.getAnswer(Wallets.getWalletIDForSimple(str[1])));
            } else if (str[0].equals(Connection.beginTransCheck)) {
                negotiateTransaction(connection, str[1]);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        if (data.equals(Connection.requestWalletIDs)) {
            Log.d(TAG, Connection.requestWalletIDs);
            Parcel answer = parcel.getAnswer(Wallets.getOwnWalletIds());
            connection.send(answer);
        } else if (data.equals(Connection.getNames)) {
            Log.d(TAG, Connection.getNames);
            connection.send(parcel.getAnswer(Wallets.getNames(Wallets.getOwnWallets())));
        }
        return null;
    }

    private void handleTransaction(final Connection connection, final Transaction transaction, final Function<Void, Void> callback) {
        final Function<Void, String> handler = new Function<Void, String>() {
            @Override
            public Void eval(String s) {
                try {
                    System.out.println("handeling");
                    final ArrayList<Transaction> transactions = (ArrayList<Transaction>) PepePay.LOADER_MANAGER.load(s);
                    final Iterator<Transaction> iter = transactions.iterator();

                    final Function<Void, Void>[] function = new Function[]{null};
                    function[0] = new Function<Void, Void>() {
                        @Override
                        public Void eval(Void aVoid) {
                            System.out.println("evaling function[0]");
                            boolean hasNext = iter.hasNext();
                            System.out.println(hasNext + "  " + transactions.size());
                            if (hasNext) {
                                Transaction next = iter.next();
                                if (next.getReceiver().equals(transaction.getSender())) {
                                    handleTransaction(connection, next, function[0]);
                                } else {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Wallets.getWallet(transaction.getReceiver()).addTransaction(transaction);
                                        }
                                    }).start();
                                    function[0].eval(null);
                                }
                            } else {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Wallets.getWallet(transaction.getReceiver()).addTransaction(transaction);
                                    }
                                }).start();
                                callback.eval(null);
                            }
                            return null;
                        }
                    };

                    function[0].eval(null);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return null;
            }
        };

        Wallet senderWallet = Wallets.getWallet(transaction.getSender());
        if (senderWallet == null) {
            connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getWalletNoTransaction, transaction.getSender()), Connection.REQ), new Function<Void, String>() {
                @Override
                public Void eval(String s) {
                    try {
                        Wallets.addWallet((Wallet) PepePay.LOADER_MANAGER.load(s));
                        connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getName, transaction.getSender()), Connection.REQ), new Function<Void, String>() {
                            @Override
                            public Void eval(String s) {
                                Wallets.addName(transaction.getSender(), s);
                                return null;
                            }
                        });
                        connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getTransactions, transaction.getSender(), transaction.getTime() + ""), Connection.REQ), handler);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return null;
                }
            });
        } else {
            connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getTransactionsAfter, transaction.getSender(), senderWallet.getLastTransaction().getTime() + "", transaction.getTime() + ""), Connection.REQ), handler);
        }
    }

    public void negotiateTransaction(final Connection connection, final String walletId) {
        negotiong = true;
        connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getTransactions, walletId), Connection.REQ), new Function<Void, String>() {
            @Override
            public Void eval(String s) {
                try {
                    final ArrayList<Transaction> transactions = (ArrayList<Transaction>) PepePay.LOADER_MANAGER.load(s);
                    final Iterator<Transaction> iter = transactions.iterator();
                    final Function<Void, Void>[] callback = new Function[]{null};
                    callback[0] = new Function<Void, Void>() {
                        @Override
                        public Void eval(Void aVoid) {
                            if (iter.hasNext()) {
                                handleTransaction(connection, iter.next(), callback[0]);
                            } else {
                                if (negotiateTransactionCallback != null) {
                                    negotiateTransactionCallback.eval(null);
                                }
                                negotiong = false;
                            }
                            return null;
                        }
                    };
                    callback[0].eval(null);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return null;
            }
        });
    }

    public void disconnect() {
        manager.disconnect(target);
    }

    public String getTargetWalletID() {
        return targetWalletID;
    }

    public void setTargetWalletID(String targetWalletID) {
        this.targetWalletID = targetWalletID;
    }
}
