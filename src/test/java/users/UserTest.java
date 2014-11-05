package users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Scanner;

import org.junit.Test;

import tools.crypto.keys.TestKey;
import tools.sms.TwilioSMSManager;

public class UserTest {
	
	private String uuid = "16134073789";
	private TestKey key = new TestKey(uuid.getBytes());
	private User user = new User(uuid);
	
	
	//@Test
	public void testLogin() {
		TwilioSMSManager smsManager =  new TwilioSMSManager();
		
		Scanner in = new Scanner(System.in);
		user.sendLoginToken(key.getHexKey());
		String token = in.nextLine();
		if(user.verifyToken(token)){
			user.sendText("YOU LOGGED IN");
			assertTrue(true);
		} else {
			user.sendText("YOU FAILED TO LOG IN. Too slow or wrong token");
			assertTrue(false);
		}
	}
	
	@Test
	public void testGetUUID(){
		assertEquals(uuid, user.getUUID());
	}

}
