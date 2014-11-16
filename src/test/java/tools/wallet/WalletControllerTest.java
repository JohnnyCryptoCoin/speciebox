package tools.wallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
//		for when we have fake coins in our tests
//		try {
//			Coin mycoins = controller.getBalance();
//			Address toAddress = new Address(params, "msj42CCGruhRsFrGATiUuh25dtxYtnpbTx");
//			controller.sendCoins(toAddress, mycoins, false);
//		} catch (AddressFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		cleanup();
	}
	
	//@Test
	public void testLoadWalletFromFileUnencrypted() throws IOException, UnreadableWalletException {
		controller.setupWalletKit(testWalletDirectory, "specie-wallet-testnet");
		
        assertNotNull(controller.getRecieveAddress(true));
        System.out.println(controller.toString());
        
        controller.shutdown();
	}
	
	@Test
	public void testEncryptAndDecryptWallet(){
		controller.setupWalletKit(null, testDirectory, 1, 1);
		String password = "specieBoxFinalProject";
		controller.encryptWallet("specieBoxFinalProject");
		assertTrue(controller.isEncrypted);
		controller.decryptWallet(password);
		assertFalse(controller.isEncrypted);
		controller.shutdown();
		cleanup();
	}
	
	//@Test
	public void testSaveWalletSeedShouldSaveWalletToDiskAndReloadItFromMSeed() throws IOException, UnreadableWalletException {
		controller.setupWalletKit(null, testDirectory, 1, 1);
		
        assertNotNull(controller.getRecieveAddress(false));
        //then save a new wallet file. this one is unencrypted!
        System.out.println("Saving Wallet");
        String seedcode = controller.saveWalletSeed(testWalletDirectory+"testWallet1");
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
        cleanup();
	}
	
	//@Test
	public void testSendFakeCoins() throws AddressFormatException {
		controller.setupWalletKit(null, testDirectory, 1, 1);
		
		// To test everything we create and print a fresh receiving address. 
		// Send some coins to "TP's TestNet Faucet" return wallet.
		// http://tpfaucet.appspot.com/
        Address address = new Address(params, "msj42CCGruhRsFrGATiUuh25dtxYtnpbTx");
        
        //new coin for test
        Coin coin = Coin.parseCoin("0.009");
        controller.sendCoins(address, coin, false);
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
