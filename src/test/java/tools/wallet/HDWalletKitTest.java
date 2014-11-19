package tools.wallet;

import static org.junit.Assert.*;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.junit.After;
import org.junit.Test;

public class HDWalletKitTest {
	private HDWalletKit walletKit_1;
	private HDWalletKit walletKit_2;
	private String testDirectory = "testFiles/tmp/";
	private String filePrefix = "speciebox-testnet-wallet_";
	
	//These are wonderful tools provided by bitcoinj for this very purpose
	private NetworkParameters params = TestNet3Params.get();

	//@Test
	public void testSetupHDWalletandReload() throws Exception{
		String name = filePrefix+System.currentTimeMillis();
		File dir = new File(testDirectory);
		walletKit_1 = new HDWalletKit(params, dir, name, 1);
		walletKit_1.startAsync();
		walletKit_1.awaitRunning();
		
        assertTrue(walletKit_1.getSigners().size() == 1);
        
        int tSigners = walletKit_1.getSigners().size();
        DeterministicKey watch1 = walletKit_1.wallet().getWatchingKey();
        
		walletKit_1.stopAsync();
		walletKit_1.awaitTerminated();
        
		System.out.println("shutdown wallet 1 loading wallet 2");
		
		walletKit_2 = new HDWalletKit(params, dir, name, 1);
		assertTrue(walletKit_2.RELOAD);
		
		walletKit_2.startAsync();
		walletKit_2.awaitRunning();
		
		DeterministicKey watch2 = walletKit_2.wallet().getWatchingKey();
		
		assertEquals(tSigners, walletKit_2.getSigners().size());
		assertEquals(watch1, watch2);
		walletKit_2.stopAsync();
		walletKit_2.awaitTerminated();
	}
	
	@Test
	public void testSetupHDWalletAndAddWatchingKey() throws Exception{
		walletKit_1 = new HDWalletKit(params, new File(testDirectory), filePrefix+System.currentTimeMillis(), 2);
		walletKit_1.startAsync();
		walletKit_1.awaitRunning();
		
		assertTrue(walletKit_1.getThreshold() == 2);
        assertTrue(walletKit_1.getSigners().size() == 1);
        SecureRandom random = new SecureRandom();
        DeterministicKeyChain chain = new DeterministicKeyChain(random);
        
        
        walletKit_1.addPairedWallet("description", chain.getWatchingKey(), true);
        assertTrue(walletKit_1.getSigners().size() == 2);
		walletKit_1.stopAsync();
		walletKit_1.awaitTerminated();
	}

	public void simpleSend (Address toAddress, Coin value){
		try {
            Wallet.SendResult result = walletKit_2.wallet().sendCoins(walletKit_2.peerGroup(), toAddress, value);
            result.broadcastComplete.get();
            System.out.println("Coins sent. Transaction hash: " + result.tx.getHashAsString());
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet, " + e.missing.getValue() + " satoshis are missing (including fees)");
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void cleanup(){
		File dir = new File(testDirectory);
	    assertTrue(dir.isDirectory());
	    for (File file:dir.listFiles()) {
	        file.delete();
	    }
	}

}

