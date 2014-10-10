package tools.crypto;

public class CryptographyManager
{
	private final int MSG_DIGITS = 6;
	private final int TIMESTEP;
	public CryptographyManager(int step){
		this.TIMESTEP = step;
	}
  
	public String getOTP(String uuid){
		TOTP otpGenerator = new TOTP(TIMESTEP);
		//SHA1 for now. Return a 6 digit passcode
		return otpGenerator.generateTOTP(uuid, MSG_DIGITS);
	}
	
	public boolean verifyOTP(String uuid, String message){
		return (message.equals(getOTP(uuid)));
	}
	
	public void sendSMS(String number){
		
	}
  
}