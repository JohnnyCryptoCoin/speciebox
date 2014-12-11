package tools.crypto;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class TOTP {

	private int timeStep;
	
     public TOTP(){
         // default constructor has 30s timeout
    	 this.timeStep = 30;
     }

     public TOTP(int timeStep) {
		this.timeStep = timeStep;
	}

	//HMAC computes a Hashed Message Authentication Code with the crypto hash algorithm as a parameter.
     private byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text){
         try {
             Mac hmac;
             hmac = Mac.getInstance(crypto);
             SecretKeySpec macKey =
                 new SecretKeySpec(keyBytes, "RAW");
             hmac.init(macKey);
             return hmac.doFinal(text);
         } catch (GeneralSecurityException gse) {
             throw new UndeclaredThrowableException(gse);
         }
     }


     // This method converts a HEX string to Byte[]
     private byte[] hexStr2Bytes(String hex){
    	 // Adding one byte to get the right conversion
         // Values starting with "0" can be converted
         byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

         // Copy all the REAL bytes, not the "first"
         byte[] ret = new byte[bArray.length - 1];
         for (int i = 0; i < ret.length; i++)
             ret[i] = bArray[i+1];
         return ret;
     }

     private final long[] DIGITS_POWER
     // 0  1   2    3     4      5       6        7         8          9           10
     = {1L,10L,100L,1000L,10000L,100000L,1000000L,10000000L,100000000L,1000000000L,10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 10000000000000L };

    // These methods generate a TOTP value for the given set of parameters.
     public String generateTOTP(String key, int returnDigits){
         return generateTOTP(key, returnDigits, "HmacSHA1");
     }
     
     public String generateTOTP256(String key, int returnDigits){
         return generateTOTP(key, returnDigits, "HmacSHA256");
     }

     public String generateTOTP512(String key, int returnDigits){
         return generateTOTP(key, returnDigits, "HmacSHA512");
     }

     // Generic TOTP method for all the above params, 
     // we do not allow user set the time manually!
     public String generateTOTP(String key, int returnDigits, String crypto){
         String result = null;
         String time = currentTimeStampUTC();
         
         //Pad the value to 16 characters
         while (time.length() < 16 ){
             time = "0" + time;
         }

         // Get the HEX strings in a Byte[]
         byte[] hash = hmac_sha(crypto, hexStr2Bytes(key), hexStr2Bytes(time));

         // put selected bytes into result int
         int offset = hash[hash.length - 1] & 0xf;

         int binary =
             ((hash[offset] & 0x7f) << 24) |
             ((hash[offset + 1] & 0xff) << 16) |
             ((hash[offset + 2] & 0xff) << 8) |
             (hash[offset + 3] & 0xff);

         //Modulo function of the 'returnDigits-th' power of 10
         result = Long.toString(binary % DIGITS_POWER[returnDigits]);
         
         //Pad the result just in case
         while (result.length() < returnDigits) {
             result = "0" + result;
         }
         return result;
     }
     
     private String currentTimeStampUTC(){
 	    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 	    long time = (cal.getTimeInMillis() / 1000) / timeStep;
 	    return Long.toString(time);
     }
}