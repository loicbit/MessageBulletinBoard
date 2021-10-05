package MessageBulletinBoard.crypto;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DiffieH {
    private KeyPairGenerator kPairGen;
    private KeyAgreement keyAgree;

    private KeyPair keyPair;

    private KeyFactory keyFac;
    private X509EncodedKeySpec x509KeySpec;
    private PublicKey pubKey;
    private SecretKeySpec aesKey;

    private String publicKeyString;

    private int counterDerivative;

    private String ALGO = "AES/CBC/PKCS5Padding";

    private byte[] salt = null;

    public DiffieH() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        this.generatePublicKey();
    }

    public String getPubKey(){
        return publicKeyString;
    }

    private void generatePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        this.kPairGen = KeyPairGenerator.getInstance("DH");
        this.kPairGen.initialize(512);
        KeyPair aliceKpair = kPairGen.generateKeyPair();

        //init keyagreement
        keyAgree = KeyAgreement.getInstance("DH");
        keyAgree.init(aliceKpair.getPrivate());

        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        publicKeyString = new String(alicePubKeyEnc);

        this.keyFac = KeyFactory.getInstance("DH");
        this.x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);

        this.pubKey = this.keyFac.generatePublic(this.x509KeySpec);
    }

    public void generateSecretKey(String pubKeyOtherString, Boolean firstTime) {
        if(firstTime) {
            this.x509KeySpec = new X509EncodedKeySpec(pubKeyOtherString.getBytes());

            PublicKey pubKeyOther = null;
            try {
                pubKeyOther = this.keyFac.generatePublic(this.x509KeySpec);
                this.keyAgree.doPhase(pubKeyOther, true);

            } catch (InvalidKeyException | InvalidKeySpecException e) {
                e.printStackTrace();
            }

            byte[] sharedSecret = this.keyAgree.generateSecret();

            this.aesKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
        }else{
            this.encryptDeriveKey();
        }
    }

    public byte[] encrypt(byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, this.aesKey, new IvParameterSpec(new byte[16]));

        return cipher.doFinal(message);
    }

    public byte[] decrypt(byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, this.aesKey, new IvParameterSpec(new byte[16]));
        return cipher.doFinal(message);
    }

    private byte[] deriveKey(String password, int keyLen) {
        SecretKeyFactory kf = null;
        SecretKey key = null;
        try {
            kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec specs = new PBEKeySpec(password.toCharArray(), salt, 1024, keyLen);
            key = kf.generateSecret(specs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key.getEncoded();
    }

    public void encryptDeriveKey() {
        Cipher cipher = null;
        byte[] newKey = null;

        try{
            byte[] data = deriveKey(Integer.toString(this.counterDerivative), 192);
            SecretKey desKey = SecretKeyFactory.getInstance("AES").generateSecret(new DESedeKeySpec(data));
            cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, desKey);
            newKey = cipher.doFinal(this.aesKey.getEncoded());
            this.counterDerivative++;
            this.aesKey = new SecretKeySpec(newKey, "AES");
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

}
