package cn.edu.buaa.crypto.encryption.hibe.bbg05.serparams;

import cn.edu.buaa.crypto.utils.PairingUtils;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;

/**
 * Created by Weiran Liu on 2015/11/3.
 *
 * Ciphertext parameters for Boneh-Boyen-Goh HIBE.
 */
public class HIBEBBG05CipherSerParameter extends PairingCipherSerParameter {
    private final int length;

    private transient Element B;
    private final byte[] byteArrayB;

    private transient Element C;
    private final byte[] byteArrayC;

    public HIBEBBG05CipherSerParameter(PairingParameters pairingParameters, int length, Element B, Element C) {
        super(pairingParameters);
        this.length = length;

        this.B = B.getImmutable();
        this.byteArrayB = this.B.toBytes();

        this.C = C.getImmutable();
        this.byteArrayC = this.C.toBytes();
    }

    public int getLength() { return this.length; }

    public Element getB() { return this.B.duplicate(); }

    public Element getC() { return this.C.duplicate(); }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof HIBEBBG05CipherSerParameter) {
            HIBEBBG05CipherSerParameter that = (HIBEBBG05CipherSerParameter)anObject;
            //Compare length
            if (this.length != that.getLength()) {
                return false;
            }
            //Compare B
            if (!PairingUtils.isEqualElement(this.B, that.getB())){
                return false;
            }
            if (!Arrays.equals(this.byteArrayB, that.byteArrayB)) {
                return false;
            }
            //Compare C
            if (!PairingUtils.isEqualElement(this.C, that.getC())){
                return false;
            }
            if (!Arrays.equals(this.byteArrayC, that.byteArrayC)) {
                return false;
            }
            //Compare Pairing Parameters
            return this.getParameters().toString().equals(that.getParameters().toString());
        }
        return false;
    }

    private void readObject(java.io.ObjectInputStream objectInputStream)
            throws java.io.IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        Pairing pairing = PairingFactory.getPairing(this.getParameters());
        this.B = pairing.getG1().newElementFromBytes(this.byteArrayB).getImmutable();
        this.C = pairing.getG1().newElementFromBytes(this.byteArrayC).getImmutable();
    }
}