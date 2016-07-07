package pepepay.pepepaynative.backend.wallet2;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.FileUtils;
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

    //WalletId to Simple
    private static HashMap<String, Integer> simpleMap = new HashMap<>();

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
        return "Unnamed Wallet " + Wallets.getSimple(wallet);
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
            result = walletID;
        }
        if (result.equals(walletID)) {
            result = "Wallet " + Wallets.getSimple(walletID);
        }
        return result;
    }

    public static String getName(Wallet wallet) {
        if (wallet == null) return "null";
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
        if (wallet == null || wallets.contains(wallet)) return;
        System.out.println("added Wallet: " + wallet.getIdentifier());
        wallets.add(wallet);
        if (privateKeys.containsKey(wallet.getIdentifier())) {
            ArrayList<WalletsListener> copy = new ArrayList<>(walletsListeners);
            for (WalletsListener walletsListener : copy) {
                walletsListener.privateWalletAdded(wallet);
            }
        }
    }

    public static ArrayList<Wallet> getOwnWallets() {
        ArrayList<Wallet> result = new ArrayList<Wallet>(privateKeys.size());
        for (String id : privateKeys.keySet()) {
            Wallet wallet = getWallet(id);
            if (wallet != null) result.add(wallet);
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

    public static ArrayList<String> getGodWalletsIDs() {
        return new ArrayList<>(godWallets);
    }

    public static void deleteWallet(Wallet wallet) {
        privateKeys.remove(wallet.getIdentifier());
        godWallets.remove(wallet);

        ArrayList<WalletsListener> copy = new ArrayList<>(walletsListeners);

        for (WalletsListener listener : copy) {
            listener.walletDeleted(wallet);
        }
    }

    public static void deleteWallet(String walletID) {
        deleteWallet(getWallet(walletID));
    }

    public static void addGodWallet(Wallet wallet) {
        addGodWallet(wallet.getIdentifier());
    }

    public static void addGodWallet(String walletID) {
        godWallets.add(walletID);
        saveGodWallets(PepePay.godWalletsFile);
    }

    public static boolean hasName(String walletID) {
        String result = walletNames.get(walletID);
        return result != null;
    }

    public static boolean hasName(Wallet wallet) {
        return hasName(wallet.getIdentifier());
    }

    public static int getOwnWalletsCount() {
        int result = 0;
        for (String s : privateKeys.keySet()) {
            if (getWallet(s) != null) {
                result++;
            }
        }
        return result;
    }

    public static Wallet getOwnWallet(int i) {
        return getWallet(getOwnWalletID(i));
    }

    public static String getOwnWalletID(int i) {
        return new ArrayList<>(privateKeys.keySet()).get(i);
    }

    public static void removeGodWallet(String walletID) {
        godWallets.remove(walletID);
    }

    public static void removeGodWallet(Wallet wallet) {
        removeGodWallet(wallet.getIdentifier());
    }

    public static String getWalletIDForSimple(String simple) {
        for (Wallet wallet : wallets) {
            if ((Wallets.getSimple(wallet) + "").equals(simple)) {
                return wallet.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Point to file which points to wallets
     *
     * @param file the File
     */
    public static void saveWallets(final File file) {
        for (final Wallet wallet : wallets) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUtils.write(FileUtils.child(file, (Wallets.getSimple(wallet) + "")), PepePay.LOADER_MANAGER.save(wallet));
                }
            }).start();
        }
    }

    public static void saveWallet(Wallet wallet) {
        FileUtils.write(FileUtils.child(PepePay.walletFile, Wallets.getSimple(wallet) + ""), PepePay.LOADER_MANAGER.save(wallet));
    }

    public static int getSimple(String walletID) {
        Integer integer = simpleMap.get(walletID);
        if (integer == null) {
            int result = 0;
            for (char c : walletID.toCharArray()) {
                result += c;
            }
            while (simpleMap.containsKey(integer)) {
                result++;
            }
            simpleMap.put(walletID, result);
            return result;
        } else {
            return integer;
        }
    }

    public static int getSimple(Wallet wallet) {
        return getSimple(wallet.getIdentifier());
    }

    public static void loadWallets(File file) {
        final ArrayList<Transaction> transactions = new ArrayList<>();
        for (final File child : file.listFiles()) {
            System.out.println("Loading wallet: " + child.getPath());
            String content = FileUtils.read(child);
            Wallet load = (Wallet) PepePay.LOADER_MANAGER.load(content);
            addWallet(load);
            simpleMap.put(load.getIdentifier(), Integer.parseInt(child.getName()));
            transactions.addAll(load.getScheduledTransactions());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(transactions, Transaction.comparator);

                for (Transaction transaction : transactions) {
                    Wallets.getWallet(transaction.getReceiver()).addTransaction(transaction);
                    Wallets.getWallet(transaction.getSender()).addTransaction(transaction);
                }
            }
        }).start();
    }

    public static void saveAll() {
        Wallets.saveWallets(PepePay.walletFile);
        Wallets.savePrivateKeys(PepePay.privateFile);
        Wallets.saveNames(PepePay.nameFile);
        Wallets.saveGodWallets(PepePay.godWalletsFile);
    }

    public static void saveGodWallets(File file) {
        save(godWallets, file);
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

    public static void loadGodWallets(String string) {
        if (string.isEmpty()) return;
        ArrayList temp = (ArrayList) PepePay.LOADER_MANAGER.load(string);
        godWallets.addAll(temp);
    }

    private static void save(Object obj, File file) {
        FileUtils.write(file, PepePay.LOADER_MANAGER.save(obj));
    }

    private static <T, U> void load(HashMap<T, U> map, File file) {
        HashMap<T, U> temp = (HashMap<T, U>) PepePay.LOADER_MANAGER.load(FileUtils.read(file));
        map.putAll(temp);
    }

    private static <T> void load(ArrayList<T> arrayList, File file) {
        ArrayList<T> temp = (ArrayList<T>) PepePay.LOADER_MANAGER.load(FileUtils.read(file));
        arrayList.addAll(temp);
    }

    public static void notifyBalanceChange(String walletID, Transaction newTransaction) {
        for (WalletsListener walletsListener : walletsListeners) {
            walletsListener.balanceChange(walletID, newTransaction);
        }
    }

    public interface WalletsListener {
        void privateWalletAdded(Wallet wallet);

        void privateWalletGeneratingBegin();

        void nameChange(String walletID, String newName);

        void balanceChange(String walletID, Transaction newTransaction);

        void walletDeleted(Wallet wallet);
    }
}
