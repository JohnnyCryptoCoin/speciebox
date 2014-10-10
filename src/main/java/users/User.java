package users;

import tools.crypto.CryptographyManager;
import tools.sms.TwilioSMSManager;

public class User
{
  private final String UUID;
  private final int TIMEOUT = 40;
  
  private TwilioSMSManager smsManager;
  private CryptographyManager cryptoManager;

  public User(String uuid)
  {
    this.UUID = uuid;
    this.smsManager = new TwilioSMSManager();
    this.cryptoManager = new CryptographyManager(TIMEOUT);
  }
   
  protected void sendText(String message){
	  smsManager.sendMessage(UUID, message);
  }
  
  protected void sendLoginToken(){
	  StringBuilder sb = new StringBuilder();
	  sb.append("This token is good for only");
	  sb.append(TIMEOUT);
	  sb.append(" seconds \n \"");
	  sb.append(cryptoManager.getOTP(UUID));
	  sb.append("\" - SpecieBox");
	  sendText(sb.toString());
  }
  
  protected boolean verifyToken(String token){
	  return cryptoManager.verifyOTP(UUID, token);
  }
  
  protected boolean createUser() {
	  //connect to DB
	  //take UUID (phone number, hashed P#) and store it in UUID - Phone number table
	  //add user details to uuid-details table
	  return false;
	
}
}