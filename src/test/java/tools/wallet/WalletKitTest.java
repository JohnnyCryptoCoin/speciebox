package tools.wallet;

import java.io.File;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.script.Script;
import org.junit.Test;

public class WalletKitTest {
	private WalletKit walletKit;
	
	//These are wonderful tools provided by bitcoinj for this very purpose
	private NetworkParameters params = UnitTestParams.get();

	@Test
	public void testNewWalletSetupShouldMockConnections() throws Exception{
		this.walletKit = new WalletKit(params, new File("."), "sBox-test");
		
		//Start the wallet and sync the test blockchain
		walletKit.startUp();
		//walletKit.startAsync();
		//walletKit.awaitRunning();
		
		//To observe wallet events we can use our own implementation of an EventListener
        //WalletListener wListener = new WalletListener();
        //walletKit.wallet().addEventListener(wListener);
        
     	//To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        //System.out.println("send money to: " + walletKit.wallet().freshReceiveAddress().toString());

        //Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //System.out.println("shutting down again");
        //walletKit.stopAsync();
        //walletKit.awaitTerminated();
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
