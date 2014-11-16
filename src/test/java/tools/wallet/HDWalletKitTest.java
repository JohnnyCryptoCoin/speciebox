package tools.wallet;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.TestNet3Params;
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
	public void testSetupHDWalletHasSigners() throws Exception{
		walletKit_1 = new HDWalletKit(params, new File(testDirectory), filePrefix, 1, 1, true);
		walletKit_1.startAsync();
		walletKit_1.awaitRunning();
		
		System.out.println("Doing things");
        assertTrue(walletKit_1.getSigners().size() == 1);
        
		walletKit_1.stopAsync();
		walletKit_1.awaitTerminated();
        
	}
	
	@Test
	public void testSetupHDWalletWithRealWatchingKey() throws Exception{
		walletKit_1 = new HDWalletKit(params, new File(testDirectory), filePrefix+System.currentTimeMillis(), 1, 1, true);
		walletKit_1.startAsync();
		walletKit_1.awaitRunning();
		
		System.out.println("Doing things");
        assertTrue(walletKit_1.getSigners().size() == 1);
        
        List<DeterministicKeyChain> chain = new ArrayList<DeterministicKeyChain>();
        chain.add(walletKit_1.wallet().getActiveKeychain());
        walletKit_2 = new HDWalletKit(params, new File(testDirectory), filePrefix+System.currentTimeMillis(), 2, 2, true);
		walletKit_2.startAsync();
		walletKit_2.awaitRunning();
		
		assertTrue(walletKit_2.getSigners().size() == 2);
		
		walletKit_1.wallet().toString();
		System.out.println("------------------------------------------------------------");
		walletKit_2.wallet().toString();
		
		WalletListener wListener = new WalletListener();
		walletKit_1.wallet().addEventListener(wListener);
		walletKit_2.wallet().addEventListener(wListener);
		
		Coin amount = Coin.valueOf(0,20);
		
		
		
		walletKit_1.stopAsync();
		walletKit_1.awaitTerminated();
		walletKit_2.stopAsync();
		walletKit_2.awaitTerminated();
        
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

