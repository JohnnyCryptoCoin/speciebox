package demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;

import tools.crypto.PluggableTransactionSigner;
import tools.wallet.WalletController2;

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
		WalletController2 controller1 = new WalletController2(params, classpath, "DemoWallet_1", 2);
		controller1.setupWalletKit(nullSeed);
		Wallet wallet1 = controller1.getWallet();
		DeterministicKey follower_for_wallet1 = wallet1.getWatchingKey();
		System.out.println(follower_for_wallet1.toString());
		System.out.println("---------------------------------------------------");
		
		WalletController2 controller2 = new WalletController2(params, classpath, "DemoWallet_2", 2);
		controller2.setupWalletKit(nullSeed);
		Wallet wallet2 = controller2.getWallet();
		DeterministicKey follower_for_wallet2 = wallet2.getWatchingKey();
		System.out.println(follower_for_wallet2.toString());
		System.out.println("---------------------------------------------------");

		controller1.setName("Wallet_1");
		controller2.setName("Wallet_2");
		
		controller1.addMarriedWallet(controller2.getName(), new PluggableTransactionSigner(follower_for_wallet2, "Signer for wallet 2"));
		controller2.addMarriedWallet(controller1.getName(), new PluggableTransactionSigner(follower_for_wallet1, "Signer for wallet 1"));
		
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
		
        System.out.println("Wallet_1" + controller1.printTransactionSigners(wallet1.getTransactionSigners()));
        System.out.println("Wallet_2" + controller2.printTransactionSigners(wallet2.getTransactionSigners()));
        controller1.saveWallet("DemoWallet_1");
        controller2.saveWallet("DemoWallet_2");
        
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
