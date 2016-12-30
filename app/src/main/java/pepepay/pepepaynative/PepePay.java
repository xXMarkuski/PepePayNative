package pepepay.pepepaynative;


import java.io.File;
import java.security.Security;
import java.util.List;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.handler.local.LocalConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.errol.Errol;
import pepepay.pepepaynative.utils.FileUtils;
import pepepay.pepepaynative.utils.loader.LoaderManager;
import pepepay.pepepaynative.utils.loader.loaders.SerializableLoader;

public class PepePay {

    public static final int PROTOCOL_VERSION_MAJOR = 1;
    public static final int PROTOCOL_VERSION_MINOR = 12;
    public static final int PROTOCOL_VERSION_PATCHLEVEL = 10;
    public static final String PROTOCOL_VERSION = PROTOCOL_VERSION_MAJOR + "." + PROTOCOL_VERSION_MINOR + "." + PROTOCOL_VERSION_PATCHLEVEL;

    public static final LoaderManager LOADER_MANAGER = new LoaderManager();

    public static String castle = "SC";
    public static ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
    public static Errol ERROL = new Errol();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        CONNECTION_MANAGER.addConnectionHandler(new LocalConnectionHandler());

        LOADER_MANAGER.registerLoader(new SerializableLoader());
        LOADER_MANAGER.registerLoader(new Wallet.WalletLoader());
    }

    public File godWalletsFile;
    public File walletFile;
    public File privateFile;
    public File nameFile;
    public File errolFile;

    public void create(List<IDeviceConnectionHandler> handlers, File godWalletsFile, File walletFile, File privateFile, File nameFile, File errolFile) {
        //TODO: Make this great again
        if (this.godWalletsFile != null) return;

        System.out.println("if you see this message more than once, shit hit the floor");

        CONNECTION_MANAGER.addConnectionHandlers(handlers);

        this.godWalletsFile = godWalletsFile;
        this.walletFile = walletFile;
        this.privateFile = privateFile;
        this.nameFile = nameFile;
        this.errolFile = errolFile;

        if (errolFile.exists()) {
            ERROL.loadErrols(errolFile);
        }

        Wallets.loadGodWallets(FileUtils.read(godWalletsFile));

        /*if (godWalletsFile.exists()) {
            Wallets.loadGodWallets(godWalletsFile);
        }*/
        if (walletFile.exists()) {
            Wallets.loadWallets(walletFile);
        }
        if (privateFile.exists()) {
            Wallets.loadPrivateKeys(privateFile);
        }
        if (nameFile.exists()) {
            Wallets.loadNames(nameFile);
        }
    }

}
