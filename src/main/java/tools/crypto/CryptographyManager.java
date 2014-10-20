package tools.crypto;

import tools.crypto.keys.TestKey;

public class CryptographyManager
{
	private final int MSG_DIGITS = 6;
	private final int TIMESTEP;
	private final TestKey KEY;
	private final TOTP totp;
	
	public CryptographyManager(int step, TestKey key){
		this.TIMESTEP = step;
		this.KEY = key;
		this.totp = new TOTP(TIMESTEP);
	}
  
	public String getOTP(){
		//SHA1 for now. Return a 6 digit passcode
		return totp.generateTOTP(KEY.getHexKey(), MSG_DIGITS);
	}
	
	public String getOTP(String key){
		//SHA1 for now. Return a 6 digit passcode
		return totp.generateTOTP(key, MSG_DIGITS);
	}
	
	public boolean verifyOTP(String key, String message){
		return (message.equals(getOTP(key)));
	}
	
}