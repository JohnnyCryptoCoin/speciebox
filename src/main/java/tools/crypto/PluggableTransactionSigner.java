package tools.crypto;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.signers.CustomTransactionSigner;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.KeyBag;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Scanner;


//Transaction signer which uses provided keychain to get signing keys from. It relies on previous signer to provide
//derivation path to be used to get signing key and, once gets the key, just signs given transaction immediately.

public class PluggableTransactionSigner extends CustomTransactionSigner {

    private DeterministicKey watchingKey;

	private String description;

    public PluggableTransactionSigner() {
    }

    public PluggableTransactionSigner(DeterministicKey watchingKey) {
        this(watchingKey, "SomeWallet's TSigner");
    }
    
    public PluggableTransactionSigner(DeterministicKey watchingKey, String description) {
    	this(watchingKey, null, description);
    }
    
    public PluggableTransactionSigner(DeterministicKey watchingKey, DeterministicKeyChain keyChain, String description) {
        this.watchingKey = watchingKey;
        this.description = description;
    }

    //The question to answer is Who am I? I have to find a way to get the signing key without relying on previous signer
    @Override
    protected SignatureAndKey getSignature(Sha256Hash sighash, List<ChildNumber> derivationPath) {
        ImmutableList<ChildNumber> keyPath = ImmutableList.copyOf(derivationPath);
        System.out.println("child numer: "+keyPath.get(0).getI());
        
    	//Dummy check. We will base our accept/reject criteria off of this.
        Scanner in = new Scanner(System.in);
    
        
    	return new SignatureAndKey(watchingKey.sign(sighash), watchingKey.getPubOnly());
    }
    
    @Override
    public boolean signInputs(ProposedTransaction propTx, KeyBag keyBag) {
    	//Dummy check. We will base our accept/reject criteria off of this.
        Scanner in = new Scanner(System.in);
        System.out.println("TransactionSigner: " + description + ", do you want to sign this transaxtion? [y/n]");
        System.out.println("Here we will send SMS message and wait for confirmation");
        
        if(in.equals("y") || in.equals("yes")){
	    	return super.signInputs(propTx, keyBag);
        } else {
        	return false;
        }
    }
    
    public DeterministicKey getWatchingKey() {
    	return watchingKey;
    }
    
    public void setWatchingKey(DeterministicKey watchingKey) {
    	this.watchingKey = watchingKey;
    }
    
    @Override
    public String toString(){
    	return "TransactionSigner: " + description + ". WatchingKey: " + watchingKey;
    }
}
