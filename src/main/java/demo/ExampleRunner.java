package demo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.DeterministicSeed;

import tools.wallet.WalletController;

import com.google.common.base.Joiner;


public class ExampleRunner {
	

//	private static final String UUID = "16134073789";
//	private static TestKey key = new TestKey(UUID.getBytes());
//	private static User user = new User(UUID);
	private static String testDirectory = "testFiles/";
	private static List<WalletController> controllers = new ArrayList<WalletController>();
	private static Scanner in = new Scanner(System.in);
	
	public static void main(String[] args) {
//		boolean loggedIn = login();
		String cmd = "cmd";
		while (cmd != "quit"){
			System.out.println("enter command [setup/load/save/balance/spend/smslogin/print/quit]");
			cmd = in.nextLine();
			if(cmd.equals("load")){
				System.out.println("enter filename: ");
				String fileName = in.nextLine();
				System.out.println("enter predetermined_threshold: ");
				int threshold = Integer.parseInt(in.nextLine());
				WalletController controller = new WalletController(TestNet3Params.get(), "demoWallet/", fileName, threshold);
				controller.setupWalletKit(null);
				
				controllers.add(controller);
				System.out.println("loaded wallet successfully");
			} 
			else if(cmd.equals("spend")){
				System.out.println("enter wallet_ID: ");
				String id = in.nextLine();
				WalletController controller = controllers.get(Integer.parseInt(id));
				try {
					System.out.println("enter address to send to: ");
					String stringAddr = in.nextLine();
					Address toAddress;
					toAddress = new Address(TestNet3Params.get(), stringAddr);
					System.out.println("enter value [0.000001 BTC,"+controller.getBalance().toFriendlyString()+"]: ");
					String stringVal = in.nextLine();
					Coin value = Coin.parseCoin(stringVal);
					controller.sendCoins(toAddress, value, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			
			else if(cmd.equals("balance")){
				System.out.println("enter wallet_ID: ");
				String id = in.nextLine();
				WalletController controller = controllers.get(Integer.parseInt(id));;
				System.out.println(controller.getBalance().toFriendlyString());
			}
			
			else if (cmd.equals("save")) {
				System.out.println("enter filename: ");
				save(in.nextLine());
			} 
			
			else if (cmd.equals("smslogin")) {
				login();
			} 
			
			else if (cmd.equals("print")) {
				System.out.println("enter wallet_ID: ");
				String id = in.nextLine();
				WalletController controller = controllers.get(Integer.parseInt(id));
				System.out.println(controller.toString());
			} 
			
			else if (cmd.equals("setup")) {
				setupMarriedWallets();
			} 
			
			else {
				System.out.println("I can't do that Dave...");
			}
		}
        
		for(WalletController controller:controllers){
			controller.shutdown();
		}
		System.out.println("shutdown complete");
	}

	private static void setupMarriedWallets() {
		String classpath = "demoWallet/";
		DeterministicSeed nullSeed = null;
		
		NetworkParameters params = TestNet3Params.get();
		WalletController controller1 = new WalletController(params, classpath, "DemoWallet_1", 2);
		controller1.setupWalletKit(nullSeed);
		Wallet wallet1 = controller1.getWallet();
		DeterministicKey follower_for_wallet1 = wallet1.getWatchingKey();
		System.out.println(follower_for_wallet1.toString());
		System.out.println("---------------------------------------------------");
		
		WalletController controller2 = new WalletController(params, classpath, "DemoWallet_2", 2);
		controller2.setupWalletKit(nullSeed);
		Wallet wallet2 = controller2.getWallet();
		DeterministicKey follower_for_wallet2 = wallet2.getWatchingKey();
		System.out.println(follower_for_wallet2.toString());
		System.out.println("---------------------------------------------------");

		controller1.setName("Wallet_1");
		controller2.setName("Wallet_2");
		
		controller1.addMarriedWallet(controller2.getName(), follower_for_wallet2);
		controller2.addMarriedWallet(controller1.getName(), follower_for_wallet1);
		
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
        
        controllers.add(controller1);
        controllers.add(controller2);
	}

	private static void save(String walletFile) {
		System.out.println("enter wallet_ID: ");
		String id = in.nextLine();
		WalletController controller = controllers.get(Integer.parseInt(id));
		controller.saveWalletSeed("demoWallet/" + walletFile);
	}

	static String readFile(String path) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(path), Charset.forName("UTF-8"));
		
		return Joiner.on(" ").join(lines);
	}
	
	private static boolean login() {
//		TwilioSMSManager smsManager =  new TwilioSMSManager();
//		
//		Scanner in = new Scanner(System.in);
//		user.sendLoginToken(key.getHexKey());
//		String token = in.nextLine();
//		if(user.verifyToken(token)){
//			user.sendText("YOU LOGGED IN");
//			return true;
//		} else {
//			user.sendText("YOU FAILED TO LOG IN. Too slow or wrong token");
			return false;
//		}
	}

}
