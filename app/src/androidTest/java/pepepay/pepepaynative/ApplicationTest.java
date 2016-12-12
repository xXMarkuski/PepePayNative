package pepepay.pepepaynative;

import android.app.Application;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;
import java.security.Security;

import pepepay.pepepaynative.utils.encryption.EncryptionUtils;

import static junit.framework.Assert.assertEquals;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationTest {
    @Test
    public void acryptotest() throws Exception{
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        KeyPair keys = EncryptionUtils.getKeyPair(12);

        String data = "asdasduoaghsdfgzjdsafguöouieuiqwbjvyx";
        System.out.println(data);

        String enc = EncryptionUtils.complexBase64RsaEncrypt(keys.getPrivate(), data);
        String enc2 = EncryptionUtils.complexBase64RsaEncrypt(keys.getPublic(), data);

        EncryptionUtils.init();

        String dec = EncryptionUtils.complexBase64RsaDecrypt(keys.getPublic(), enc);
        String dec2 = EncryptionUtils.complexBase64RsaDecrypt(keys.getPrivate(), enc2);

        System.out.println(enc);
        System.out.println(dec);

        assertEquals(data, dec);

        System.out.println(enc2);
        System.out.println(dec2);

        assertEquals(data, dec2);

        KeyPair other = EncryptionUtils.getKeyPair(12);

        String messageenc = EncryptionUtils.messageRsaEncrypt(keys.getPrivate(), keys.getPublic(), data);

        EncryptionUtils.init();

        String messagedec = EncryptionUtils.messageRsaDecrypt(keys.getPublic(), keys.getPrivate(), messageenc);

        System.out.println(messageenc);
        System.out.println(messagedec);

        assertEquals(data, messagedec);

    }
}