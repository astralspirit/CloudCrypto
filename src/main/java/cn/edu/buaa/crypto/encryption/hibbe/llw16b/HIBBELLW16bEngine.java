package cn.edu.buaa.crypto.encryption.hibbe.llw16b;

import cn.edu.buaa.crypto.algebra.generators.PairingKeyPairGenerator;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerPair;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerParameter;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.HIBBEEngine;
import cn.edu.buaa.crypto.encryption.hibbe.genparams.*;
import cn.edu.buaa.crypto.encryption.hibbe.llw16a.serparams.HIBBELLW16aMasterSecretKeySerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.generators.HIBBELLW16bDecryptionGenerator;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.generators.HIBBELLW16bEncryptionGenerator;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.generators.HIBBELLW16bKeyPairGenerator;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.generators.HIBBELLW16bSecretKeyGenerator;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.serparams.HIBBELLW16bCiphertextSerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.serparams.HIBBELLW16bMasterSecretKeySerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.serparams.HIBBELLW16bPublicKeySerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16b.serparams.HIBBELLW16bSecretKeySerParameter;
import cn.edu.buaa.crypto.signature.pks.PairingDigestSigner;
import cn.edu.buaa.crypto.signature.pks.bb08.BB08SignKeyPairGenerationParameter;
import cn.edu.buaa.crypto.signature.pks.bb08.BB08SignKeyPairGenerator;
import cn.edu.buaa.crypto.signature.pks.bb08.BB08Signer;
import cn.edu.buaa.crypto.utils.PairingUtils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;

/**
 * Created by Weiran Liu on 2016/11/10.
 *
 * Liu-Liu-Wu prime-order CCA2-secure HIBBE engine.
 */
public class HIBBELLW16bEngine implements HIBBEEngine {
    //Scheme name, used for exceptions
    public static final String SCHEME_NAME = "Liu-Liu-Wu-16 CCA2-secure prime-order HIBBE";

    private static HIBBELLW16bEngine engine;
    private Signer signer = new PairingDigestSigner(new BB08Signer(), new SHA256Digest());
    private PairingKeyPairGenerator signKeyPairGenerator = new BB08SignKeyPairGenerator();

    public String getEngineName() {
        return SCHEME_NAME;
    }

    public static HIBBELLW16bEngine getInstance() {
        if (engine == null) {
            engine = new HIBBELLW16bEngine();
        }
        return engine;
    }

    private HIBBELLW16bEngine() {
        this.signKeyPairGenerator.init(
                new BB08SignKeyPairGenerationParameter(PairingFactory.getPairingParameters(PairingUtils.PATH_a_160_512)));
    }

    public void setSigner(Signer signer, PairingKeyPairGenerator signKeyPairGenerator, KeyGenerationParameters signKeyPairGenerationParameter) {
        this.signer = signer;
        this.signKeyPairGenerator = signKeyPairGenerator;
        this.signKeyPairGenerator.init(signKeyPairGenerationParameter);
    }

    public PairingKeySerPair setup(PairingParameters pairingParameters, int maxUser) {
        HIBBELLW16bKeyPairGenerator keyPairGenerator = new HIBBELLW16bKeyPairGenerator();
        HIBBEKeyPairGenerationParameter keyPairGenerationParameter = new HIBBEKeyPairGenerationParameter(pairingParameters, maxUser);
        keyPairGenerationParameter.setSigner(signer);
        keyPairGenerator.init(keyPairGenerationParameter);

        return keyPairGenerator.generateKeyPair();
    }

    public PairingKeySerParameter keyGen(PairingKeySerParameter publicKey, PairingKeySerParameter masterKey, String[] ids) {
        if (!(publicKey instanceof HIBBELLW16bPublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + publicKey.getClass().getName() + ", require "
                            + HIBBELLW16bPublicKeySerParameter.class.getName());
        }
        if (!(masterKey instanceof HIBBELLW16bMasterSecretKeySerParameter)) {
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + masterKey.getClass().getName() + ", require"
                            + HIBBELLW16aMasterSecretKeySerParameter.class.getName());
        }
        HIBBELLW16bSecretKeyGenerator secretKeyGenerator = new HIBBELLW16bSecretKeyGenerator();
        secretKeyGenerator.init(new HIBBESecretKeyGenerationParameter(
                publicKey, masterKey, ids));

        return secretKeyGenerator.generateKey();
    }

    public PairingKeySerParameter delegate(PairingKeySerParameter publicKey, PairingKeySerParameter secretKey, int index, String id) {
        if (!(publicKey instanceof HIBBELLW16bPublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + publicKey.getClass().getName() + ", require "
                            + HIBBELLW16bPublicKeySerParameter.class.getName());
        }
        if (!(secretKey instanceof HIBBELLW16bSecretKeySerParameter)) {
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + secretKey.getClass().getName() + ", require"
                            + HIBBELLW16bSecretKeySerParameter.class.getName());
        }
        HIBBELLW16bSecretKeyGenerator secretKeyGenerator = new HIBBELLW16bSecretKeyGenerator();
        secretKeyGenerator.init(new HIBBEDelegateGenerationParameter(
                publicKey, secretKey, index, id));

        return secretKeyGenerator.generateKey();
    }

    public PairingCipherSerParameter encryption(PairingKeySerParameter publicKey, String[] ids, Element message){
        if (!(publicKey instanceof HIBBELLW16bPublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + publicKey.getClass().getName() + ", require "
                            + HIBBELLW16bPublicKeySerParameter.class.getName());
        }
        HIBBELLW16bEncryptionGenerator encryptionGenerator = new HIBBELLW16bEncryptionGenerator();
        HIBBEEncryptionGenerationParameter encryptionGenerationParameter = new HIBBEEncryptionGenerationParameter(publicKey, ids, message);
        encryptionGenerationParameter.setSigner(this.signer, this.signKeyPairGenerator);
        encryptionGenerator.init(encryptionGenerationParameter);
        return encryptionGenerator.generateCiphertext();
    }

    public Element decryption(PairingKeySerParameter publicKey, PairingKeySerParameter secretKey, String[] ids, PairingCipherSerParameter ciphertext)
            throws InvalidCipherTextException {
        if (!(publicKey instanceof HIBBELLW16bPublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + publicKey.getClass().getName() + ", require "
                            + HIBBELLW16bPublicKeySerParameter.class.getName());
        }
        if (!(secretKey instanceof HIBBELLW16bSecretKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + secretKey.getClass().getName() + ", require "
                            + HIBBELLW16bSecretKeySerParameter.class.getName());
        }
        if (!(ciphertext instanceof HIBBELLW16bCiphertextSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance, find "
                            + ciphertext.getClass().getName() + ", require "
                            + HIBBELLW16bCiphertextSerParameter.class.getName());
        }
        HIBBELLW16bDecryptionGenerator decryptionGenerator = new HIBBELLW16bDecryptionGenerator();
        HIBBEDecryptionGenerationParameter decryptionGenerationParameter =
                new HIBBEDecryptionGenerationParameter(publicKey, secretKey, ids, ciphertext);
        decryptionGenerationParameter.setSigner(this.signer);
        decryptionGenerator.init(decryptionGenerationParameter);

        return decryptionGenerator.recoverMessage();
    }
}
