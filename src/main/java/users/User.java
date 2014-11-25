package users;

import tools.crypto.CryptographyManager;
import tools.crypto.keys.TestKey;
import tools.sms.TwilioSMSManager;

public class User {
	private final String UUID;
	private int TIMEOUT = 40;
	
	public int getTIMEOUT() {
		return TIMEOUT;
	}
	
	public void setTIMEOUT(int tIMEOUT) {
		TIMEOUT = tIMEOUT;
	}
	
	private final TestKey KEY;
	
	private TwilioSMSManager smsManager;
	private CryptographyManager cryptoManager;
	
	public User(String uuid)
	{
	//TODO: KEY CREATION DECISIONS. Obviously we won't use TestKey OR just the UUID
	//		This will probably be a secure DB call with a volatile byte array or something
	this.KEY = new TestKey(uuid.getBytes());
	  this.UUID = uuid;
	  this.smsManager = new TwilioSMSManager();
	  this.cryptoManager = new CryptographyManager(TIMEOUT, KEY);
	}
	 
	public void sendText(String message){
	  smsManager.sendMessage(UUID, message);
	}
	
	public void sendLoginToken(String OTPKey){
	  StringBuilder sb = new StringBuilder();
	  sb.append("This token is good for only");
	  sb.append(TIMEOUT);
	  sb.append(" seconds \n \"");
	  sb.append(cryptoManager.getOTP(OTPKey));
	  sb.append("\" - SpecieBox");
	  sendText(sb.toString());
	}
	
	public boolean verifyToken(String token){
	  return cryptoManager.verifyOTP(KEY.getHexKey(), token);
	}
	
	public boolean createUser() {
	  //connect to DB
	  //take UUID (phone number, hashed P#) and store it in UUID - Phone number table
	  //add user details to uuid-details table
	  System.out.println("Adding user to DB -- STUBBED METHOD");
	  return true;
	}
	
	protected String getUUID(){
	  return this.UUID;
	}
}