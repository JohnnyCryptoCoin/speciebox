package tools.crypto;

import tools.crypto.keys.IKeyProvider;
import tools.crypto.keys.TestKey;

public abstract class OTP {
    /// <summary>
    /// Secret key
    /// </summary>
    protected static IKeyProvider secretKey;

    /// <summary>
    /// The hash mode to use
    /// </summary>
    protected static OtpHashMode hashMode;

    /// Constructor for the abstract class.  This is to guarantee that all implementations have a secret key
    public OTP(byte[] secretKey, OtpHashMode mode) throws Exception
    {
        if (secretKey.equals(null))
            throw new NullPointerException("secretKey");
        if (secretKey.length < 1)
            throw new Exception("secretKey empty");

        this.secretKey = new TestKey(secretKey);

        this.hashMode = mode;
    }

    /// <summary>
    /// Constrocutor for the abstract class.  This is to guarantee that all implementations have a secret key
    /// </summary>
    /// <param name="secretKey"></param>
    /// <param name="mode">The hash mode to use</param>
    public OTP(IKeyProvider secretKey, OtpHashMode mode)
    {
        if (secretKey.equals(null))
            throw new NullPointerException("secretKey");

        this.secretKey = secretKey;
        this.hashMode = mode;
    }

    /// <summary>
    /// An abstract definition of a compute method.  Takes a counter and runs it through the derived algorithm.
    /// </summary>
    /// <param name="counter">Counter or step</param>
    /// <param name="mode">The hash mode to use</param>
    /// <returns>OTP calculated code</returns>
    protected abstract String compute(long counter, OtpHashMode mode);

    /// <summary>
    /// Helper method that calculates OTPs
    /// </summary>
    protected long calculateOtp(byte[] data, OtpHashMode mode)
    {
        byte[] hmacComputedHash = this.secretKey.ComputeHmac(mode.toString(), data);

        // The RFC has a hard coded index 19 in this value.
        // This is the same thing but also accomodates SHA256 and SHA512

        int offset = hmacComputedHash[hmacComputedHash.length - 1] & 0x0F;
        return (hmacComputedHash[offset] & 0x7f) << 24
            | (hmacComputedHash[offset + 1] & 0xff) << 16
            | (hmacComputedHash[offset + 2] & 0xff) << 8
            | (hmacComputedHash[offset + 3] & 0xff) % 1000000;
    }
/**
    // truncates a number down to the specified number of digits
    protected static String digits(long input, int digitCount)
    {
        Integer truncatedValue = ((int)input % (int)Math.pow(10, digitCount));
        if (truncatedValue.toString().length() < digitCount){
    }

    /// <summary>
    /// Verify an OTP value
    /// </summary>
    /// <param name="initialStep">The initial step to try</param>
    /// <param name="valueToVerify">The value to verify</param>
    /// <param name="matchedStep">Output parameter that provides the step where the match was found.  If no match was found it will be 0</param>
    /// <param name="window">The window to verify</param>
    /// <returns>True if a match is found</returns>
    protected boolean verify(long initialStep, string valueToVerify, out long matchedStep, VerificationWindow window)
    {
        if (window == null)
            window = new VerificationWindow();
        foreach (frame : window.ValidationCandidates(initialStep))
        {
            var comparisonValue = this.Compute(frame, this.hashMode);
            if (comparisonValue == valueToVerify)
            {
                matchedStep = frame;
                return true;
            }
        }

        matchedStep = 0;
        return false;
    } **/
}
