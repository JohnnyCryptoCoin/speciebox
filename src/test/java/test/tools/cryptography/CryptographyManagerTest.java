package test.tools.cryptography;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import tools.crypto.CryptographyManager;
import tools.crypto.TOTP;

public class CryptographyManagerTest {
	
	private CryptographyManager manager = new CryptographyManager(30);
	@Mock TOTP otpGenerator;
	
	@Test
	public void testGetOTPShouldReturnA6DigitCode(){
		String uuid = "6134073789";
		String otp = manager.getOTP(uuid);
		
		System.out.println(otp);
		assertNotNull(otp);
		//verify(otpGenerator).generateTOTP(uuid, 6);
	}
	
	@Test
	public void testVerifyOTPShouldMatchEnteredTokenWithGeneratedOne() throws InterruptedException{
		String uuid = "6134073789";
		String otp = manager.getOTP(uuid);
		
		Thread.sleep(2000L);
		assertNotNull(otp);
		assertTrue(manager.verifyOTP(uuid, otp));
	}
	
	@Test
	public void testVerifyOTPShouldFailIf30SecondsPass() throws InterruptedException{
		String uuid = "6134073789";
		String otp = manager.getOTP(uuid);
		
		Thread.sleep(31000L);
		assertNotNull(otp);
		assertFalse(manager.verifyOTP(uuid, otp));
	}
}
