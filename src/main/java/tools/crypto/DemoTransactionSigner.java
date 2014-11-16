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

    public DemoTransactionSigner() {
    }

    public DemoTransactionSigner(DeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
    }

    @Override
    protected SignatureAndKey getSignature(Sha256Hash sighash, List<ChildNumber> derivationPath) {
        ImmutableList<ChildNumber> keyPath = ImmutableList.copyOf(derivationPath);
        DeterministicKey key = keyChain.getKeyByPath(keyPath, true);
        return new SignatureAndKey(key.sign(sighash), key.getPubOnly());
    }
}
