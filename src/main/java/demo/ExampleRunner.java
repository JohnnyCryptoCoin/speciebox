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
import org.bitcoinj.core.Wallet;
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
		while (!cmd.isEmpty()){
			System.out.println("enter command [load/save/balance/spend/smslogin/print/quit]");
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
				} catch (AddressFormatException e) {
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
			
			else {
				cmd = "";
			}
		}
        
		for(WalletController controller:controllers){
			controller.shutdown();
		}
		System.out.println("shutdown complete");
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
