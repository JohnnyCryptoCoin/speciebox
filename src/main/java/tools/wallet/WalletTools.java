package tools.wallet;

import java.security.SecureRandom;
import java.util.List;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.testing.KeyChainTransactionSigner;
import org.bitcoinj.testing.TestWithWallet;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.MarriedKeyChain;

import com.google.common.collect.Lists;

public class WalletTools extends TestWithWallet{
	
	//TestWithWalled only has basic blockstore. We need spv to work with tests
	protected SPVBlockStore spvblockstore;
	
	private void createMultiSigWallet(int threshold, int numKeys) throws BlockStoreException {
        createMultiSigWallet(threshold, numKeys, true);
    }

    
    private void createMultiSigWallet(int threshold, int numKeys, boolean addSigners) throws BlockStoreException {
        wallet = new Wallet(params);
        blockStore = new MemoryBlockStore(params);
        chain = new BlockChain(params, wallet, blockStore);

        List<DeterministicKey> followingKeys = Lists.newArrayList();
        for (int i = 0; i < numKeys - 1; i++) {
            final DeterministicKeyChain keyChain = new DeterministicKeyChain(new SecureRandom());
            DeterministicKey partnerKey = DeterministicKey.deserializeB58(null, keyChain.getWatchingKey().serializePubB58());
            followingKeys.add(partnerKey);
            if (addSigners && i < threshold - 1)
                wallet.addTransactionSigner(new KeyChainTransactionSigner(keyChain));
        }

        MarriedKeyChain chain = MarriedKeyChain.builder()
                .random(new SecureRandom())
                .followingKeys(followingKeys)
                .threshold(threshold).build();
        wallet.addAndActivateHDChain(chain);
    }

}
