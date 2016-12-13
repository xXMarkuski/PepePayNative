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

        String data = "lo5vKihMpKTVeoFrSzsG" +
                "5gxNI1cWJr2zfwbtZksz" +
                "gTvSP6nIBIJa6cLpWGNY" +
                "hI4mpRj4c1DZgXruz6P7" +
                "idB0bE3nWwZRK6iYdEMU" +
                "pvXcZfJp10iqSCZC6THu" +
                "e3mba1EOCdVmEpbNlKB2" +
                "AW9k9jiBTVdOiAGO3Zct" +
                "kZkxLKonHs3gOYK4biNA" +
                "PI6DRV3SHG93QMmwuO8I" +
                "2k4I78Gztv4E5w7mkqqk" +
                "Xyp4mgdysrWD5bIexwyG" +
                "25CvWPWrpobpR2mKP694" +
                "eYjy9kCvZBVRVfJgtWm6" +
                "1Oln8PrZj9BG0j7ECb5K" +
                "XOAtua8FCN2lhFOR7Tmw" +
                "uIVkZVgALPxL4u1IxeOI" +
                "04fdXfhEBNakqWDNKXgU" +
                "q7x3mqlEoc2x3ZqwJjkm" +
                "U38IpmhHqNXFpG7ips2q" +
                "yeeb0m6UTMsmWC2EQKhT" +
                "Bw6lOIdTfe3TZ4UbXYfx" +
                "GDMzbpX1uip6yHphtDJ1" +
                "ezyyuUIk8vfhsLTo5XCI" +
                "XtK6CtVwa5XcoKihj0Ct" +
                "X1BQYMefZVfGMZE08pkH" +
                "ML34LwcFIvByavVVUXHZ" +
                "S3euEJKcnT9sN4j70sH1" +
                "V71gzUZzZqcYORWLbb0m" +
                "PJ6utgng3ylvBDB76ozN" +
                "XrVNJvLJwkVEk60OnzF7" +
                "j6OJxVcbOnb991zcfZ8H" +
                "rZZ0tOzYv2MN2MnyvI3j" +
                "txgbfyoQiJr3OMmLzAT9" +
                "9gCaYO1HGqcDIMX4sYJm" +
                "au7ADJwky5JviP4PntkP" +
                "KMhvNUYyMvVCbYwTYy8k" +
                "9ziZg3pUI8jXxD9zMuFm" +
                "373LvAkxEiSQGhlpyFb6" +
                "skDr8JrX141BvdlKX1b0" +
                "T36RnNel9V3Fm7Sbw3eT" +
                "FZyczYxAYqWhk2ngJ7dJ" +
                "tPPUyh2vvaMGk40aHr8W" +
                "g8QRZ49gTW7L8vztgND5" +
                "Al5yX8KiWcshrjAH32lY" +
                "p2ZkqXKcVJYyYrTThb22" +
                "46Xx0N79LPtf1TcegiOm" +
                "3BVo4VeLAlMikLqbprbd" +
                "oa9JakiRhz6i2MF4iHM4" +
                "bk83DN7UCqr89aMfFQyj";
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