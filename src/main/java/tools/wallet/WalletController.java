package tools.wallet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.LocalTransactionSigner;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import tools.crypto.PluggableTransactionSigner;
import tools.wallet.HDWalletKit;

public class WalletController {
	
	protected final HDWalletKit SPECIEBOX;
	private static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss.SSS";
	private String filePrefix;
	private NetworkParameters params;
	private String name = "SpecieWallet";
	
	public boolean isEncrypted;
    
    public WalletController(NetworkParameters params, String walletDirectory, int threshold){
    	// Determine what network params we are going to handle
    	this(params, walletDirectory, "specie-wallet_" + now(), threshold);
    }
    
    public WalletController(NetworkParameters params, String walletDirectory, String fileName, int threshold){
    	this.params = params;
    	this.isEncrypted = false;
    	this.filePrefix = fileName;
		SPECIEBOX = new HDWalletKit(params, new File(walletDirectory), fileName, threshold);
    }
    
    public WalletController(NetworkParameters params, String walletDirectory, int threshold, List<DeterministicKeyChain> followingKeys){
    	this.params = params;
    	this.isEncrypted = false;
    	this.filePrefix = "specie-wallet_" + now();
    	SPECIEBOX = new HDWalletKit(params, new File(walletDirectory), filePrefix, threshold, true, followingKeys);
    }

    //An HD wallet kit with 1/1 signers is basically a regular wallet
	public void setupWalletKit(@Nullable DeterministicSeed seed) {
		if (seed != null) {
			SPECIEBOX.restoreWalletFromSeed(seed);
		}
		startupWalletKit(filePrefix);
    }
	
	private void startupWalletKit(String fileName){
		SPECIEBOX.setAutoSave(true);
        // Download the block chain and wait until it's done.
        SPECIEBOX.startAsync();
        SPECIEBOX.awaitRunning();
        
        WalletListener wListener = new WalletListener();
        SPECIEBOX.wallet().addEventListener(wListener);
        setName(fileName+"_Fresh Wallet");
	}
	
	public void shutdown(){
		SPECIEBOX.stopAsync();
		SPECIEBOX.awaitTerminated();
	}
	
	public boolean saveWallet(String filepath){
		File walletFile = new File(filepath+".wallet");
		try {
			SPECIEBOX.wallet().saveToFile(walletFile);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public String saveWalletSeed(String filepath){
		try {
			DeterministicSeed seed = SPECIEBOX.wallet().getKeyChainSeed();
			String mcode =Joiner.on(" ").join(seed.getMnemonicCode());
	        BufferedWriter out = new BufferedWriter(new FileWriter(filepath+".sbx"));
            out.write(mcode);
            out.close();
            return mcode;
        } catch (IOException e) {
        	//log once we get that figured out
        	return null;
        }
	}
	
	// Simple wrappers, yet necessary for our implementation of wallet.
	public void encryptWallet(String pass){
		final KeyCrypterScrypt crypter = new KeyCrypterScrypt();
		SPECIEBOX.wallet().encrypt(crypter, crypter.deriveKey(pass));
		isEncrypted = true;
	}
	
	public void decryptWallet(String pass){
		SPECIEBOX.wallet().decrypt(pass);
		isEncrypted = false;
	}
	
	public void addListener(WalletEventListener listener){
		SPECIEBOX.wallet().addEventListener(listener);
	}
	
	public void addFollowingWallet(String description, DeterministicKeyChain chain){
		SPECIEBOX.addPairedWallet(description, chain, false);
	}
	
	public void addMarriedWallet(String description, DeterministicKeyChain chain){
		SPECIEBOX.addPairedWallet(description, chain, true);
	}
	
	public Address getRecieveAddress(boolean isFreshAddress){
		return SPECIEBOX.wallet().freshReceiveAddress();
	}
	
	// for testing only, we will eventually hide this away forever 
	// when we want to know exactly what to expose
	public Wallet getWallet(){
		return SPECIEBOX.wallet();
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		SPECIEBOX.wallet().setDescription(name);
		this.name = name;
	}
	
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public void sendCoins (Address toAddress, Coin value, boolean isRetry){
		try {
            Wallet.SendResult result = SPECIEBOX.wallet().sendCoins(SPECIEBOX.peerGroup(), toAddress, value);
            result.broadcastComplete.get();
            System.out.println("Coins sent. Transaction hash: " + result.tx.getHashAsString());
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet, " + e.missing.getValue() + " satoshis are missing (including fees)");
            listenForCoinsAndRetry(value, toAddress);
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void listenForCoinsAndRetry(final Coin value, final Address toAddress){
        // Wait until the we have enough balance and display a notice.
        ListenableFuture<Coin> balanceFuture = SPECIEBOX.wallet().getBalanceFuture(value, BalanceType.AVAILABLE);
        FutureCallback<Coin> retry = new FutureCallback<Coin>() {
            public void onSuccess(Coin balance) {
                System.out.println("coins arrived and the wallet now has enough balance");
                try {
					Wallet.SendResult result = SPECIEBOX.wallet().sendCoins(SPECIEBOX.peerGroup(), toAddress, value);
					System.out.println("Info: COINS RESENT. Transaction hash: " + result.tx.getHashAsString());
				} catch (InsufficientMoneyException e) {
					e.printStackTrace();
					System.out.println("Still not enough coins in your wallet, missing " + e.missing.getValue() + " satoshis.");
				}
            }
            public void onFailure(Throwable t) {
                System.out.println("something went wrong");
            }
        };
        Futures.addCallback(balanceFuture, retry);
	}
	
	@Override
	public String toString(){
		StringBuilder out = new StringBuilder();
		out.append("This is a ").append(SPECIEBOX.getSigners().size()).append("/").append(SPECIEBOX.getFollowingKeys().size()+1).append(" Wallet. \n");
		
		out.append(printTransactionSigners(SPECIEBOX.getSigners()));
		out.append("\n \n ------------------------------------------------ \n");
		out.append(SPECIEBOX.wallet().toString(true, true, true, null));
		return out.toString();
	}

	public String printTransactionSigners(List<TransactionSigner> signers) {
		StringBuilder out = new StringBuilder();
		out.append("TransactionSigner Info: \n");
		for (TransactionSigner signer : signers){
			out.append("    -").append(signer.toString()).append("\n");
		}
		return out.toString();
	}

	public Coin getBalance() {
		return SPECIEBOX.wallet().getBalance();
	}
}

// A helper class from bitcoinj. Stubbed methods for the most part
// The Wallet event listener its implementations get called on wallet changes.
class WalletListener extends AbstractWalletEventListener {

@Override
public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
    System.out.println("-----> coins resceived: " + tx.getHashAsString());
    System.out.println(wallet.getDescription()+" received: " + tx.getValue(wallet));
}

@Override
public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
    System.out.println("-----> confidence changed: " + tx.getHashAsString());
    TransactionConfidence confidence = tx.getConfidence();
    System.out.println("new block depth: " + confidence.getDepthInBlocks());
}

@Override
public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
    System.out.println("coins sent from "+wallet.getDescription());
}

@Override
public void onReorganize(Wallet wallet) {
}

@Override
public void onWalletChanged(Wallet wallet) {
	System.out.println("wallet "+wallet.getDescription()+" changed");
	System.out.println("last seen block at: " + wallet.getLastBlockSeenTime());
	System.out.println("balance: "+ wallet.getBalance());
}

@Override
public void onKeysAdded(List<ECKey> keys) {
    System.out.println("new key added");
//    for(int i=0; i<keys.size();i++){
//    	System.out.println(keys.get(i));
//    }
}

@Override
public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
    System.out.println("new script added");
    Iterator<Script> scriptIt = scripts.iterator();
    while(scriptIt.hasNext()){
    	Script currentScript = scriptIt.next();
    	System.out.println(currentScript.toString());
    	
    	System.out.println("Need "+ currentScript.getNumberOfSignaturesRequiredToSpend() + " sigs to spend");
    }
}
}
