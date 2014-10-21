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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

import org.apache.commons.lang.NullArgumentException;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.protocols.channels.StoredPaymentChannelClientStates;
import org.bitcoinj.protocols.channels.StoredPaymentChannelServerStates;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.WalletProtobufSerializer;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
public class WalletKit extends AbstractIdleService {
    protected static final Logger log = LoggerFactory.getLogger(WalletKit.class);

    protected final String filePrefix;
    protected final NetworkParameters params;
    protected volatile BlockChain vChain;
    protected volatile SPVBlockStore vStore;
    protected volatile Wallet vWallet;
    protected volatile PeerGroup vPeerGroup;

    protected final File directory;
    protected volatile File vWalletFile;

    protected boolean useAutoSave = true;
    protected PeerAddress[] peerAddresses;
    protected PeerEventListener downloadListener;
    protected boolean autoStop = true;
    protected InputStream checkpoints;
    protected boolean blockingStartup = true;
    protected String userAgent, version;
    protected WalletProtobufSerializer.WalletFactory walletFactory;
    @Nullable protected DeterministicSeed restoreFromSeed;

    public WalletKit(NetworkParameters params, File directory, String filePrefix) {
    	if (params.equals(null) || directory.equals(null) || filePrefix.equals(null)){
    		throw new NullArgumentException("NO ARGUMENTS CAN BE NULL");
    	}
        this.params = params;
        this.directory = directory;
        this.filePrefix = filePrefix;
        //TODO: We can check for what runtime environment we are in for customization
        InputStream stream = WalletKit.class.getResourceAsStream("/" + params.getId() + ".checkpoints");
        if (stream != null){
            setCheckpoints(stream);
        }
    }

    /** Will only connect to the given addresses. Cannot be called after startup. */
    public WalletKit setPeerNodes(PeerAddress... addresses) {
        checkState(state() == State.NEW, "Cannot call after startup");
        this.peerAddresses = addresses;
        return this;
    }

    /** Will only connect to localhost. Cannot be called after startup. */
    public WalletKit connectToLocalHost() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return setPeerNodes(new PeerAddress(localHost, params.getPort()));
        } catch (UnknownHostException e) {
            // Borked machine with no loopback adapter configured properly.
            throw new RuntimeException(e);
        }
    }

    /** If true, the wallet will save itself to disk automatically whenever it changes. */
    public WalletKit setAutoSave(boolean value) {
        checkState(state() == State.NEW, "Cannot call after startup");
        useAutoSave = value;
        return this;
    }

    /**
     * If you want to learn about the sync process, you can provide a listener here. 
     * For instance, a DownloadListener is a good choice.
     */
    public WalletKit setDownloadListener(PeerEventListener listener) {
        this.downloadListener = listener;
        return this;
    }

    /** If true, will register a shutdown hook to stop the library. Defaults to true. */
    public WalletKit setAutoStop(boolean autoStop) {
        this.autoStop = autoStop;
        return this;
    }

    /**
     * If set, the file is expected to contain a checkpoints file calculated with BuildCheckpoints. It makes initial
     * block sync faster for new users - refer to the documentation on the bitcoinj website for further details.
     */
    public WalletKit setCheckpoints(InputStream checkpoints) {
        if (this.checkpoints != null){
            Utils.closeUnchecked(this.checkpoints);
        }
        this.checkpoints = checkNotNull(checkpoints);
        return this;
    }

    /**
     * If true (the default) then the startup of this service won't be considered complete until the network has been
     * brought up, peer connections established and the block chain synchronised. Therefore startAndWait() can
     * potentially take a very long time. If false, then startup is considered complete once the network activity
     * begins and peer connections/block chain sync will continue in the background.
     */
    public WalletKit setBlockingStartup(boolean blockingStartup) {
        this.blockingStartup = blockingStartup;
        return this;
    }

    /**
     * Sets the string that will appear in the subver field of the version message.
     * Should be SpecieBox and the Version Number. These fields will probably come from the DB via web
     */
    public WalletKit setUserAgent(String userAgent, String version) {
        this.userAgent = checkNotNull(userAgent);
        this.version = checkNotNull(version);
        return this;
    }

    /**
     * If called, then an embedded Tor client library will be used to connect to the P2P network. 
     * The user does not need any additional software for this. 
     * As of April 2014 this mode is experimental in bitcoinj library, implications are not fully understood.
     */
//	  public boolean useTor = false; //True if we want to. Could add a LOT of security, but might be too high of cost
//    public WalletKit useTor() {
//        this.useTor = true;
//        return this;
//    }

    /**
     * If a seed is set here then any existing wallet that matches the file name will be 
     * renamed to a backup name, the chain file will be deleted, and the wallet object will 
     * be instantiated with the given seed instead of a fresh one being created.
     */
    public WalletKit restoreWalletFromSeed(DeterministicSeed seed) {
        this.restoreFromSeed = seed;
        return this;
    }

    /**
     * When this is called, chain(), store(), and peerGroup() will return the created objects, 
     * however they are not initialized. 
     * Be careful using this, it is included at the recommendation of bitcoinj community.
     */
    protected List<WalletExtension> provideWalletExtensions() throws Exception {
        return ImmutableList.of();
    }

    /**
     * This method is invoked on a background thread after all objects are initialised, but before the peer group
     * or block chain download is started. You can tweak the objects configuration here.
     */
    protected void onSetupCompleted() {
    	// in lieu of a logger...
    	System.out.println("Setup has been completed");
    	
    	//if we choose to store logs on user devices...
    	log.info("Setup has been completed");
    }

    /**
     * From bitcoinj's AppKit. Inteded to run on localhost for now. Eventually new files will be placed on the device
     * or system of user while the app is running. 
     * 
     * Tests to see if the spvchain file has an operating system file lock on it. Useful for checking if your app
     * is already running. If another copy of your app is running and you start the appkit anyway, an exception will
     * be thrown during the startup process. Returns false if the chain file does not exist.
     */
    public boolean isChainFileLocked() throws IOException {
        RandomAccessFile file2 = null;
        try {
            File file = new File(directory, filePrefix + ".spvchain");
            if (!file.exists())
                return false;
            file2 = new RandomAccessFile(file, "rw");
            FileLock lock = file2.getChannel().tryLock();
            if (lock == null)
                return true;
            lock.release();
            return false;
        } finally {
            if (file2 != null)
                file2.close();
        }
    }

    
    /**
     * The Startup method itself.
     * 
     * 1) New thread to make all the directories we will need, basically an ensureCreated
     */
    @Override
    protected void startUp() throws Exception {
        // Runs in a separate thread.
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create directory " + directory.getAbsolutePath());
            }
        }
        log.info("Starting up with directory = {}", directory);
        try {
            File chainFile = new File(directory, filePrefix + ".spvchain");
            boolean chainFileExists = chainFile.exists();
            vWalletFile = new File(directory, filePrefix + ".wallet");
            // New wallet or set up an existing one from a seed?
            boolean shouldReplayWallet = (vWalletFile.exists() && !chainFileExists) || restoreFromSeed != null;
            vWallet = createOrLoadWallet(shouldReplayWallet);

            // Initiate Bitcoin network objects (block store, blockchain and peer group).
            // Part of a bitcoinj method.
            vStore = new SPVBlockStore(params, chainFile);
            if ((!chainFileExists || restoreFromSeed != null) && checkpoints != null) {
                // Initialize the chain file with a checkpoint to speed up first-run sync.
                long time;
                if (restoreFromSeed != null) {
                    time = restoreFromSeed.getCreationTimeSeconds();
                    if (chainFileExists) {
                        log.info("Deleting the chain file in preparation from restore.");
                        vStore.close();
                        if (!chainFile.delete())
                            throw new Exception("Failed to delete chain file in preparation for restore.");
                        vStore = new SPVBlockStore(params, chainFile);
                    }
                } else {
                    time = vWallet.getEarliestKeyCreationTime();
                }
                CheckpointManager.checkpoint(params, checkpoints, vStore, time);
            }
            vChain = new BlockChain(params, vStore);
            vPeerGroup = createPeerGroup();
            if (this.userAgent != null)
                vPeerGroup.setUserAgent(userAgent, version);

            // Set up peer addresses or discovery first, so if wallet extensions try to broadcast a transaction
            // before we're actually connected the broadcast waits for an appropriate number of connections.
            if (peerAddresses != null) {
                for (PeerAddress addr : peerAddresses) vPeerGroup.addAddress(addr);
                vPeerGroup.setMaxConnections(peerAddresses.length);
                peerAddresses = null;
            } else {
                vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
            }
            vChain.addWallet(vWallet);
            vPeerGroup.addWallet(vWallet);
            onSetupCompleted();
            
            // bitcoinj's recommended method for retrieving and playing the correct blockchain.
            if (blockingStartup) {
                vPeerGroup.startAsync();
                vPeerGroup.awaitRunning();
                // Make sure we shut down cleanly.
                installShutdownHook();
                completeExtensionInitiations(vPeerGroup);

                // TODO: Be able to use the provided download listener when doing a blocking startup.
                final DownloadListener listener = new DownloadListener();
                vPeerGroup.startBlockChainDownload(listener);
                listener.await();
            } else {
                vPeerGroup.startAsync();
                vPeerGroup.addListener(new Service.Listener() {
                    @Override
                    public void running() {
                        completeExtensionInitiations(vPeerGroup);
                        final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
                        vPeerGroup.startBlockChainDownload(l);
                    }

                    @Override
                    public void failed(State from, Throwable failure) {
                        throw new RuntimeException(failure);
                    }
                }, MoreExecutors.sameThreadExecutor());
            }
        } catch (BlockStoreException e) {
            throw new IOException(e);
        }
    }

    private Wallet createOrLoadWallet(boolean shouldReplayWallet) throws Exception {
        Wallet wallet;

        maybeMoveOldWalletOutOfTheWay();

        if (vWalletFile.exists()) {
            wallet = loadWallet(shouldReplayWallet);
        } else {
            wallet = createWallet();
            wallet.freshReceiveKey();
            for (WalletExtension e : provideWalletExtensions()) {
                wallet.addExtension(e);
            }
            wallet.saveToFile(vWalletFile);
        }

        if (useAutoSave) wallet.autosaveToFile(vWalletFile, 200, TimeUnit.MILLISECONDS, null);

        return wallet;
    }

    private Wallet loadWallet(boolean shouldReplayWallet) throws Exception {
        Wallet wallet;
        FileInputStream walletStream = new FileInputStream(vWalletFile);
        try {
            List<WalletExtension> extensions = provideWalletExtensions();
            wallet = new Wallet(params);
            WalletExtension[] extArray = extensions.toArray(new WalletExtension[extensions.size()]);
            Protos.Wallet proto = WalletProtobufSerializer.parseToProto(walletStream);
            final WalletProtobufSerializer serializer;
            if (walletFactory != null)
                serializer = new WalletProtobufSerializer(walletFactory);
            else
                serializer = new WalletProtobufSerializer();
            wallet = serializer.readWallet(params, extArray, proto);
            if (shouldReplayWallet)
                wallet.clearTransactions(0);
        } finally {
            walletStream.close();
        }
        return wallet;
    }

    protected Wallet createWallet() {
        KeyChainGroup kcg;
        if (restoreFromSeed != null)
            kcg = new KeyChainGroup(params, restoreFromSeed);
        else
            kcg = new KeyChainGroup(params);
        if (walletFactory != null) {
            return walletFactory.create(params, kcg);
        } else {
            return new Wallet(params, kcg);  // default
        }
    }

    private void maybeMoveOldWalletOutOfTheWay() {
        if (restoreFromSeed == null) return;
        if (!vWalletFile.exists()) return;
        int counter = 1;
        File newName;
        do {
            newName = new File(vWalletFile.getParent(), "Backup " + counter + " for " + vWalletFile.getName());
            counter++;
        } while (newName.exists());
        log.info("Renaming old wallet file {} to {}", vWalletFile, newName);
        if (!vWalletFile.renameTo(newName)) {
            // This should not happen unless something is really messed up.
            throw new RuntimeException("Failed to rename wallet for restore");
        }
    }

    /*
     * As soon as the transaction broadcaster han been created we will pass it to the
     * payment channel extensions
     */
    private void completeExtensionInitiations(TransactionBroadcaster transactionBroadcaster) {
        StoredPaymentChannelClientStates clientStoredChannels = (StoredPaymentChannelClientStates)
                vWallet.getExtensions().get(StoredPaymentChannelClientStates.class.getName());
        if(clientStoredChannels != null) {
            clientStoredChannels.setTransactionBroadcaster(transactionBroadcaster);
        }
        StoredPaymentChannelServerStates serverStoredChannels = (StoredPaymentChannelServerStates)
                vWallet.getExtensions().get(StoredPaymentChannelServerStates.class.getName());
        if(serverStoredChannels != null) {
            serverStoredChannels.setTransactionBroadcaster(transactionBroadcaster);
        }
    }



    protected PeerGroup createPeerGroup() throws TimeoutException {
        //Eventually we could create a TOR peergroup here if we wanted
        return new PeerGroup(params, vChain);
    }

    /** bitcoinj's own shutdown hook for the app threads. */
    private void installShutdownHook() {
        if (autoStop) Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                try {
                    WalletKit.this.stopAsync();
                    WalletKit.this.awaitTerminated();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
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

    public NetworkParameters params() {
        return params;
    }

    public BlockChain chain() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vChain;
    }

    public SPVBlockStore store() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vStore;
    }

    public Wallet wallet() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vWallet;
    }

    public PeerGroup peerGroup() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vPeerGroup;
    }

    public File directory() {
        return directory;
    }
}
