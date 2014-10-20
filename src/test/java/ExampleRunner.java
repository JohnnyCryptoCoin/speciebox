import static org.junit.Assert.assertTrue;

import java.util.Scanner;

import tools.crypto.keys.TestKey;
import tools.sms.TwilioSMSManager;
import users.User;


public class ExampleRunner {
	

	private static final String UUID = "16134073789";
	private static TestKey key = new TestKey(UUID.getBytes());
	private static User user = new User(UUID);
	
	public static void main(String[] args) {
		boolean loggedIn = login();
	}

	private static boolean login() {
		TwilioSMSManager smsManager =  new TwilioSMSManager();
		
		Scanner in = new Scanner(System.in);
		user.sendLoginToken(key.getHexKey());
		String token = in.nextLine();
		if(user.verifyToken(token)){
			user.sendText("YOU LOGGED IN");
			return true;
		} else {
			user.sendText("YOU FAILED TO LOG IN. Too slow or wrong token");
			return false;
		}
	}

}
