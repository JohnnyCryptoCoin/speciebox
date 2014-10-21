package tools.crypto.keys;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import tools.crypto.keys.TestKey;

public class TestKeyTest {
	private TestKey KEY;
	
	@Before
	public void setup(){
		KEY = new TestKey("SIMEON".getBytes());
	}
	
	@Test
	public void testGetters(){
		System.out.println(KEY.getHexKey());
		assertNotNull(KEY.getkeyBytes());
		assertNotNull(KEY.getHexKey());
	}
	
	@Test
	public void testComputeHmacShouldDoJustThat(){
		byte[] hmac = KEY.ComputeHmac("sha1", "I am a message".getBytes());
		assertTrue(hmac.length > 0);
	}

}
