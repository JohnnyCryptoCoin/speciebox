package test.tools.cryptography;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mock;

import tools.crypto.TOTP;
import tools.crypto.keys.TestKey;

public class TOTPTest {
	private TestKey key = mock(TestKey.class);
	
	private final int MSG_DIGITS = 6;
	
	private TOTP totp = new TOTP(MSG_DIGITS);

	@Test
	public void testGetOTPShouldReturnAnOTP(){
		when(key.getHexKey()).thenReturn("D9562EF65DA0AE19293FEE5242EF4B1C992535B94F6729C89FED5708D1F36A05");
		String otp = totp.generateTOTP(key.getHexKey(), MSG_DIGITS);
		System.out.println(otp);
	}
}
