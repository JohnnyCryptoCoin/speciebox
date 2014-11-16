package demo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
	private static WalletController controller = new WalletController(TestNet3Params.get());
	
	public static void main(String[] args) {
//		boolean loggedIn = login();
		String cmd = "cmd";
		while (!cmd.isEmpty()){
			Scanner in = new Scanner(System.in);
			System.out.println("enter command [load/save/balance/spend/smslogin/print/quit]");
			cmd = in.nextLine();
			if(cmd.equals("load")){
				System.out.println("enter filename: ");
				controller.setupWalletKit("demoWallet/", in.nextLine());
				System.out.println("loaded wallet successfully");
			} 
			
			else if(cmd.equals("seed")){
				System.out.println("enter filename: ");
				loadDemoWallet(in.nextLine());
			} 
			
			else if(cmd.equals("spend")){
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
				System.out.println(controller.toString());
			} 
			
			else {
				cmd = "";
			}
		}
        
		controller.shutdown();
		System.out.println("shutdown complete");
	}

	private static void save(String walletFile) {
			controller.saveWalletSeed("demoWallet/" + walletFile);
	}

	private static void loadDemoWallet(String walletName) {
		try {
			String seedCode = readFile("demoWallet/mnemonic_seed.sbx");
			System.out.println(seedCode);
			String passphrase = "";
	        Long creationtime = System.currentTimeMillis();

	        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, creationtime);
			//Loading wallet from seed
			controller.setupWalletKit(seed, "demoWallet/");
			
			Wallet wallet = controller.getWallet();
			wallet.allowSpendingUnconfirmedTransactions();
			System.out.println(wallet.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnreadableWalletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
