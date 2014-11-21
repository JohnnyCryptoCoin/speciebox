package tools.crypto.keys;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;


public class TestKey implements IKeyProvider {
	private final byte[] KEY;
	public TestKey(byte[] secretKey) {
		//TODO: key design decisions. Pending on DB decisions as well
		SHA256Digest digest = new SHA256Digest();
		digest.update(secretKey, 0, secretKey.length);
		byte[] resBuf=new byte[digest.getDigestSize()];
		digest.doFinal(resBuf, 0);
		this.KEY = resBuf;
		KeyUtilities.destroy(secretKey);
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
    

	public String getHexString(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	public String getHexKey() {
		return getHexString(KEY);
	}
	
	public byte[] getkeyBytes(){
		return KEY;
	}
}
