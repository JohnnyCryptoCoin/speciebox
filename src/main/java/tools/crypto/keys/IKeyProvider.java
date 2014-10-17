package tools.crypto.keys;

public interface IKeyProvider {
    // Uses the key to get an HMAC using the specified algorithm and data
    // HMAC of the key and data
    public byte[] ComputeHmac(String mode, byte[] data);
    
    public String getHexString(byte[] bytes);
    public String getHexKey();
}
