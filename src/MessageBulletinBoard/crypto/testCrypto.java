package MessageBulletinBoard.crypto;

import MessageBulletinBoard.authenticationserver.AuthenticationServerInterface;
import org.apache.commons.lang3.SerializationUtils;

import java.security.*;
import java.util.Base64;

public class testCrypto {
    private static final int SALT_LENGTH = 16; // in bytes

    public static void main(String[] args) throws Exception {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        PrivateKey privKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        Signature signSign = Signature.getInstance("SHA256WithDSA");

        signSign.initSign(pair.getPrivate());

        byte[] token = generateToken();
        byte[] token2 = generateToken();

        signSign.update(token);
        byte[] tokenSigned = signSign.sign();

        Signature signVerify = Signature.getInstance("SHA256WithDSA");
        signVerify.initVerify(pair.getPublic());
        //signVerify.update(token2);
        signVerify.update(token);

        Boolean resultSign = signVerify.verify(tokenSigned);

        System.out.println(resultSign);



        DiffieH diffieA = new DiffieH(false);
        DiffieH diffieB = new DiffieH(false);

        byte [] empty = new byte[1];

        byte [] salt = generateSalt();

        //diffieA.setSalt(salt);
        //diffieB.setSalt(salt);

        String pubkeyA = diffieA.getPubKey();
        String pubkeyB = diffieB.getPubKey();

        diffieA.generateSecretKey(pubkeyB);
        diffieB.generateSecretKey(pubkeyA);

        int seed = diffieA.getSeed();
        diffieB.setSeed(seed);

        String testLength = "test_1lqksùflmkqsùdkqsùlmdfkùlsqmkdfùlmqskdfùlqmskdfùmlkqsùfmdlkqslmdfkùmqsldkfùlmqskdfùmlqskdfùmlqskdùmflkqsùlfdmkqsùmdlfkùqslmdkflmqskdfùmlqskdfùlmqskdfmlkqsùdlmfkqsùdmlfkùlqmsdkfùmqslkdfùqmslkdfùlmqskdùflmkqsùlmdfkùqslmdkfùlmqskdfmlqskdùfmlqskdùflmqskdùlfmkqsùdlfmkqsùdflqsùdflmkqsùdmlfkùqslmkdfùqslkfùltest_1lqksùflmkqsùdkqsùlmdfkùlsqmkdfùlmqskdfùlqmskdfùmlkqsùfmdlkqslmdfkùmqsldkfùlmqskdfùmlqskdfùmlqskdùmflkqsùlfdmkqsùmdlfkùqslmdkflmqskdfùmlqskdfùlmqskdfmlkqsùdlmfkqsùdmlfkùlqmsdkfùmqslkdfùqmslkdfùlmqskdùflmkqsùlmdfkùqslmdkfùlmqskdfmlqskdùfmlqskdùflmqskdùlfmkqsùdlfmkqsùdflqsùdflmkqsùdmlfkùqslmkdfùqslkfùl";
        String result = diffieA.encrypt(testLength);
        System.out.println(diffieB.decrypt(result));

        String result2 = diffieA.encrypt("test_2");
        System.out.println(diffieB.decrypt(result2));

        System.out.println("Test KDF");

        String result_kdf_1 = diffieA.encrypt("test_1");
        System.out.println(diffieB.decrypt(result_kdf_1));

        String result_kdf_2 = diffieA.encrypt("test_2");
        System.out.println(diffieB.decrypt(result_kdf_2));


        String result_kdf_2_1 = diffieA.encrypt("test_kdf_2_1");
        System.out.println(diffieB.decrypt(result_kdf_2_1));

        String result_kdf_2_2 = diffieA.encrypt("test_kdf_2_2");
        System.out.println(diffieB.decrypt(result_kdf_2_2));

        String plainText = "Test_RSA";

        AsymEncrypt alice = new AsymEncrypt();
        AsymEncrypt bob = new AsymEncrypt();

        byte[] data = SerializationUtils.serialize(result);
        String yourObject = SerializationUtils.deserialize(data);

    }

    private static byte[] generateSalt(){
        SecureRandom randomSalt = new SecureRandom();
        randomSalt.setSeed(12345);

        byte[] saltTemp = new byte[SALT_LENGTH];
        randomSalt.nextBytes(saltTemp);
        return saltTemp;
    }

    private static byte[] generateToken() throws SignatureException, InvalidKeyException {
       SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[4];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }
}
