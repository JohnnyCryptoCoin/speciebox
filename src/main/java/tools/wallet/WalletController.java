package tools.wallet;

import java.io.File;
import java.io.IOException;
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

import com.google.common.collect.Lists;

public class WalletController {
	
	protected static String APP_NAME = "SpecieWallet";
	protected String filePrefix;
	protected NetworkParameters params;
	protected static WalletAppKit bitcoin;
    
    public WalletController(NetworkParameters params){
    	this.params = params;
    	// Determine what network params we are going to handle
		if (params.equals(TestNet3Params.get())) {
		    this.filePrefix = "speciebox-testnet";
		} else if (params.equals(RegTestParams.get())) {
			this.filePrefix = "speciebox-regtest";
		} else {
			this.filePrefix = "speciebox";
		}
    }

	public void setupWalletKit(@Nullable DeterministicSeed seed, String fileExtention) {
		
        // If seed is non-null it means we are restoring from backup.
		bitcoin = new WalletAppKit(params, new File(fileExtention), filePrefix);
        if (seed != null) {
            bitcoin.restoreWalletFromSeed(seed);
        }
        
        // Download the block chain and wait until it's done.
        bitcoin.startAsync();
        bitcoin.awaitRunning();
        
        WalletListener wListener = new WalletListener();
        bitcoin.wallet().addEventListener(wListener);
    }
	
	public void shutdown(){
		bitcoin.stopAsync();
		bitcoin.awaitTerminated();
		System.out.println("Shutdown complete");
	}
	
	public void saveWallet(File walletFile) throws IOException{
		bitcoin.wallet().saveToFile(walletFile);
	}
		
	public void marryWallets(Wallet spouse){
		DeterministicKey spouseKey = spouse.getWatchingKey();
		// threshold of 2 keys,
		//bitcoin.wallet().addFollowingAccountKeys(Lists.newArrayList(spouseKey), 2);
	}
	
	public void addListener(WalletEventListener listener){
		bitcoin.wallet().addEventListener(listener);
	}
	
	public Address getFreshRecieveAddress(){
		return bitcoin.wallet().freshReceiveAddress();
	}
	
	public Wallet getWallet(){
		return bitcoin.wallet();
	}
	
	public void sendCoins (Address toAddress, Coin value){
		
		try {
            Wallet.SendResult result = bitcoin.wallet().sendCoins(bitcoin.peerGroup(), toAddress, value);
            System.out.println("coins sent. transaction hash: " + result.tx.getHashAsString());
            // you can use a block explorer like https://www.biteasy.com/ to inspect the transaction with the printed transaction hash. 
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
            System.out.println("Send money to: " + bitcoin.wallet().currentReceiveAddress().toString());
        }
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
