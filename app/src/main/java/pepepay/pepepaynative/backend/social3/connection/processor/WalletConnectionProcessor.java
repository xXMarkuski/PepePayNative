package pepepay.pepepaynative.backend.social3.connection.processor;

import java.security.PrivateKey;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.utils.encryption.EncryptionUtils;

public class WalletConnectionProcessor implements ConnectionProcessor {

    private Wallet sender;
    private PrivateKey senderKey;
    private Wallet receiver;

    private ConnectionManager manager;

    public WalletConnectionProcessor(ConnectionManager manager, Wallet sender, PrivateKey senderKey, Wallet receiver) {
        this.manager = manager;
        this.sender = sender;
        this.senderKey = senderKey;
        this.receiver = receiver;
    }

    @Override
    public String send(String data) {
        return EncryptionUtils.messageRsaEncrypt(senderKey, receiver.getPublicKey(), data);
    }

    @Override
    public String receive(String data) {
        return EncryptionUtils.messageRsaDecrypt(receiver.getPublicKey(), senderKey, data);
    }

    @Override
    public String id() {
        return "wallet";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WalletConnectionProcessor) {
            WalletConnectionProcessor other = (WalletConnectionProcessor) obj;
            return other.sender.equals(this.sender) && other.receiver.equals(this.receiver);
        }

        return false;
    }
}
