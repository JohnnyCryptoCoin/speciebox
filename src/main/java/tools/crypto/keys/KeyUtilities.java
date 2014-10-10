// Extending Devin Martin's OTP-Sharp
// https://bitbucket.org/devinmartin/otp-sharp/wiki/Home
package tools.crypto.keys;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class KeyUtilities {
    // Overwrite potentially sensitive data with random junk
    // Warning!
    // This isn't foolproof by any means.  
    // This method is an effort to limit the exposure of sensitive data in memory.
    protected static void destroy(byte[] sensitiveData)
    {
        if (sensitiveData == null)
            throw new NullPointerException("sensitiveData");
        new Random().nextBytes(sensitiveData);
    }

    // converts bumbers into a big endian byte array.
    // RFC 4226 specifies big endian as the method for converting the counter to data to hash.
    protected static byte[] getBigEndianBytes(long input)
    {
    	ByteBuffer inBytes = ByteBuffer.allocate(Long.BYTES);
    	inBytes.putLong(input);
    	
    	return getBigEndianBytes(inBytes.array());
    }
    
	protected static byte[] getBigEndianBytes(int input)
    {
			ByteBuffer inBytes = ByteBuffer.allocate(Integer.BYTES);
			inBytes.putLong(input);
	
			return getBigEndianBytes(inBytes.array());
    }
	
	private static byte[] getBigEndianBytes(byte[] input) {
		ByteBuffer bb = ByteBuffer.wrap(input);
    	bb.order( ByteOrder.BIG_ENDIAN);
    	return bb.array();
	}
}
