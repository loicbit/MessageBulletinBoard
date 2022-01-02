package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.mixednetwork.MixedNetworkServerInterface;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.MessageDigest;

public class BulletinBoardClient {
    private Registry registry;
    private final BulletinBoardInterface bulletinServerStub;


    private final MessageDigest md;

    private final AsymEncrypt asymEncrypt;

    private Key publicKeyOther;

    private final String authName;


    public BulletinBoardClient() throws Exception {

        this.asymEncrypt = new AsymEncrypt();

        try {
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        } catch (Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        this.bulletinServerStub = (BulletinBoardInterface) this.registry.lookup(BulletinBoardInterface.STUB_NAME);
        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);

        int randomId = (int) Math.floor(Math.random() * 1000);
        this.authName = MixedNetworkServerInterface.DEF_NAME + " " + randomId;
        this.connectToBulletin();
    }

    public void connectToBulletin() throws RemoteException {
        byte[] publicKeyOtherSerialized = this.bulletinServerStub.initMixedServer(this.authName, this.asymEncrypt.getPublicKeySer());
        this.publicKeyOther = SerializationUtils.deserialize(publicKeyOtherSerialized);
    }

    public void add(int index, String value, String tag) throws Exception {
        byte[] indexEnc = this.asymEncrypt.encryptionTBytes(Integer.toString(index), this.publicKeyOther);
        byte[] valueEnc = this.asymEncrypt.encryptionTBytes(value, this.publicKeyOther);
        byte[] tagEnc = this.asymEncrypt.encryptionTBytes(tag, this.publicKeyOther);

        this.bulletinServerStub.add(indexEnc, valueEnc, tagEnc);
    }

    public String get(int index, String tag) throws Exception {
        byte[] indexEnc = this.asymEncrypt.encryptionTBytes(Integer.toString(index), this.publicKeyOther);
        byte[] tagEnc = this.asymEncrypt.encryptionTBytes(tag, this.publicKeyOther);
        byte[] authEnc = this.asymEncrypt.encryptionTBytes(this.authName, this.publicKeyOther);

        byte[] messageEnc = this.bulletinServerStub.get(indexEnc, tagEnc, authEnc);

        if (messageEnc.length < 2) {
            return null;
        } else return this.asymEncrypt.decryptionToString(messageEnc);
    }
}
