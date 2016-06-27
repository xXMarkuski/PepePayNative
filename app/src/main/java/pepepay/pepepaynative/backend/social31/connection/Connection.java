package pepepay.pepepaynative.backend.social31.connection;


import java.util.ArrayList;

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
import pepepay.pepepaynative.utils.StringUtils;

public class Connection implements ReceiveHandler {
    public static final String requestWalletIDs = "getWalletIds";
    public static final String getWallet = "getWallet";

    public static final String REQ = "req";
    public static final String ANS = "ans";


    private IDevice target;
    private ArrayList<Parcel> toSend;
    private ArrayList<ReceiveHandler> receiveHandlers;
    private ConnectionProcessor processor;
    private ConnectionManager manager;

    public Connection(IDevice device, ConnectionManager manager) {
        target = device;
        toSend = new ArrayList<Parcel>();
        receiveHandlers = new ArrayList<ReceiveHandler>();
        this.addReceiveHandler(this);
        this.processor = new DefaultConnectionProcessor();
        this.manager = manager;
    }

    public void send(Parcel parcel) {
        toSend.add(parcel);
    }

    public void update() {
        if (manager.canSend(this)) {
            for (Parcel parcel : toSend) {
                manager.send(target, processor.send(PepePay.LOADER_MANAGER.save(parcel)), this);
                toSend.remove(parcel);
            }
        }
    }

    public void receive(String data) {
        Parcel parcel = (Parcel) PepePay.LOADER_MANAGER.load(processor.receive(data));
        for (ReceiveHandler handler : receiveHandlers) {
            handler.eval(parcel, this);
        }
    }

    public void addReceiveHandler(ReceiveHandler receiveHandler) {
        this.receiveHandlers.add(receiveHandler);
    }


    @Override
    public Void eval(Parcel parcel, Connection connection) {
        String data = parcel.getData();
        try {
            Object obj = PepePay.LOADER_MANAGER.load(data);
            if (obj instanceof Transaction) {
                Wallets.getWallet(((Transaction) obj).getReceiver()).addTransaction((Transaction) obj);
            }
        } catch (Throwable throwable) {

        }

        try {
            String[] str = StringUtils.demultiplex(data);
            if (str[0].equals(Connection.getWallet)) {
                Wallet wallet = Wallets.getWallet(str[1]);
                String saveKey = PepePay.LOADER_MANAGER.save(wallet.getPublicKey());
                String saveReceived = PepePay.LOADER_MANAGER.save(wallet.getReceivedTransactions());
                String saveSend = PepePay.LOADER_MANAGER.save(wallet.getSendTransactions());
                connection.send(parcel.getAnswer(StringUtils.multiplex(saveKey, saveReceived, saveSend)));
            } else if (str[0].equals("pros")) {

            }
        } catch (Throwable throwable) {

        }

        if (data.equals(Connection.requestWalletIDs)) {
            Parcel answer = parcel.getAnswer(PepePay.LOADER_MANAGER.save(Wallets.getOwnWalletIds()));
            connection.send(answer);
        }
        return null;
    }
}
