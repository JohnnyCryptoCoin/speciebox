package tools.crypto;

public enum OtpHashMode {
    /// Sha1 is used as the HMAC hashing algorithm
    Sha1 ("HmacSHA1"),
    /// Sha256 is used as the HMAC hashing algorithm
    Sha256 ("HmacSHA256"),
    /// Sha512 is used as the HMAC hashing algorithm
    Sha512 ("HmacSHA512");
    
    private String mode;
    private OtpHashMode(String mode) {
    	this.mode = mode;
	}
    
    @Override
    public String toString() {
    	return this.mode;
    }
}
