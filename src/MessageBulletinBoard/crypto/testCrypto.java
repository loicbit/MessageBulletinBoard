package MessageBulletinBoard.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class testCrypto {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        DiffieH diffieA = new DiffieH();
        DiffieH diffieB = new DiffieH();

        String pubkeyA = diffieA.getPubKey();
        String pubkeyB = diffieB.getPubKey();


        //PublicKey pubkeyA = diffieA.getPublickey();
        //PublicKey pubkeyB = diffieB.getPublickey();

        diffieA.generateSecretKey(pubkeyB);
        diffieB.generateSecretKey(pubkeyA);

        String result = diffieA.encrypt("testjejjj");
        //byte[] result = diffieA.encrypt("testjejjj".getBytes());

        System.out.println(diffieB.decrypt(result));
        //System.out.println(new String(diffieB.decrypt(result)));

    }
}
