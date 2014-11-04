package tools.wallet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.DeterministicSeed;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class WalletController {
	
	private static String APP_NAME = "specie-wallet";
	protected static WalletAppKit SPECIEBOX;
	private static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss.SSS";
	private String filePrefix;
	private NetworkParameters params;
    
    public WalletController(NetworkParameters params){
    	this.params = params;
    	
    	// Determine what network params we are going to handle
    	String timestamp = now();
		if (params.equals(TestNet3Params.get())) {
		    this.filePrefix = APP_NAME + "-testnet_" + timestamp;
		} else if (params.equals(RegTestParams.get())) {
			this.filePrefix = APP_NAME + "-regtest_" + timestamp;
		} else {
			this.filePrefix = APP_NAME + "_" + timestamp;
		}
    }

	public void setupWalletKit(@Nullable DeterministicSeed seed, String fileExtention) {
		// If seed is non-null it means we are restoring from backup.
		SPECIEBOX = new WalletAppKit(params, new File(fileExtention), filePrefix);
		if (seed != null) {
			SPECIEBOX.restoreWalletFromSeed(seed);
		}
        // Download the block chain and wait until it's done.
        SPECIEBOX.startAsync();
        SPECIEBOX.awaitRunning();
        
        WalletListener wListener = new WalletListener();
        SPECIEBOX.wallet().addEventListener(wListener);
    }
	
	public void setupWalletKitFromFile(String fileExtention, String fileName) {
		// Send in the filename and file extention. Might trigger exceptions if wallet is already running
		SPECIEBOX = new WalletAppKit(params, new File(fileExtention), fileName);
        // Download the block chain and wait until it's done.
        SPECIEBOX.startAsync();
        SPECIEBOX.awaitRunning();
        
        WalletListener wListener = new WalletListener();
        SPECIEBOX.wallet().addEventListener(wListener);
    }
	
	public void shutdown(){
		SPECIEBOX.stopAsync();
		SPECIEBOX.awaitTerminated();
	}
	
	public boolean saveWallet(String filepath){
		File walletFile = new File(filepath);
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
	        BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
            out.write(mcode);
            out.close();
            return mcode;
        } catch (IOException e) {
        	//log once we get that figured out
        	return null;
        }
	}
	
	public void marryWallets(Wallet spouse){
		DeterministicKey spouseKey = spouse.getWatchingKey();
		// threshold of 2 keys,
		//bitcoin.wallet().addFollowingAccountKeys(Lists.newArrayList(spouseKey), 2);
	}
	
	public void addListener(WalletEventListener listener){
		SPECIEBOX.wallet().addEventListener(listener);
	}
	
	public Address getFreshRecieveAddress(){
		return SPECIEBOX.wallet().freshReceiveAddress();
	}
	
	//for testing only, we will eventually hide this away forever 
	//when we want to know exactly what to expose
	public Wallet getWallet(){
		return SPECIEBOX.wallet();
	}
	
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public void sendCoins (Address toAddress, Coin value){
		
		try {
            Wallet.SendResult result = SPECIEBOX.wallet().sendCoins(SPECIEBOX.peerGroup(), toAddress, value);
            System.out.println("coins sent. transaction hash: " + result.tx.getHashAsString());
            // you can use a block explorer like https://www.biteasy.com/ to inspect the transaction with the printed transaction hash. 
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
            System.out.println("Send money to: " + SPECIEBOX.wallet().currentReceiveAddress().toString());
        }
	}
	
	@Override
	public String toString(){
		return SPECIEBOX.wallet().toString();
	}
}

//A helper class from bitcoinj. Stubbed methods for the most part
//The Wallet event listener its implementations get called on wallet changes.
class WalletListener extends AbstractWalletEventListener {

@Override
public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
    System.out.println("-----> coins resceived: " + tx.getHashAsString());
    System.out.println("received: " + tx.getValue(wallet));
}

@Override
public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
    System.out.println("-----> confidence changed: " + tx.getHashAsString());
    TransactionConfidence confidence = tx.getConfidence();
    System.out.println("new block depth: " + confidence.getDepthInBlocks());
}

@Override
public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
    System.out.println("coins sent");
}

@Override
public void onReorganize(Wallet wallet) {
}

@Override
public void onWalletChanged(Wallet wallet) {
	System.out.println("wallet changed");
}

@Override
public void onKeysAdded(List<ECKey> keys) {
    System.out.println("new key added");
}

@Override
public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
    System.out.println("new script added");
}
}
