package pepepay.pepepaynative.utils.encryption;

import com.google.common.primitives.Bytes;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.utils.Holder2;
import pepepay.pepepaynative.utils.StringUtils;

public class EncryptionUtils {

    private static Cipher rsaCipher;
    private static Cipher aesCipher;
    private static KeyGenerator aesKeyGen;
    private static KeyPairGenerator rsaKeyGen;

    static {
        try {
            rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding", PepePay.castle);
            aesCipher = Cipher.getInstance("AES/CFB/PKCS5Padding", PepePay.castle);

            aesKeyGen = KeyGenerator.getInstance("AES", PepePay.castle);
            rsaKeyGen = KeyPairGenerator.getInstance("RSA", PepePay.castle);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //For these algorithms see http://goo.gl/4XPAZs, but basically we use a temporary aes key
    private static byte[] byteArrayAesEncrypt(SecretKey key, byte[] data) {
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            return aesCipher.doFinal(data);
        } catch (Throwable t) {
            t.printStackTrace();
            return new byte[]{Byte.MIN_VALUE};
        }
    }

    private static Holder2<byte[], Throwable> byteArrayAesDecrypt(SecretKey key, byte[] encrypted, byte[] iv) {
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return new Holder2<byte[], Throwable>(aesCipher.doFinal(encrypted), null);
        } catch (Throwable t) {
            t.printStackTrace();
            return new Holder2<byte[], Throwable>(new byte[]{Byte.MIN_VALUE}, t);
        }
    }

    private static String basicBase64AesEncrypt(SecretKey key, String data) {
        return StringUtils.encode(byteArrayAesEncrypt(key, data.getBytes()));
    }

    private static Holder2<String, Holder2<byte[], Throwable>> basicBase64AesDecrypt(SecretKey key, String encrypted, byte[] iv) {
        Holder2<byte[], Throwable> byteArrayAesDecrypt = byteArrayAesDecrypt(key, StringUtils.decode(encrypted), iv);
        return new Holder2<String, Holder2<byte[], Throwable>>(new String(byteArrayAesDecrypt.getT()), byteArrayAesDecrypt);
    }

    /**
     * Use this for aes encrypt
     *
     * @param key  the Key to encrypt with
     * @param data the data to get encrypted
     * @return aes encrypted "data" with the Key key
     */
    public static String complexBase64AesEncrypt(SecretKey key, String data) {
        return StringUtils.multiplex(basicBase64AesEncrypt(key, data), StringUtils.encode(aesCipher.getIV()));
    }

    /**
     * Use this for aes decrypt
     *
     * @param key       the Key to decrypt with
     * @param encrypted the data to get decrypted
     * @return aes decrypted "encrypted" with the Key key
     */
    public static Holder2<String, Holder2<String, Holder2<byte[], Throwable>>> complexBase64AesDecrypt(SecretKey key, String encrypted) {
        String[] parts = StringUtils.demultiplex(encrypted);
        byte[] iv = StringUtils.decode(parts[1]);
        Holder2<String, Holder2<byte[], Throwable>> basicBase64AesDecrypt = basicBase64AesDecrypt(key, parts[0], iv);
        return new Holder2(basicBase64AesDecrypt.getT(), basicBase64AesDecrypt);
    }

    /**
     * Use this for aes encrypt with password
     *
     * @param pw   the password to encrypt with
     * @param data the data to get encrypted
     * @return aes encrypted "data" with the String pw
     */
    public static String complexBase64AesEncryptWithPassword(String pw, String data) {
        ArrayList<Byte> keyBytes = new ArrayList<Byte>(32);
        keyBytes.addAll(Bytes.asList(pw.getBytes()));

        byte[] randomBytes = new byte[32 - keyBytes.size()];
        new Random().nextBytes(randomBytes);

        keyBytes.addAll(Bytes.asList(randomBytes));

        //consider using a pool
        SecretKeySpec key = new SecretKeySpec(Bytes.toArray(keyBytes), "AES");
        return StringUtils.multiplex(StringUtils.encode(randomBytes), complexBase64AesEncrypt(key, data));
    }

    /**
     * Use this for aes decrypt with password
     *
     * @param pw        the Password to decrypt with
     * @param encrypted the data to get decrypted
     * @return aes decrypted "encrypted" with the String pw
     */
    public static String complexBase64AesDecryptWithPassword(String pw, String encrypted) {
        ArrayList<Byte> keyBytes = new ArrayList<Byte>(32);
        keyBytes.addAll(Bytes.asList(pw.getBytes()));

        String[] parts = StringUtils.demultiplex(encrypted);

        byte[] randomBytes = StringUtils.decode(parts[0]);

        keyBytes.addAll(Bytes.asList(randomBytes));

        SecretKeySpec key = new SecretKeySpec(Bytes.toArray(keyBytes), "AES");

        return complexBase64AesDecrypt(key, parts[1]).getT();
    }

    public static boolean complexBase64AesDecryptIsValidPassword(String pw, String encrypted) {
        ArrayList<Byte> keyBytes = new ArrayList<Byte>(32);
        keyBytes.addAll(Bytes.asList(pw.getBytes()));

        String[] parts = StringUtils.demultiplex(encrypted);

        byte[] randomBytes = StringUtils.decode(parts[0]);

        keyBytes.addAll(Bytes.asList(randomBytes));

        SecretKeySpec key = new SecretKeySpec(Bytes.toArray(keyBytes), "AES");

        Throwable throwable = complexBase64AesDecrypt(key, parts[1]).getU().getU().getU();
        return throwable == null;
    }

    private static byte[] basicByteArrayRsaEncrypt(Key key, byte[] data) {
        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, key);
            return rsaCipher.doFinal(data);
        } catch (Throwable t) {
            t.printStackTrace();
            return new byte[]{Byte.MIN_VALUE};
        }
    }

    private static byte[] basicByteArrayRsaDecrypt(Key key, byte[] encrypted) {
        try {
            rsaCipher.init(Cipher.DECRYPT_MODE, key);
            return rsaCipher.doFinal(encrypted);
        } catch (Throwable t) {
            t.printStackTrace();
            return new byte[]{Byte.MIN_VALUE};
        }
    }

    private static String basicBase64RsaEncrypt(Key key, String data) {
        return StringUtils.encode(basicByteArrayRsaEncrypt(key, data.getBytes()));
    }

    private static String basicBase64RsaDecrypt(Key key, String encrypted) {
        return new String(basicByteArrayRsaDecrypt(key, StringUtils.decode(encrypted)));
    }

    /**
     * Use this for rsa encrypt
     *
     * @param key  the Key to encrypt with
     * @param data the data to get encrypted
     * @return rsa encrypted "data" with the Key key
     */
    public static String complexBase64RsaEncrypt(Key key, String data) {
        aesKeyGen.init(256);
        SecretKey aesKey = aesKeyGen.generateKey();

        return StringUtils.multiplex(basicBase64RsaEncrypt(key, StringUtils.encode(aesKey.getEncoded())), complexBase64AesEncrypt(aesKey, data));
    }

    /**
     * Use this for rsa decrypt
     *
     * @param key       the Key to decrypt with
     * @param encrypted the data to get decrypted
     * @return rsa decrypted "encrypted" with the Key key
     */
    public static String complexBase64RsaDecrypt(Key key, String encrypted) {
        String[] parts = StringUtils.demultiplex(encrypted);

        String base64AesKey = basicBase64RsaDecrypt(key, parts[0]);
        SecretKey aesKey = new SecretKeySpec(StringUtils.decode(base64AesKey), "AES");

        return complexBase64AesDecrypt(aesKey, parts[1]).getT();
    }

    /**
     * Use this for message encrypt
     *
     * @param sender   senders PrivateKey
     * @param receiver receiver PublicKey
     * @param data     the data to ger encrypted
     * @return encrypted "data" with senders privateKey and receiver PublicKey
     */
    public static String messageRsaEncrypt(Key sender, Key receiver, String data) {
        return complexBase64RsaEncrypt(receiver, complexBase64RsaEncrypt(sender, data));
    }

    /**
     * Use this for message decrypt
     *
     * @param sender    senders PublicKey
     * @param receiver  receivers PrivateKey
     * @param encrypted the encrypted data
     * @return decrypted "encrypted" with senders PublicKey and receiver PrivateKey
     */
    public static String messageRsaDecrypt(Key sender, Key receiver, String encrypted) {
        return complexBase64RsaDecrypt(sender, complexBase64RsaDecrypt(receiver, encrypted));
    }

    public static String encryptFloat(Key key, float f) {
        return complexBase64RsaEncrypt(key, f + "");
    }

    public static float decryptFloat(Key key, String encrypted) {
        return Float.parseFloat(complexBase64RsaDecrypt(key, encrypted));
    }


    //maybe move to KeyUtils
    private static String basicKeyToString(Key key) {
        return StringUtils.encode(key.getEncoded());
    }

    /**
     * May only work for RSA and AES keys
     *
     * @param key The Key
     * @return String representing the Key
     */
    public static String complexKeyToString(Key key) {
        return StringUtils.multiplex(key.getAlgorithm(), key.getFormat(), basicKeyToString(key));
    }

    /**
     * May only work for RSA and AES keys
     *
     * @param string The String representing the Key
     * @return The Key
     */
    public static Key loadKeyFromString(String string) {
        try {
            String[] parts = StringUtils.demultiplex(string);
            //no java 1.7 String switch-case for us :(
            if (parts[1].equals("X.509"))
                return KeyFactory.getInstance(parts[0]).generatePublic(new X509EncodedKeySpec(StringUtils.decode(parts[2])));
            else if (parts[1].equals("PKCS#8"))
                return KeyFactory.getInstance(parts[0]).generatePrivate(new PKCS8EncodedKeySpec(StringUtils.decode(parts[2])));
            else if (parts[1].equals("RAW"))
                return new SecretKeySpec(StringUtils.decode(parts[2]), parts[0]);
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a new KeyPair
     *
     * @param keysize the higher the more secure, but also slower to generate. 11 or more recommended
     * @return New RsaKeyPair
     */
    public static KeyPair getKeyPair(int keysize) {
        rsaKeyGen.initialize((int) Math.pow(2, keysize));
        return rsaKeyGen.generateKeyPair();
    }


}
