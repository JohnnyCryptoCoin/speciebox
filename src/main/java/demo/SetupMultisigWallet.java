package demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;

import tools.wallet.WalletController;

public class SetupMultisigWallet {
	/**
	 * Setup a Demo wallet and then save it to disk. When doing so, go to some testnet
	 * faucet and ask for something like 0.05 btc. 
	 * That is all you will have to work with for about 48h so tests should work with
	 * no more than 0.001 btc otherwise we will test ourselves into a corner
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String classpath = "demoWallet/";
		DeterministicSeed nullSeed = null;
		
		NetworkParameters params = TestNet3Params.get();
		WalletController controller1 = new WalletController(params, classpath, "DemoWallet_1", 2);
		controller1.setupWalletKit(nullSeed);
		Wallet wallet1 = controller1.getWallet();
		DeterministicKey follower_for_wallet1 = wallet1.getWatchingKey();
		
		WalletController controller2 = new WalletController(params, classpath, "DemoWallet_2", 2);
		controller2.setupWalletKit(nullSeed);
		Wallet wallet2 = controller2.getWallet();
		DeterministicKey follower_for_wallet2 = wallet2.getWatchingKey();
		
		System.out.println("---------------------------------------------------");
		System.out.println("Wallet1 WatchingKey: " + follower_for_wallet1);
		System.out.println("Wallet1 B58Key: " + DeterministicKey.deserializeB58(null, follower_for_wallet1.serializePubB58()));
		
		System.out.println("Wallet2: " + follower_for_wallet2);
		System.out.println("Wallet2 B58Key: " + DeterministicKey.deserializeB58(null, follower_for_wallet2.serializePubB58()));
		System.out.println("---------------------------------------------------");
		
		controller1.addMarriedWallet(follower_for_wallet2);
		controller2.addMarriedWallet(follower_for_wallet1);
		
		System.out.println("---------------------------------------------------");
		System.out.println("Send coins to: " + controller1.getRecieveAddress(true));
        System.out.println("Hit enter when you have sent testCoins from a faucet");
        String token = in.nextLine();
		
		
		System.out.println("---------------------------------------------------");
//		System.out.println("Enter password to encrypt demoWallet: ");
//      String password = in.nextLine();
//		wallet.encrypt(password);
		
		System.out.println("---------------------------------------------------");
		System.out.println("Send coins to: " + controller2.getRecieveAddress(true));
		System.out.println("Hit enter when you have sent testCoins from a faucet");
        token = in.nextLine();
		
        controller1.shutdown();
        controller2.shutdown();
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
