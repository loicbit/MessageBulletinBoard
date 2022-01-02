package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.MessageDigest;
import java.util.LinkedList;

public class BulletinBoardClient {
    private Registry registry;
    String nameUser = null;

    CellLocationPair nextCellLocationPairAB = null;
    CellLocationPair nextCellLocationPairBA = null;

    private BulletinBoardInterface bulletinServerStub;


    private MessageDigest md;

    private AsymEncrypt asymEncrypt;

    private boolean isEncrypted = false;
    private boolean publicKeysSend = false;

    private LinkedList<String> tokens = new LinkedList<>();

    private boolean connected =false;

    private Key publicKeyOther;

    private String authToken;

    // todo generate hash of the state

    //todo connection with mixed network
    public BulletinBoardClient() throws Exception {
        //todo: Generate authtokens and save in file

        this.asymEncrypt = new AsymEncrypt();

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        this.bulletinServerStub = (BulletinBoardInterface) this.registry.lookup(BulletinBoardInterface.STUB_NAME);
        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);

        this.authToken="";
        this.connectToBulletin();
    }

    public void connectToBulletin() throws RemoteException {


        byte[] publicKeyOtherSerialized =  this.bulletinServerStub.initMixedServer(authToken, this.asymEncrypt.getPublicKeySer());
        this.publicKeyOther = SerializationUtils.deserialize(publicKeyOtherSerialized);
    }

    /*
    public boolean isConnected(){
        //todo replace emptyresponse to interface
        byte[] emptyResponse = new byte[0];

    }*/
    public void add(int index, String value, String tag) throws Exception{
        //todo decrypt

        byte[] indexEnc =  this.asymEncrypt.do_RSAEncryption(Integer.toString(index), this.publicKeyOther);
        byte[] valueEnc =  this.asymEncrypt.do_RSAEncryption(value, this.publicKeyOther);
        byte[] tagEnc =  this.asymEncrypt.do_RSAEncryption(tag, this.publicKeyOther);

        this.bulletinServerStub.add(indexEnc, valueEnc, tagEnc);
    }

    public String get(int index, String tag) throws Exception{
        byte[] indexEnc =  this.asymEncrypt.do_RSAEncryption(Integer.toString(index), this.publicKeyOther);
        byte[] tagEnc =  this.asymEncrypt.do_RSAEncryption(tag, this.publicKeyOther);
        byte[] authEnc =  this.asymEncrypt.do_RSAEncryption(tag, this.publicKeyOther);

        //todo check secure channel
        byte[] messageEnc = this.bulletinServerStub.get(indexEnc, tagEnc, authEnc);


        if (messageEnc.length < 2){
            return null;
        }
        else return this.asymEncrypt.decryptionToString(messageEnc);
    }

    /*public getPublicKey(){

    }*/

}
