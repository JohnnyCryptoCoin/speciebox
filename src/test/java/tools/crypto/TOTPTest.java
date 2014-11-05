package tools.crypto;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import tools.crypto.keys.TestKey;
import tools.crypto.TOTP;

public class TOTPTest {
	private TestKey key = mock(TestKey.class);
	
	private final int MSG_DIGITS = 6;
	
	private TOTP totp = new TOTP(MSG_DIGITS);

	@Test
	public void testGetOTPShouldReturnAnOTP(){
		when(key.getHexKey()).thenReturn("D9562EF65DA0AE19293FEE5242EF4B1C992535B94F6729C89FED5708D1F36A05");
		String otp = totp.generateTOTP(key.getHexKey(), MSG_DIGITS);
	}
	
	@Test
	public void testGetMultipleOTPsShouldReturnTheSameOTP(){
		when(key.getHexKey()).thenReturn("D9562EF65DA0AE19293FEE5242EF4B1C992535B94F6729C89FED5708D1F36A05");
		String otp = totp.generateTOTP(key.getHexKey(), MSG_DIGITS);
		String otp2 = totp.generateTOTP(key.getHexKey(), MSG_DIGITS);
		assertEquals(otp, otp2);
	}
}
