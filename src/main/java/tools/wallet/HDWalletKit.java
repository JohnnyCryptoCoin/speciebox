/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.wallet;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.MarriedKeyChain;

import tools.crypto.DemoTransactionSigner;

import com.google.common.collect.Lists;

/**
 * Extention of bitcoinj's WalletAppKit for our own purposes.
 * 
 * Utility class that wraps the boilerplate needed to set up a new Simplified Payment Verification (SPV) bitcoinj app. 
 * Instantiate it with a directory and file prefix, optionally configure a few things, then use startAsync 
 * and optionally awaitRunning. The object will construct and configure a BlockChain, SPVBlockStore, Wallet and PeerGroup. 
 * 
 * This WalletKit will continue to be extended once we have a server environment, as opposed to only demos on localhost.
 * These files being stored will need design and security decisions made once the impact of their unintended release is
 * better understood. Let's not shoot ourselves in the foot from the getgo here. This SPV style of doing things is 
 * intended to provide a thin client application.
 * 
 * FROM bitcoingj's AbstractIdleService Docs:
 * 
 * Depending on the value of the blockingStartup property, startup will be considered complete once the block chain 
 * has fully synchronized, so it can take a while.
 *
 * To add listeners and modify the objects that are constructed, you can either do that by overriding the
 * onSetupCompleted() method (which will run on a background thread) and make your changes there,
 * or by waiting for the service to start and then accessing the objects from wherever you want. However, you cannot
 * access the objects this class creates until startup is complete.
 *
 */
public class HDWalletKit extends WalletAppKit {
	
	protected boolean RELOAD = false;
	private int walletThreshold;
	private boolean addSigners;
	private List<DeterministicKey> followingKeys;
	
	
	// As of Bitcoin Core 0.9 all multisig transactions which require more than 3 public keys are non-standard
	// meaning we might need to configure all our peers to deal with this and somehow wrap the transactions in 
	// a std one while it goes out into the world?
	
	//loads a wallet from file or creats a new one with no followingkeys and a threshold of 1
	public HDWalletKit(NetworkParameters params, File file, String fileName) {
		super(params, file, fileName);
		this.walletThreshold = 1;
		this.addSigners = true;
		this.followingKeys = null;
		this.RELOAD = true;
		this.followingKeys = new ArrayList<DeterministicKey>();
	}
    
    // creats a wallet that need Threshold of Keys (X out of Y) to spend
    HDWalletKit(NetworkParameters params, File directory, String filePrefix, int threshold, 
    			boolean addSigners, List<DeterministicKey> followingKeys) {
    	super(params, directory, filePrefix);
    	this.walletThreshold = threshold;
    	this.addSigners = addSigners;
    	
    	// once the wallet's creation is handled we are going to build a list of DeterministicKeys
        this.followingKeys = followingKeys;
    }

	// If true, the wallet will save itself to disk automatically whenever it changes.
    public HDWalletKit setAutoSave(boolean value) {
        checkState(state() == State.NEW, "Cannot call after startup");
        useAutoSave = value;
        return this;
    }
    
    public void addPairedWallet (DeterministicKey watchKey, boolean increaseThreshold){
    	System.out.println("This "+walletThreshold+"/"+followingKeys.size()+1);
    	followingKeys.add(watchKey);
    	
    	DeterministicKey partnerKey = DeterministicKey.deserializeB58(null, watchKey.serializePubB58());
		if(increaseThreshold){
			this.wallet().addTransactionSigner(new DemoTransactionSigner(watchKey));
		}
	
		MarriedKeyChain chain = MarriedKeyChain.builder()
				.random(new SecureRandom())
				.followingKeys(DeterministicKey.deserializeB58(null, watchKey.serializePubB58()))
				.threshold(walletThreshold).build();
		this.wallet().addAndActivateHDChain(chain);
    	
    }
    
    public int getThreshold(){
    	return walletThreshold;
    }
    
    public List<TransactionSigner> getSigners(){
    	return this.wallet().getTransactionSigners();
    }
    
    public List<DeterministicKey> getFollowingKeys(){
    	return this.followingKeys;
    }
    
    @Override
    protected void onSetupCompleted() {
    	if(RELOAD){
    		this.walletThreshold = wallet().getTransactionSigners().size()+1;
    		System.out.println("Wallet reload successful");
    	}
    	// in lieu of a logger...
    	System.out.println("Setup has now been completed");
    }

    // In our HD startup method we will create a number of following keys based on numberOfKeys
    @Override
    protected void startUp() throws Exception {
    	super.startUp();
    	
    	if(! RELOAD){
    		//is this really necessary??
    		List<DeterministicKey> followingB58Keys = new ArrayList<DeterministicKey>();
    		Iterator<DeterministicKey> it = followingKeys.iterator();
    		int i = 0;
    		while(it.hasNext()) {
    			DeterministicKey dKey = it.next();
    			//method that gets watching key for the new list of following keys. we can get the watchingkey of any Wallet though
    			DeterministicKey partnerKey = DeterministicKey.deserializeB58(null, dKey.serializePubB58());
    			followingB58Keys.add(partnerKey);
    			if(addSigners && i < walletThreshold - 1){
    				this.wallet().addTransactionSigner(new DemoTransactionSigner(dKey));
    				i++;
    			}
    		}
    		// This keychain keeps track of "following keychains" that follow the account key of this keychain.
    		// You can get P2SH addresses to receive coins to from this chain. The threshold (walletThreshold)
    		// specifies how many signatures required to spend transactions for this married keychain.
    		MarriedKeyChain chain = MarriedKeyChain.builder()
    				.random(new SecureRandom())
    				.followingKeys(followingB58Keys)
    				.threshold(walletThreshold).build();
    		this.wallet().addAndActivateHDChain(chain);
    		//We can create new wallets from each of these following keys, and they will start as part of the HD structure.
    	}

    }

    @Override
    protected void shutDown() throws Exception {
        // Runs in a separate thread.
        try {
            vPeerGroup.stopAsync();
            vPeerGroup.awaitTerminated();
            vWallet.saveToFile(vWalletFile);
            vStore.close();

            vPeerGroup = null;
            vWallet = null;
            vStore = null;
            vChain = null;
        } catch (BlockStoreException e) {
            throw new IOException(e);
        }
    }
}
