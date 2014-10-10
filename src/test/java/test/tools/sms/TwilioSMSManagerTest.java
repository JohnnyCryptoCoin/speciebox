package test.tools.sms;

import static org.junit.Assert.*;

import org.junit.Test;

import tools.sms.TwilioSMSManager;

public class TwilioSMSManagerTest {

	TwilioSMSManager manager =  new TwilioSMSManager();
	
	@Test
	public void testSendBasicSMSFromSimeonsTwilioNumber() {
		String message = "This is a test message from simeon's Twilio Number. Message me if you recieve it";
		manager.sendMessage("16134073789", message);
	}

}
