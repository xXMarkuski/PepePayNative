package pepepay.pepepaynative.backend.wallet2;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.FileUtils;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.encryption.EncryptionUtils;

public class Wallets {
    //privateKey for WalletID
    private static HashMap<String, String> privateKeys = new HashMap<String, String>();
    //WalletName for WalletID
    private static HashMap<String, String> walletNames = new HashMap<String, String>();
    //Wallets
    private static ArrayList<Wallet> wallets = new ArrayList<Wallet>();
    //God Wallets
    private static ArrayList<String> godWallets = new ArrayList<String>();

    //Wallet add Listeners
    private static ArrayList<WalletsListener> walletsListeners = new ArrayList<WalletsListener>();

    public static void generateAndAddWallet(final int keysize, final String name, final String pin, final WalletsListener listener) {
        for (WalletsListener walletAddListener : walletsListeners) {
            walletAddListener.privateWalletGeneratingBegin();
        }
        if (listener != null) {
            listener.privateWalletGeneratingBegin();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final KeyPair pair = EncryptionUtils.getKeyPair(keysize);
                Wallet wallet = new Wallet(pair.getPublic(), new ArrayList<Transaction>());
                Wallets.addPrivateKey(wallet, pair.getPrivate(), pin);
                if (name.isEmpty()) Wallets.addName(wallet, Wallets.getDefaultName(wallet));
                else Wallets.addName(wallet, name);
                for (WalletsListener walletAddListener : walletsListeners) {
                    walletAddListener.privateWalletAdded(wallet);
                }
                if (listener != null) {
                    listener.privateWalletAdded(wallet);
                }
                saveAll();
            }
        }).start();
    }

    public static void generateAndAddWallet(final int keysize, final String name, final String pin) {
        Wallets.generateAndAddWallet(keysize, name, pin, null);
    }

    public static String getDefaultName(Wallet wallet) {
        return "Unnamed Wallet " + StringUtils.getSimple(wallet.getIdentifier());
    }

    public static void addWalletAddListener(WalletsListener walletAddListener) {
        walletsListeners.add(walletAddListener);
    }

    public static void removeWalletAddListener(WalletsListener walletAddListener) {
        walletsListeners.remove(walletAddListener);
    }

    public static boolean isValidPassword(String walletID, String password) {
        String encrypted = privateKeys.get(walletID);
        return EncryptionUtils.complexBase64AesDecryptIsValidPassword(password, encrypted);
    }

    public static boolean isValidPassword(Wallet wallet, String password) {
        return isValidPassword(wallet.getIdentifier(), password);
    }

    public static PrivateKey getPrivateKey(String walletID, String password) {
        String encrypted = privateKeys.get(walletID);
        String decrypted = EncryptionUtils.complexBase64AesDecryptWithPassword(password, encrypted);
        return (PrivateKey) EncryptionUtils.loadKeyFromString(decrypted);
    }

    public static PrivateKey getPrivateKey(Wallet wallet, String password) {
        return getPrivateKey(wallet.getIdentifier(), password);
    }

    public static PrivateKey getPrivateKey(Wallet wallet) {
        return getPrivateKey(wallet, "");
    }


    public static void addPrivateKey(String walletID, PrivateKey key, String password) {
        privateKeys.put(walletID, EncryptionUtils.complexBase64AesEncryptWithPassword(password, EncryptionUtils.complexKeyToString(key)));
    }

    public static void addPrivateKey(Wallet wallet, PrivateKey privateKey, String password) {
        addPrivateKey(wallet.getIdentifier(), privateKey, password);
        addWallet(wallet);
    }

    public static String getName(String walletID) {
        String result = walletNames.get(walletID);
        if (result == null) {
            return walletID;
        }
        if (result.equals(walletID)) {
            result = "Wallet " + StringUtils.getSimple(walletID);
        }
        return result;
    }

    public static String getName(Wallet wallet) {
        return getName(wallet.getIdentifier());
    }

    public static ArrayList<String> getNames(ArrayList<Wallet> wallets) {
        ArrayList<String> result = new ArrayList<String>(wallets.size());
        for (Wallet wallet : wallets) {
            result.add(getName(wallet));
        }
        return result;
    }

    public static void addName(String walletID, String name) {
        walletNames.put(walletID, name);
        for (WalletsListener walletListener : walletsListeners) {
            walletListener.nameChange(walletID, name);
        }
    }

    public static void addName(Wallet wallet, String name) {
        addWallet(wallet);
        addName(wallet.getIdentifier(), name);
    }

    public static Wallet getWallet(String walletID) {
        for (Wallet wallet : wallets) {
            if (wallet.getIdentifier().equals(walletID)) return wallet;
        }
        return null;
    }

    public static ArrayList<Wallet> getWallets(ArrayList<String> ids) {
        ArrayList<Wallet> result = new ArrayList<Wallet>(ids.size());
        for (String id : ids) {
            Wallet wallet = getWallet(id);
            if (wallet != null) result.add(wallet);
        }
        return result;
    }

    public static void addWallet(Wallet wallet) {
        wallets.add(wallet);
    }

    public static ArrayList<Wallet> getOwnWallets() {
        ArrayList<Wallet> result = new ArrayList<Wallet>(privateKeys.size());
        for (String id : privateKeys.keySet()) {
            result.add(getWallet(id));
        }

        return result;
    }

    public static ArrayList<String> getOwnWalletIds() {
        return new ArrayList<>(privateKeys.keySet());
    }

    public static boolean isOwnWallet(String walletId) {
        return Wallets.privateKeys.keySet().contains(walletId);
    }

    public static boolean isOwnWallet(Wallet wallet) {
        return isOwnWallet(wallet.getIdentifier());
    }

    public static boolean isGodWallet(String wallet) {
        return godWallets.contains(wallet);
    }

    public static boolean isGodWallet(Wallet wallet) {
        return isGodWallet(wallet.getIdentifier());
    }

    public static ArrayList<Wallet> getGodWallets() {
        ArrayList<Wallet> result = new ArrayList<Wallet>(godWallets.size());
        for (String id : godWallets) {
            result.add(getWallet(id));
        }
        return result;
    }

    /**
     * Point to file which points to wallets
     *
     * @param file the File
     */
    public static void saveWallets(File file) {
        for (Wallet wallet : wallets) {
            FileUtils.write(FileUtils.child(file, (StringUtils.getSimple(wallet.getIdentifier()) + "")), PepePay.LOADER_MANAGER.save(wallet));
        }
    }

    public static void saveWallet(Wallet wallet) {
        FileUtils.write(FileUtils.child(PepePay.walletFile, StringUtils.getSimple(wallet.getIdentifier()) + ""), PepePay.LOADER_MANAGER.save(wallet));
    }

    public static void loadWallets(File file) {
        for (File child : file.listFiles()) {
            String content = null;
            try {
                System.out.println(child);
                content = Files.toString(child, Charset.forName("UTF-8"));
                System.out.println(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            wallets.add((Wallet) PepePay.LOADER_MANAGER.load(content));
        }
    }

    public static void saveAll() {
        Wallets.saveWallets(PepePay.walletFile);
        Wallets.savePrivateKeys(PepePay.privateFile);
        Wallets.saveNames(PepePay.nameFile);
    }

    public static void savePrivateKeys(File file) {
        save(privateKeys, file);
    }

    public static void loadPrivateKeys(File file) {
        load(privateKeys, file);
    }

    public static void saveNames(File file) {
        save(walletNames, file);
    }

    public static void loadNames(File file) {
        load(walletNames, file);
    }

    public static void loadGodWallets(File file) {
        load(godWallets, file);
    }

    private static void save(Object obj, File file) {
        try {
            Files.write(PepePay.LOADER_MANAGER.save(obj), file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T, U> void load(HashMap<T, U> map, File file) {
        HashMap<T, U> temp = (HashMap<T, U>) PepePay.LOADER_MANAGER.load(FileUtils.read(file));
        map.putAll(temp);
    }

    private static <T> void load(ArrayList<T> ArrayList, File file) {
        ArrayList<T> temp = (ArrayList<T>) PepePay.LOADER_MANAGER.load(FileUtils.read(file));
        ArrayList.addAll(temp);
    }

    public static void notifyBalanceChange(String walletID, float newBalance) {
        for (WalletsListener walletsListener : walletsListeners) {
            walletsListener.balanceChange(walletID, newBalance);
        }
    }

    public interface WalletsListener {
        void privateWalletAdded(Wallet wallet);

        void privateWalletGeneratingBegin();

        void nameChange(String walletID, String newName);

        void balanceChange(String walletID, float newBalance);
    }
}
