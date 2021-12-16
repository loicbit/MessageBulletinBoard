package MessageBulletinBoard.crypto;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.Cipher;
import java.security.*;
import java.util.HashMap;

//todo: change key serialisation
// https://docs.oracle.com/javase/7/docs/api/java/security/KeyFactory.html
public class AssymEncrypt {
    private static final String RSA
            = "RSA";
    private KeyPair keypair = null;

    private HashMap<String, Key> publicKeysContacts = new HashMap<>();

    public AssymEncrypt() throws Exception {
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

    /*public void setPublickKey(String contactName, String publicKey){
        Key key= SerializationUtils.deserialize(publicKey.getBytes());
        this.publicKeysContacts.put(contactName, key);
    }*/

    // Encryption function which converts
    // the plainText into a cipherText
    // using private Key.
    public byte[] do_RSAEncryption(String plainText, Key publicKey) throws Exception {
        //Key publicKey= SerializationUtils.deserialize(publicKeyStr.getBytes());

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(
                plainText.getBytes());
    }

    // Encryption function which converts
    // the plainText into a cipherText
    // using private Key.
    public byte[] do_RSAEncryption(byte[] plainTextBytes, Key publicKey) throws Exception {
        //Key publicKey= SerializationUtils.deserialize(publicKeyStr.getBytes());

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plainTextBytes);
    }

    // Decryption function which converts
    // the ciphertext back to the
    // original plaintext.
    public String do_RSADecryption(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }

    // Decryption function which converts
    // the ciphertext back to the
    // original plaintext.
    public byte[] do_RSADecryption_byte(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
        byte[] result = cipher.doFinal(cipherText);

        return result;
    }

    /*


        byte[] cipherText
                = do_RSAEncryption(
                plainText);

        System.out.println(
                "The Public Key is: "
                        + DatatypeConverter.printHexBinary(
                        keypair.getPublic().getEncoded()));

        System.out.println(
                "The Private Key is: "
                        + DatatypeConverter.printHexBinary(
                        keypair.getPrivate().getEncoded()));

        System.out.print("The Encrypted Text is: ");

        System.out.println(
                DatatypeConverter.printHexBinary(
                        cipherText));

        String decryptedText
                = do_RSADecryption(
                cipherText,
                keypair.getPublic());

        System.out.println(
                "The decrypted text is: "
                        + decryptedText);
    }*/
}
