package demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;

import tools.wallet.WalletController;

import com.google.common.base.Joiner;

public class SetupDemoWallet {

	/**
	 * Setup a Demo wallet and then save it to disk. When doing so, go to some testnet
	 * faucet and ask for something like 0.5 btc. 
	 * That is all you will have to work with for about 48h
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		NetworkParameters params = TestNet3Params.get();
		WalletController controller = new WalletController(params);
		controller.setupWalletKit(null, "demoWallet/");
		Wallet wallet = controller.getWallet();
		
		System.out.println(wallet.toString());
		
        //System.out.println("Hit enter when you have sent testCoins from a faucet");
        //String token = in.nextLine();
		
//		System.out.println("Enter password to encrypt demoWallet: ");
//      String password = in.nextLine();
//		wallet.encrypt(password);
        
        List<String> mcode = getMCode(wallet);
        saveMCode(mcode);
        controller.shutdown();
	}
	
	private static void saveMCode(List<String> mcode) {
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter("demoWallet/mnemonic_seed.msc"));
	            for (String word: mcode) {
	                out.write(word);
	                out.newLine();
	            }
	            out.close();
	        } catch (IOException e) {}
	}

	public static List<String> getMCode (Wallet wallet){
        DeterministicSeed seed = wallet.getKeyChainSeed();
        System.out.println("seed: " + seed.toString());

        return seed.getMnemonicCode();
	}

}
