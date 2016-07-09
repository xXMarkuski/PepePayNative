package pepepay.pepepaynative;


import android.app.Activity;

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
import pepepay.pepepaynative.utils.Options;
import pepepay.pepepaynative.utils.loader.LoaderManager;
import pepepay.pepepaynative.utils.loader.loaders.SerializableLoader;

public class PepePay {

    public static final int PROTOCOL_VERSION_MAJOR = 1;
    public static final int PROTOCOL_VERSION_MINOR = 12;
    public static final int PROTOCOL_VERSION_PATCHLEVEL = 5;
    public static final String PROTOCOL_VERSION = PROTOCOL_VERSION_MAJOR + "." + PROTOCOL_VERSION_MINOR + "." + PROTOCOL_VERSION_PATCHLEVEL;

    public static final LoaderManager LOADER_MANAGER = new LoaderManager();

    public static String castle = "SC";

    public static File godWalletsFile;
    public static File walletFile;
    public static File privateFile;
    public static File nameFile;
    public static File optionsFile;
    public static File errolFile;

    public static Options OPTIONS;

    public static ConnectionManager CONNECTION_MANAGER;
    public static Activity ACTIVITY;
    public static Errol ERROL;
    private static List<IDeviceConnectionHandler> handlers;

    public PepePay(List<IDeviceConnectionHandler> handlers) {
        PepePay.handlers = handlers;
    }

    public static void runOnUIThread(Runnable runnable) {
        ACTIVITY.runOnUiThread(runnable);
    }

    public void create(File godWalletsFile, File walletFile, File privateFile, File nameFile, File optionsFile, File errolFile, Activity activity) {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        PepePay.ACTIVITY = activity;

        CONNECTION_MANAGER = new ConnectionManager();
        CONNECTION_MANAGER.addConnectionHandlers(handlers);
        CONNECTION_MANAGER.addConnectionHandler(new LocalConnectionHandler());

        LOADER_MANAGER.registerLoader(new SerializableLoader());
        LOADER_MANAGER.registerLoader(new Wallet.WalletLoader());

        PepePay.godWalletsFile = godWalletsFile;
        PepePay.walletFile = walletFile;
        PepePay.privateFile = privateFile;
        PepePay.nameFile = nameFile;
        PepePay.optionsFile = optionsFile;
        PepePay.errolFile = errolFile;

        ERROL = new Errol();
        if (errolFile.exists()) {
            ERROL.loadErrols(errolFile);
        }

        Wallets.loadGodWallets(FileUtils.readAsset("godWallets"));

        if (godWalletsFile.exists()) {
            Wallets.loadGodWallets(godWalletsFile);
        }
        if (walletFile.exists()) {
            Wallets.loadWallets(walletFile);
        }
        if (privateFile.exists()) {
            Wallets.loadPrivateKeys(privateFile);
        }
        if (nameFile.exists()) {
            Wallets.loadNames(nameFile);
        }

        OPTIONS = Options.load(optionsFile);
    }

}
