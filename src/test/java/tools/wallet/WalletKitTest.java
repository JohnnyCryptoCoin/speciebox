package tools.wallet;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;

public class WalletKitTest {
	private WalletKit walletKit;
	
	//These are wonderful tools provided by bitcoinj for this very purpose
	private NetworkParameters params = TestNet3Params.get();

	//@Test
	public void testNewWalletSetupShouldMockConnections() throws Exception{
		this.walletKit = new WalletKit(params, new File("testFiles/"), "sBox-test");
		walletKit.setBlockingStartup(true);
		
		//Setup the wallet and make sure spvblockchain files are present
		walletKit.startUp();
		
		//assertEquals(walletKit.vWalletFile.getName(), "sBox-test.wallet");
		//assertTrue(walletKit.vStore.getChainHead().toString().contains("difficulty target (nBits): "));
		
		walletKit.awaitRunning();
		
		//To observe wallet events we can use our own implementation of an EventListener
        //WalletListener wListener = new WalletListener();
        //walletKit.wallet().addEventListener(wListener);
        System.out.println("added eventlistener");
     	//To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        System.out.println("send money to: " + walletKit.wallet().freshReceiveAddress().toString());

        //Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        System.out.println("shutting down testWallet");
        walletKit.stopAsync();
        walletKit.awaitTerminated();
	}

}

