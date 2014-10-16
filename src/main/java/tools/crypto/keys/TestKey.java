package tools.crypto.keys;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;


public class TestKey implements IKeyProvider {
	private final byte[] KEY;
	public TestKey(byte[] secretKey) {
		this.KEY = secretKey;
	}

	//HMAC computes a Hashed Message Authentication Code with the crypto hash algorithm as a parameter.
    public byte[] ComputeHmac(String mode, byte[] text) {
        HMac hmac;
		if(mode.equalsIgnoreCase("sha1")){
			SHA1Digest digest =  new SHA1Digest();
			hmac=new HMac(digest);
		}
		else{
			SHA256Digest digest = new SHA256Digest();
			hmac=new HMac(digest);
		}
		byte[] resBuf=new byte[hmac.getMacSize()];
		hmac.init(new KeyParameter(KEY));
		hmac.update(text,0,text.length);
		hmac.doFinal(resBuf,0);
		return resBuf;
    }
    

	@Override
	public String getHexString(byte[] bytes) {
		return Hex.encode(bytes).toString();
	}
}
