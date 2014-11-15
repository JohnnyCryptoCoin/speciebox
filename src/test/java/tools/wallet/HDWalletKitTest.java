package tools.wallet;

import static org.junit.Assert.*;

import java.io.File;

import javax.annotation.Nullable;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.After;
import org.junit.Test;

public class HDWalletKitTest {
	private HDWalletKit walletKit;
	private String testDirectory = "testFiles/tmp/";
	private String filePrefix = "speciebox-testnet-wallet_"+System.currentTimeMillis();
	
	//These are wonderful tools provided by bitcoinj for this very purpose
	private NetworkParameters params = TestNet3Params.get();

	@Test
	public void testSetupHDWalletHasSigners() throws Exception{
		walletKit = new HDWalletKit(params, new File(testDirectory), filePrefix, 2, 2, true);
		walletKit.startAsync();
		walletKit.awaitRunning();
		
		System.out.println("Doing things");
        assertTrue(walletKit.getSigners().size() == 2);
        
		walletKit.stopAsync();
		walletKit.awaitTerminated();
        
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

