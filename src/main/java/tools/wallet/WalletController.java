package tools.wallet;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.DeterministicSeed;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WalletController {
	
	public static String APP_NAME = "SpecieWallet";
	public static NetworkParameters params = TestNet3Params.get();
    public static WalletAppKit bitcoin;

	public void setupWalletKit(@Nullable DeterministicSeed seed) {
        // If seed is non-null it means we are restoring from backup.
        bitcoin = new WalletAppKit(params, new File("."), APP_NAME) {
            @Override
            protected void onSetupCompleted() {
                // Don't make the user wait for confirmations for now, as the intention is they're sending it their own money! for now
                bitcoin.wallet().allowSpendingUnconfirmedTransactions();
                if (params != RegTestParams.get())
                    bitcoin.peerGroup().setMaxConnections(11);
                bitcoin.peerGroup().setBloomFilterFalsePositiveRate(0.00001);
            }
        };
        // Now configure and start the appkit. This will take a second or two
        if (params == RegTestParams.get()) {
            bitcoin.connectToLocalHost();   // You should run a regtest mode bitcoind locally.
        } else if (params == MainNetParams.get()) {
            // Checkpoints are block headers that ship inside our app: for a new user, we pick the last header
            // in the checkpoints file and then download the rest from the network. It makes things much faster.
            // Checkpoint files are made using the BuildCheckpoints tool and usually we have to download the
            // last months worth or more (takes a few seconds).
            bitcoin.setCheckpoints(getClass().getResourceAsStream("checkpoints"));
        } else if (params == TestNet3Params.get()) {
            bitcoin.setCheckpoints(getClass().getResourceAsStream("org.bitcoin.test.checkpoints"));
            // As an example!
            bitcoin.useTor();
        }
        if (seed != null)
            bitcoin.restoreWalletFromSeed(seed);
    }
}
