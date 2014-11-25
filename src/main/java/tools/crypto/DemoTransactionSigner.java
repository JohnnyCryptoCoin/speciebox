package tools.crypto;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.signers.CustomTransactionSigner;
import org.bitcoinj.signers.TransactionSigner.ProposedTransaction;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.KeyBag;

import tools.crypto.keys.TestKey;
import tools.sms.TwilioSMSManager;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Scanner;


//Transaction signer which uses provided keychain to get signing keys from. It relies on previous signer to provide
//derivation path to be used to get signing key and, once gets the key, just signs given transaction immediately.

public class DemoTransactionSigner extends CustomTransactionSigner {

    private DeterministicKeyChain keyChain;
	private DeterministicKey watchingKey;
    private String description;
    private String contactNumber = "";
    private volatile TestKey vhex;
    private TwilioSMSManager smsManager =  new TwilioSMSManager();
    private CryptographyManager cryptoManager;

    public DemoTransactionSigner() {
    }

    public DemoTransactionSigner(DeterministicKeyChain keyChain) {
        this(keyChain.getWatchingKey(), keyChain, "SomeWallet's TSigner");
    }
    
    public DemoTransactionSigner(DeterministicKey watchingKey) {
        this(watchingKey, "SomeWallet's TSigner");
    }
    
    public DemoTransactionSigner(DeterministicKeyChain keyChain, String description) {
        this(keyChain.getWatchingKey(), keyChain, description);
    }
    
    public DemoTransactionSigner(DeterministicKey watchingKey, String description) {
    	this(watchingKey, null, description);
    }
    
    public DemoTransactionSigner(DeterministicKey watchingKey, DeterministicKeyChain keyChain, String description) {
        this.watchingKey = watchingKey;
        this.description = description;
        this.keyChain = keyChain;
    }
    
    public boolean setup(DeterministicKeyChain keyChain, String description, String contactNumber) {
        this.watchingKey = keyChain.getWatchingKey();
        this.description = description;
        this.keyChain = keyChain;
        this.contactNumber = contactNumber;
        this.vhex = new TestKey(contactNumber.getBytes());
        this.cryptoManager =  new CryptographyManager(80, vhex);
        return this.isReady();
    }

    //The question to answer is Who am I? I have to find a way to get the signing key without relying on previous signer
    @Override
    protected SignatureAndKey getSignature(Sha256Hash sighash, List<ChildNumber> derivationPath) {
        ImmutableList<ChildNumber> keyPath = ImmutableList.copyOf(derivationPath);
        
	    DeterministicKey key = keyChain.getKeyByPath(keyPath, true);
	    return new SignatureAndKey(key.sign(sighash), key.getPubOnly());
    }
    

	@Override
    public boolean signInputs(ProposedTransaction propTx, KeyBag keyBag) {
    	//Dummy check. We will base our accept/reject criteria off of this.
        Scanner in = new Scanner(System.in);
        
        if (contactNumber.equals("")){
        	System.out.println("You must run setup first!!!!!");
        	return false;
        } else {
        	notifyFollower();
    		System.out.println("--- "+description+" OTP ---");
	        String sig = in.nextLine();
	        if(cryptoManager.verifyOTP(vhex.getHexKey(), sig)){
	        	return super.signInputs(propTx, keyBag);
	        } else {
	        	return false;
	        }
        }
    }
	
	private void notifyFollower() {
		 StringBuilder sb = new StringBuilder();
		  sb.append("This token is good for only").append(80).append(" seconds \n \"");
		  sb.append(cryptoManager.getOTP(vhex.getHexKey()));
		  sb.append("\" - SpecieBox \n");
		  sb.append("Please enter the token in the command-line");
		  smsManager.sendMessage(contactNumber, sb.toString());
	}

	public DeterministicKey getWatchingKey() {
		return watchingKey;
	}
    
	public DeterministicKeyChain getKeyChain() {
		return keyChain;
	}
	
    @Override
    public String toString(){
    	return "TransactionSigner: " + description + ". WatchingKey: " + watchingKey;
    }
}
