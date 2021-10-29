package MessageBulletinBoard.crypto;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class DiffieH {
    private KeyPairGenerator kPairGen;
    private KeyAgreement keyAgree;

    private KeyPair keyPair;

    private KeyFactory keyFac;
    private X509EncodedKeySpec x509KeySpec;
    //private PublicKey pubKey;
    //private SecretKeySpec aesKey = null;

    private String publicKeyString = null;



    private int counterDerivative;

    //private String ALGO = "AES/CBC/PKCS5Padding";

    private byte[] salt = null;

    private PublicKey publickey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret = null;

    String ALGO = "AES";

    public DiffieH() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        //this.generatePublicKey();
        generatePublicKey();
    }




    private void generatePublicKey() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(128);
            KeyPair kp = kpg.generateKeyPair();
            this.publickey = kp.getPublic();
            this.keyAgreement = KeyAgreement.getInstance("ECDH");
            this.keyAgreement.init(kp.getPrivate());

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void generateSecretKey(String publicKeyString) {
        try {
            byte[] byte_pubkey_other = Base64.getDecoder().decode(publicKeyString);
            KeyFactory factory = KeyFactory.getInstance("EC");
            PublicKey publickeyOther = factory.generatePublic(new X509EncodedKeySpec(byte_pubkey_other));

            keyAgreement.doPhase(publickeyOther, true);
            this.sharedsecret = keyAgreement.generateSecret();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String msg) {
        //byte[] decodedKey = Base64.getDecoder().decode(msg);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(this.sharedsecret, 16), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);
            byte[] cipherText = cipher.doFinal(msg.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while encrypting data", e);
        }
        /*try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(msg.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encVal);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;*/
    }

    public String decrypt(String encryptedData) {
        /*try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            //byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
            byte[] decordedValue = encryptedData.getBytes("UTF-8");
            byte[] decValue = c.doFinal(decordedValue);
            return Base64.getEncoder().encodeToString(decValue);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encryptedData;*/

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(this.sharedsecret, 16), "AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while decrypting data", e);
        }
    }

    public String getPubKey() {
        byte[] byte_pubkey = this.publickey.getEncoded();
        return Base64.getEncoder().encodeToString(byte_pubkey);
    }

    protected Key generateKey() {
        return new SecretKeySpec(sharedsecret, ALGO);
    }

    public boolean isSecurd(){
        return this.publickey != null && this.sharedsecret != null;
    }

    /*
    public String getPubKey(){
        return publicKeyString;
    }

    private void generatePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        this.kPairGen = KeyPairGenerator.getInstance("DH");
        this.kPairGen.initialize(512);
        KeyPair aliceKpair = kPairGen.generateKeyPair();

        //init keyagreement
        this.keyAgree = KeyAgreement.getInstance("DH");
        this.keyAgree.init(aliceKpair.getPrivate());

        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        this.publicKeyString = new String(alicePubKeyEnc);

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

    public boolean isSecurd(){
        return this.pubKey != null && this.aesKey != null;
    }*/

}
