package demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
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
		
		NetworkParameters params = TestNet3Params.get();
		WalletController controller1 = new WalletController(params);
		WalletController controller2 = new WalletController(params);
		
		controller1.setupWalletKit(null, "demoWallet/", 2, 2);
		Wallet wallet1 = controller1.getWallet();
		
		
		System.out.println("---------------------------------------------------");
		System.out.println("Send coins to: " + controller1.getRecieveAddress(true));
		
        System.out.println("Hit enter when you have sent testCoins from a faucet");
        String token = in.nextLine();
		
        List<DeterministicKeyChain> followingKeyChains = new ArrayList<DeterministicKeyChain>();
        
        
        followingKeyChains.add(controller1.getWallet().getActiveKeychain());
        
        List<DeterministicKeyChain> seedChains = controller1.getFollowingKeyChains();
        
        controller2.setupWalletKit(seedChains.get(0).getSeed(), "demoWallet/", 2, followingKeyChains);
        
		
		System.out.println("---------------------------------------------------");
//		System.out.println("Enter password to encrypt demoWallet: ");
//      String password = in.nextLine();
//		wallet.encrypt(password);
		
		System.out.println("---------------------------------------------------");
		System.out.println("Send coins to: " + controller2.getRecieveAddress(true));
		System.out.println("Hit enter when you have sent testCoins from a faucet");
        token = in.nextLine();
		
		System.out.println(controller1.toString());
		System.out.println(controller2.toString());

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
