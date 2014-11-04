package demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.core.Wallet;

import com.google.common.base.Joiner;
import com.google.common.base.Utf8;

import tools.wallet.WalletController;


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
			System.out.println("enter command [load/smslogin/quit]");
			cmd = in.nextLine();
			if(cmd.equals("load")){
				loadDemoWallet();
			} else if (cmd.equals("save")) {
				System.out.println("enter filename: ");
		        String token = in.nextLine();
				save(token);
			} else if (cmd.equals("smslogin")) {
				login();
			} else {
				cmd = "";
			}
		}
        
	}

	private static void save(String walletFile) {
		try {
			controller.saveWallet("demoWallet/" + walletFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void loadDemoWallet() {
		try {
			String seedCode = readFile("demoWallet/mnemonic_seed.msc");
			System.out.println(seedCode);
			String passphrase = "";
	        Long creationtime = System.currentTimeMillis();

	        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, creationtime);
			//Loading wallet from seed
			controller.setupWalletKit(seed, "demoWallet/");
			
			Wallet wallet = controller.getWallet();
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
