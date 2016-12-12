package pepepay.pepepaynative;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.junit.Test;

import java.security.Security;

import pepepay.pepepaynative.utils.encryption.EncryptionUtils;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void acryptotest() throws Exception{
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        String data = "asdasduoaghsdfgzjdsafguöouieuiqwbjvyx";
        String enc = EncryptionUtils.complexBase64AesEncryptWithPassword("hallosicherespw", data);
        String dec = EncryptionUtils.complexBase64AesDecryptWithPassword("hallosicherespw", data);

        System.out.println(data);
        System.out.println(enc);
        System.out.println(dec);

        System.out.println(dec.equals(enc));
    }
}