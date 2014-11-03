package tools.wallet;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Before;
import org.junit.Test;

public class WalletControllerTest {
	
	private NetworkParameters params;
	private WalletController controller;
	private static String testDirectory = "testFiles/";

	@Before
	public void setUp() throws Exception {
		params = TestNet3Params.get();
		controller = new WalletController(params);
	}

	@Test
	public void testSetupWalletKitShouldSetupWalletKit() {
		controller.setupWalletKit(null, testDirectory);
		
		// To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        assertNotNull(controller.getFreshRecieveAddress());
        controller.shutdown();
	}
	
	@Test
	public void testSendFakeCoins() throws AddressFormatException {
		controller.setupWalletKit(null, testDirectory);
		
		// To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        Address address = new Address(params, "mupBAFeT63hXfeeT4rnAUcpKHDkz1n4fdw");
        
        //new coin for test
        Coin coin = Coin.parseCoin("0.009");
        controller.sendCoins(address, coin);
        controller.shutdown();
	}
	
	public void cleanup(){
		File dir = new File(testDirectory);
        assertTrue(dir.isDirectory());
        for (File file:dir.listFiles()) {
            file.delete();
        }
	}

}
