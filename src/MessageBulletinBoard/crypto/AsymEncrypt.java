package MessageBulletinBoard.crypto;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.Cipher;
import java.security.*;


public class AsymEncrypt {
    private static final String KEY_ALGO
            = "RSA";
    private KeyPair keypair = null;


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
                = KeyPairGenerator.getInstance(KEY_ALGO);

        keyPairGenerator.initialize(
                2048, secureRandom);
        return keyPairGenerator
                .generateKeyPair();
    }

    public byte[] encryptionTBytes(String plainText, Key publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGO);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(
                plainText.getBytes());
    }

    public byte[] encryptionTBytes(byte[] plainTextBytes, Key publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGO);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plainTextBytes);
    }

    public String decryptionToString(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(KEY_ALGO);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }

    public byte[] decryptionToByte(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(KEY_ALGO);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return result;
    }
}
