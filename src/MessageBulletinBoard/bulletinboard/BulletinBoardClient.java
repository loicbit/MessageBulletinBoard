package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
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
        this.connectToBulletin();
    }

    public void connectToBulletin() throws RemoteException {
        String authToken="";

        byte[] publicKeyOtherSerialized =  this.bulletinServerStub.initMixedServer(authToken, this.asymEncrypt.getPublicKeySer());
        this.publicKeyOther = SerializationUtils.deserialize(publicKeyOtherSerialized);
    }

    /*
    public boolean isConnected(){
        //todo replace emptyresponse to interface
        byte[] emptyResponse = new byte[0];

    }*/
    public boolean add(int index, String value, String tag) throws RemoteException{
        //todo check secure channel
        this.bulletinServerStub.add(index, value, tag);
        return false;
    }

    public String get(int i, String b) throws RemoteException{
        //todo check secure channel
        return this.bulletinServerStub.get(i, b);
    }

    /*public getPublicKey(){

    }*/

}
