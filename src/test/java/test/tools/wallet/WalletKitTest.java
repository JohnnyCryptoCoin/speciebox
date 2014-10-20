package test.tools.wallet;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Before;
import org.mockito.Mock;

import tools.wallet.WalletKit;

public class WalletKitTest {
	private WalletKit walletKit;
	
	@Mock NetworkParameters params;
	@Mock File directory;
	
	@Before
	public void setup (){
		this.walletKit = new WalletKit(params, directory, "sBox-test");
	}
}
