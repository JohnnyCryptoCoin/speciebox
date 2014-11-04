package demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.spi.LoggerFactory;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;

import tools.wallet.WalletController;

import com.google.common.base.Joiner;

public class SetupDemoWallet {
	/**
	 * Setup a Demo wallet and then save it to disk. When doing so, go to some testnet
	 * faucet and ask for something like 0.05 btc. 
	 * That is all you will have to work with for about 48h so tests should work with
	 * no more than 0.001 btc otherwise we will test ourselves into a corner
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String classpath = "demoWallet/";
		
		NetworkParameters params = TestNet3Params.get();
		WalletController controller = new WalletController(params);
		controller.setupWalletKit(null, "demoWallet/");
		Wallet wallet = controller.getWallet();
		
		System.out.println(wallet.toString());
		
		System.out.println("---------------------------------------------------");
		System.out.println("Send coins to: " + controller.getRecieveAddress(true));
		
        System.out.println("Hit enter when you have sent testCoins from a faucet");
        String token = in.nextLine();
		
//		System.out.println("Enter password to encrypt demoWallet: ");
//      String password = in.nextLine();
//		wallet.encrypt(password);
        
        controller.saveWallet(classpath+"DemoWallet"+WalletController.now()+".wallet");
        controller.saveWalletSeed(classpath+"mnemonic_seed.sbx");
        controller.shutdown();
	}
	
	private static void saveMCode(List<String> mcode) {
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter("demoWallet/mnemonic_seed"+System.currentTimeMillis()+".msc"));
            for (String word: mcode) {
                out.write(word);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
        	//Something??
        }
	}

	public static List<String> getMCode (Wallet wallet){
        DeterministicSeed seed = wallet.getKeyChainSeed();
        System.out.println("seed: " + seed.toString());

        return seed.getMnemonicCode();
	}

}
