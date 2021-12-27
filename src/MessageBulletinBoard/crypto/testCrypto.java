package MessageBulletinBoard.crypto;

import org.apache.commons.lang3.SerializationUtils;

import java.security.*;

public class testCrypto {
    private static final int SALT_LENGTH = 16; // in bytes

    public static void main(String[] args) throws Exception {
        DiffieH diffieA = new DiffieH();
        DiffieH diffieB = new DiffieH();

        byte [] salt = generateSalt();

        //diffieA.setSalt(salt);
        //diffieB.setSalt(salt);

        String pubkeyA = diffieA.getPubKey();
        String pubkeyB = diffieB.getPubKey();


        //PublicKey pubkeyA = diffieA.getPublickey();
        //PublicKey pubkeyB = diffieB.getPublickey();

        diffieA.generateSecretKey(pubkeyB);
        diffieB.generateSecretKey(pubkeyA);

        int seed = diffieA.getSeed();
        diffieB.setSeed(seed);

        String result = diffieA.encrypt("test_1");
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
}
