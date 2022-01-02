package MessageBulletinBoard.crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class DiffieH {
    //private KeyPairGenerator kPairGen;
    //private KeyAgreement keyAgree;

    //private KeyPair keyPair;

    //private KeyFactory keyFac;
    //private X509EncodedKeySpec x509KeySpec;

    //private PublicKey pubKey;
    //private SecretKeySpec aesKey = null;

    private final String publicKeyString = null;



    private int counterDerivative;

    //private String ALGO = "AES/CBC/PKCS5Padding";

    private PublicKey publickey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret = null;
    byte[] sharedAESsecret = null;
    private SecretKey originalKey = null;
    private final KeySpec specs = null;
    private final KeySpec specs2 = null;

    String ALGO = "AES";
    private final String KEYGEN_SPEC = "PBKDF2WithHmacSHA1";
    private static final int SALT_LENGTH = 16; // in bytes
    private static final int ITERATIONS = 32768;
    private static final int KEY_LENGTH = 16;

    private final String KEYFACT_INST = "EC";

    public byte[] secretKeyPBE;

    private final Random randomSeedGenerator;
    private Random randomByteGenerator;
    private int seed;
    private final int LENGTH_RANDOM_BYTES = 12;

    private PublicKey publickeyOther;

    private boolean kdfEnabled = false;

    ByteArrayOutputStream bos;
    ObjectOutputStream out;

    public DiffieH(boolean kdf) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        //todo: Generate seed and replace if necessary
        this.kdfEnabled = kdf;

        generatePublicKey();

        this.randomSeedGenerator = new Random();
        this.seed = this.randomSeedGenerator.nextInt();

        this.randomByteGenerator = new Random();
        this.randomByteGenerator.setSeed(this.seed);

        ByteArrayOutputStream bos = null;
        ObjectOutputStream out = null;
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
            this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, KEY_LENGTH);
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

    public void generateSecretKeyByte(byte[] publicKeyString) {
        try {
            KeyFactory factory = KeyFactory.getInstance(this.KEYFACT_INST);
            this.publickeyOther = factory.generatePublic(new X509EncodedKeySpec(publicKeyString));

            keyAgreement.doPhase(publickeyOther, true);
            this.sharedsecret = keyAgreement.generateSecret();
            this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, KEY_LENGTH);
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

    public void generateSecretKeyObject(PublicKey publicKey) {
        try {
            this.publickeyOther = publicKey;

            keyAgreement.doPhase(publickeyOther, true);
            this.sharedsecret = keyAgreement.generateSecret();
            this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, KEY_LENGTH);
            this.originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
            //this.secretKeyAES = new SecretKeySpec(this.sharedAESsecret, "AES");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String msg) {
        //byte[] decodedKey = Base64.getDecoder().decode(msg);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec

            cipher.init(Cipher.ENCRYPT_MODE, this.originalKey);
            byte[] cipherText = cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));

            if(this.kdfEnabled) this.deriveKey();

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while encrypting data", e);
        }
    }

    public byte[] encryptBytes(byte[] msg) {
        //byte[] decodedKey = Base64.getDecoder().decode(msg);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec

            cipher.init(Cipher.ENCRYPT_MODE, this.originalKey);
            byte[] cipherText = cipher.doFinal(msg);

            if(this.kdfEnabled) this.deriveKey();

            return cipherText;
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

            if(this.kdfEnabled) this.deriveKey();

            return new String(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while decrypting data", e);
        }
    }

    public byte[] decryptBytes(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            //SecretKey originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
            cipher.init(Cipher.DECRYPT_MODE, this.originalKey);
            byte[] cipherText = cipher.doFinal(encryptedData);

            if(this.kdfEnabled) this.deriveKey();

            return cipherText;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while decrypting data", e);
        }
    }

    public String getPubKey() {
        byte[] byte_pubkey = this.publickey.getEncoded();
        return Base64.getEncoder().encodeToString(byte_pubkey);
    }

    public PublicKey getPubkeyObject(){
        return this.publickey;
    }

    public byte[] getPubKeyByte() {
        return this.publickey.getEncoded();
    }

    public int getSeed(){
        return this.seed;
    }

    public void setSeed(int seed){
       this.seed = seed;

       this.randomByteGenerator = new Random();
       this.randomByteGenerator.setSeed(seed);
    }

    private byte[] generatRandomBytes(){
        byte[] saltTemp = new byte[this.LENGTH_RANDOM_BYTES];
        this.randomByteGenerator.nextBytes(saltTemp);
        return saltTemp;
    }

    private void deriveKey() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, ShortBufferException {
        MessageDigest keyHash = MessageDigest.getInstance("SHA-256");
        keyHash.update(this.sharedAESsecret);

        byte[] randomSeed = this.generatRandomBytes();

        // Sort to have the same order for A and B
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(this.publickey.getEncoded()), ByteBuffer.wrap(this.publickeyOther.getEncoded()), ByteBuffer.wrap(randomSeed));
        Collections.sort(keys);

        keyHash.update(keys.get(0));
        keyHash.update(keys.get(1));
        keyHash.update(keys.get(2));

        this.sharedsecret = keyHash.digest();
        this.sharedAESsecret = Arrays.copyOf(this.sharedsecret, KEY_LENGTH);
        this.originalKey = new SecretKeySpec(this.sharedAESsecret, "AES");
    }

    public SecretKey getSharedKey(){
        return this.originalKey;
    }
    public boolean isSecurd(){
        return this.publickey != null && this.sharedsecret != null;
    }


}
