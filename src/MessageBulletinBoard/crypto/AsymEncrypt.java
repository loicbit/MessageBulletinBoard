package MessageBulletinBoard.crypto;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.Cipher;
import java.security.*;
import java.util.HashMap;

//todo: change key serialisation
// https://docs.oracle.com/javase/7/docs/api/java/security/KeyFactory.html
public class AsymEncrypt {
    private static final String RSA
            = "RSA";
    private KeyPair keypair = null;

    private HashMap<String, Key> publicKeysContacts = new HashMap<>();

    public AsymEncrypt() throws Exception {
        this.keypair = generateRSAKkeyPair();
    }

    public byte[] getPublicKeySer(){
        byte[] pubKey =  SerializationUtils.serialize(this.keypair.getPublic());
        return pubKey;
    }
    // Generating public & private keys
    // using RSA algorithm.
    public static KeyPair generateRSAKkeyPair()
            throws Exception
    {
        SecureRandom secureRandom
                = new SecureRandom();
        KeyPairGenerator keyPairGenerator
                = KeyPairGenerator.getInstance(RSA);

        keyPairGenerator.initialize(
                2048, secureRandom);
        return keyPairGenerator
                .generateKeyPair();
    }

    public byte[] do_RSAEncryption(String plainText, Key publicKey) throws Exception {
        //Key publicKey= SerializationUtils.deserialize(publicKeyStr.getBytes());

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(
                plainText.getBytes());
    }

    public byte[] do_RSAEncryption(byte[] plainTextBytes, Key publicKey) throws Exception {
        //Key publicKey= SerializationUtils.deserialize(publicKeyStr.getBytes());

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plainTextBytes);
    }

    public String decryptionToString(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }

    public byte[] decryptionToByte(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return result;
    }
}
