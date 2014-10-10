package users;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

import tools.sms.TwilioSMSManager;
import users.User;

public class UserTest {
	
	private String uuid = "16134073789";
	private User user = new User(uuid);
	@Test
	public void testLogin() {
		TwilioSMSManager smsManager =  new TwilioSMSManager();
		
		Scanner in = new Scanner(System.in);
		user.sendLoginToken();
		String token = in.nextLine();
		if(user.verifyToken(token)){
			user.sendText("YOU LOGGED IN");
			assertTrue(true);
		} else {
			user.sendText("YOU FAILED TO LOG IN. Too slow or wrong token");
			assertTrue(false);
		}
	}

}
