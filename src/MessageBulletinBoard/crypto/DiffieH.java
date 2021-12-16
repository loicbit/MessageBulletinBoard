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
import java.util.Random;

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

    SecureRandom randomSalt;
    byte[] salt;


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
            PublicKey publickeyOther = factory.generatePublic(new X509EncodedKeySpec(byte_pubkey_other));

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
            //deriveKey();

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
            //deriveKey();

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

    public void deriveKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        //KeySpec specs = new
        int keylength = 2^this.KEY_LENGTH;
        this.specs = new PBEKeySpec(this.sharedsecret.toString().toCharArray(), this.salt, 65536, 256);
        this.specs2 = new PBEKeySpec(this.sharedsecret.toString().toCharArray(), this.salt, 65536, 256);
        //SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        //KeySpec keySpec = new SecretKeySpec()
        byte[] key = kf.generateSecret(specs).getEncoded();
        byte[] key_aes = Arrays.copyOf(key, this.KEY_LENGTH);
        SecretKey secret = new SecretKeySpec(key_aes, "AES");
        this.originalKey = secret;
        //this.originalKey = kf.generateSecret(specs);



        //this.originalKey = SecretKeyFactory.getInstance("AES").generateSecret();
        // as salt use random with init salt
        /*byte[] salt = generateSalt();
        KeySpec spec = new PBEKeySpec(this.sharedsecret.toString().toCharArray(), salt, ITERATIONS, 128);
        SecretKey secret = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded(), "AES");

        this.sharedAESsecret = secret.getEncoded();*/
        //SecretKeyFactory factory = null;
        /*
        SecretKeyFactory factory = null;

        try {
            factory = SecretKeyFactory.getInstance(KEYGEN_SPEC);
        } catch (NoSuchAlgorithmException impossible) {
            //return null;
        }
        // derive a longer key, then split into AES key and authentication key

        KeySpec spec = new PBEKeySpec(this.sharedAESsecret.toString().toCharArray(), salt, ITERATIONS, 128);
        SecretKey tmp = null;

        try {
            tmp = factory.generateSecret(spec);
        } catch (InvalidKeySpecException impossible) {
        }



        this.sharedAESsecret = tmp.getEncoded();*/


    }

    public boolean isSecurd(){
        return this.publickey != null && this.sharedsecret != null;
    }
}
