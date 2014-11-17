package tools.crypto;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.signers.CustomTransactionSigner;
import org.bitcoinj.wallet.DeterministicKeyChain;
import com.google.common.collect.ImmutableList;

import java.util.List;


//Transaction signer which uses provided keychain to get signing keys from. It relies on previous signer to provide
//derivation path to be used to get signing key and, once gets the key, just signs given transaction immediately.

public class DemoTransactionSigner extends CustomTransactionSigner {

    private DeterministicKeyChain keyChain;
    private DeterministicKey watchingKey;

    public DemoTransactionSigner() {
    }

    // Older style where we start up with a keychain. Not as desireable i think?
    public DemoTransactionSigner(DeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
    }
    
    public DemoTransactionSigner(DeterministicKey watchingKey) {
        this.watchingKey = watchingKey;
    }

    //The question to answer is Who am I? I have to find a way to get the signing key without relying on previous signer
    @Override
    protected SignatureAndKey getSignature(Sha256Hash sighash, List<ChildNumber> derivationPath) {
        ImmutableList<ChildNumber> keyPath = ImmutableList.copyOf(derivationPath);
        System.out.println("Transaction Sighash: "+sighash.toString());
        System.out.println("KeyPath: "+keyPath.get(0).toString());
        System.out.println("child numer: "+keyPath.get(0).getI());
        
        System.out.println("Manual SIgning Here??");
        DeterministicKey key = keyChain.getKeyByPath(keyPath, true);
        return new SignatureAndKey(key.sign(sighash), key.getPubOnly());
    }
}
