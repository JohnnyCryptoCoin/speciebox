package tools.wallet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;

public class WalletControllerTest {
	
	private NetworkParameters params;
	private WalletController controller;
	private static String testDirectory = "testFiles/tmp/";
	private static String testWalletDirectory = "testFiles/";

	@Before
	public void setUp() throws Exception {
		params = TestNet3Params.get();
		controller = new WalletController(params);
	}
	
	@After
	public void teardown(){
		cleanup();
	}
	
	@Test
	public void testLoadWalletFromFileUnencrypted() throws IOException, UnreadableWalletException {
		controller.setupWalletKitFromFile(testWalletDirectory, "specie-wallet-testnet");
		
        assertNotNull(controller.getFreshRecieveAddress());
        System.out.println(controller.toString());
        
        controller.shutdown();
	}
	
	//@Test
	public void testSaveWalletSeedShouldSaveWalletToDiskAndReloadItFromMSeed() throws IOException, UnreadableWalletException {
		controller.setupWalletKit(null, testDirectory);
		
        assertNotNull(controller.getFreshRecieveAddress());
        //then save a new wallet file. this one is unencrypted!
        System.out.println("Saving Wallet");
        String seedcode = controller.saveWalletSeed(testWalletDirectory+"testWallet1.sbox");
        assertNotNull(seedcode);
        System.out.println(seedcode);
        
        //Load from file 
        String passphrase = "";
        Long creationtime = System.currentTimeMillis();
        
        DeterministicSeed seed = new DeterministicSeed(seedcode, null, passphrase, creationtime);

		WalletController loadedController = new WalletController(params);
		loadedController.setupWalletKit(seed, testDirectory);
		loadedController.getWallet().toString();
        
		assertEquals(controller.getWallet().getWatchingKey(), loadedController.getWallet().getWatchingKey());
        controller.shutdown();
        loadedController.shutdown();
	}
	
	//@Test
	public void testSendFakeCoins() throws AddressFormatException {
		controller.setupWalletKit(null, testDirectory);
		
		// To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        Address address = new Address(params, "mupBAFeT63hXfeeT4rnAUcpKHDkz1n4fdw");
        
        //new coin for test
        Coin coin = Coin.parseCoin("0.009");
        controller.sendCoins(address, coin);
        controller.shutdown();
        cleanup();
	}
	
	public void cleanup(){
		File dir = new File(testDirectory);
        assertTrue(dir.isDirectory());
        for (File file:dir.listFiles()) {
            file.delete();
        }
	}

}
