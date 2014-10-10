package tools.crypto.keys;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import tools.crypto.OtpHashMode;

public class TestKey implements IKeyProvider {
	private final byte[] KEY;
	public TestKey(byte[] secretKey) {
		this.KEY = secretKey;
	}

	//HMAC computes a Hashed Message Authentication Code with the crypto hash algorithm as a parameter.
    public byte[] ComputeHmac(String mode, byte[] text){
        try {
            Mac hmac;
            hmac = Mac.getInstance(mode);
            SecretKeySpec macKey =
                new SecretKeySpec(KEY, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}
