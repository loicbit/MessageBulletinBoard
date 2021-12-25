package MessageBulletinBoard.crypto;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class DiffieH {
    //private KeyPairGenerator kPairGen;
    //private KeyAgreement keyAgree;

    //private KeyPair keyPair;

    //private KeyFactory keyFac;
    //private X509EncodedKeySpec x509KeySpec;

    //private PublicKey pubKey;
    //private SecretKeySpec aesKey = null;

    private String publicKeyString = null;



    private int counterDerivative;

    //private String ALGO = "AES/CBC/PKCS5Padding";

    private PublicKey publickey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret = null;
    byte[] sharedAESsecret = null;
    private SecretKey originalKey = null;
    private KeySpec specs = null;
    private KeySpec specs2 = null;

    String ALGO = "AES";
    private String KEYGEN_SPEC = "PBKDF2WithHmacSHA1";
    private static final int SALT_LENGTH = 16; // in bytes
    private static final int ITERATIONS = 32768;
    private static final int KEY_LENGTH = 16;

    private String KEYFACT_INST = "EC";

    public byte[] secretKeyPBE;

    SecureRandom randomSalt;
    byte[] salt;

    private PublicKey publickeyOther;

    public DiffieH() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        //this.generatePublicKey();
        generatePublicKey();

        this.randomSalt = new SecureRandom();
        this.randomSalt.setSeed(12345);
    }

    private void generatePublicKey() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(this.KEYFACT_INST);
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
            KeyFactory factory = KeyFactory.getInstance(this.KEYFACT_INST);
            this.publickeyOther = factory.generatePublic(new X509EncodedKeySpec(byte_pubkey_other));

            keyAgreement.doPhase(publickeyOther, true);
            this.sharedsecret = keyAgreement.generateSecret();
            this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, this.KEY_LENGTH);
            this.originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
            //this.secretKeyAES = new SecretKeySpec(this.sharedAESsecret, "AES");
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

            cipher.init(Cipher.ENCRYPT_MODE, this.originalKey);
            byte[] cipherText = cipher.doFinal(msg.getBytes("UTF-8"));
            this.deriveKey();

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            //SecretKey originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
            cipher.init(Cipher.DECRYPT_MODE, this.originalKey);
            byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            this.deriveKey();

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

    /*public void setRandomSeed(byte[] seedSalt){
        this.randomSalt = new SecureRandom(seedSalt);
    }*/

    public void setRandomSeed(){
        //this.randomSalt = new SecureRandom(seedSalt);
    }

    public void setSalt(byte[] salt){
        this.salt = salt;
    }
    /*private byte[] generateRandomSeed(){
        //return getSecureRandomSeed();
    }*/

    private byte[] generateSalt(){
        byte[] saltTemp = new byte[this.SALT_LENGTH];
        this.randomSalt.nextBytes(saltTemp);
        return saltTemp;
    }

    public void deriveKey() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, ShortBufferException {
        // todo: add random salt based on a securrandom
        // todo: bigger key

        MessageDigest keyHash = MessageDigest.getInstance("SHA-256");
        keyHash.update(this.sharedAESsecret);

        // Sort to have the same order for A and B
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(this.publickey.getEncoded()), ByteBuffer.wrap(this.publickeyOther.getEncoded()));
        Collections.sort(keys);

        keyHash.update(keys.get(0));
        keyHash.update(keys.get(1));

        this.sharedsecret = keyHash.digest();
        this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, this.KEY_LENGTH);
        this.originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
    }

    public boolean isSecurd(){
        return this.publickey != null && this.sharedsecret != null;
    }
}
