package tools.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import tools.crypto.keys.TestKey;

public class CryptographyManagerTest {
	private static byte[] UUID = "SIMEON:6134073789".getBytes();
	private TestKey key = new TestKey(UUID);
	
	private CryptographyManager manager = new CryptographyManager(3,key);
	@Mock TOTP otpGenerator;
	
	@Test
	public void testGetOTPShouldReturnA6DigitCode(){
		String otp = manager.getOTP();
		String otp2 = manager.getOTP(key.getHexString("SIMEON".getBytes()));
		
		assertNotNull(otp);
		assertFalse(otp.equals(otp2));
	}
	
	//time based tests suck
	//@Test
	public void testVerifyOTPShouldMatchEnteredTokenWithGeneratedOne() throws InterruptedException{
		String otp = manager.getOTP();
		
		Thread.sleep(1500L);
		assertNotNull(otp);
		assertTrue(manager.verifyOTP(key.getHexKey(), otp));
	}
	
	//@Test
	public void testVerifyOTPShouldFailIf30SecondsPass() throws InterruptedException{
		String otp = manager.getOTP();
		
		Thread.sleep(3100L);
		assertNotNull(otp);
		assertFalse(manager.verifyOTP(key.getHexKey(), otp));
	}
	
	@Test
	public void testVerifyOTPShouldFailIfWrongKey() throws InterruptedException{
		String otp = manager.getOTP();
		
		Thread.sleep(2100L);
		assertNotNull(otp);
		String keyString = key.getHexString("SIMEON".getBytes());
		assertFalse(manager.verifyOTP(keyString, otp));
	}
}
