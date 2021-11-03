package MessageBulletinBoard.crypto;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class testCrypto {
    public static void main(String[] args) throws Exception {
        DiffieH diffieA = new DiffieH();
        DiffieH diffieB = new DiffieH();

        String pubkeyA = diffieA.getPubKey();
        String pubkeyB = diffieB.getPubKey();


        //PublicKey pubkeyA = diffieA.getPublickey();
        //PublicKey pubkeyB = diffieB.getPublickey();

        diffieA.generateSecretKey(pubkeyB);
        diffieB.generateSecretKey(pubkeyA);

        String result = diffieA.encrypt("test_1");
        System.out.println(diffieB.decrypt(result));

        String result2 = diffieA.encrypt("test_2");
        System.out.println(diffieB.decrypt(result2));


        String plainText = "Test_RSA";

        AssymEncrypt alice = new AssymEncrypt();
        AssymEncrypt bob = new AssymEncrypt();

        byte[] data = SerializationUtils.serialize(result);
        String yourObject = SerializationUtils.deserialize(data);

    }
}
