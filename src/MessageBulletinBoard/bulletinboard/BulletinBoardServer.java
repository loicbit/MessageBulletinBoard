package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.BulletinCell;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import MessageBulletinBoard.data.CellPair;
import org.apache.commons.lang3.SerializationUtils;

import java.security.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BulletinBoardServer implements BulletinBoardInterface{
    private BulletinCell cells[] = null;
    private int NUMBER_CELLS = 300;

    static Registry registry = null;
    private MessageDigest md;
    private AsymEncrypt asymEncrypt = null;

    private List<String> authTokens = new LinkedList<>();
    private HashMap<String,Key> publickeys = new HashMap<>();

    public BulletinBoardServer() throws Exception {
        this.cells = new BulletinCell[NUMBER_CELLS];
        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);

        for(int i=0; i< NUMBER_CELLS; i++){
            this.cells[i] = new BulletinCell();
        }
        this.asymEncrypt = new AsymEncrypt();

    }

    public static void main(String[] args){
        try {
            BulletinBoardServer obj = new BulletinBoardServer();
            BulletinBoardInterface stub = (BulletinBoardInterface) UnicastRemoteObject.exportObject(obj, 0);

            try{
                registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
            }catch(Exception e) {
                registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
            }

            registry.bind(BulletinBoardInterface.STUB_NAME, stub);

            System.err.println("Server ready");
        }catch (Exception e) {
            System.out.println("BulletinBoard Server failed: " + e);
        }
    }

    @Override
    public byte[] initMixedServer(String authToken, byte[] pubKey) throws RemoteException {
        if(this.verifyAuthToken(authToken)){
            Key publicKeyOther = SerializationUtils.deserialize(pubKey);
            this.publickeys.put(authToken, publicKeyOther);

            return this.asymEncrypt.getPublicKeySer();
        }else{
            return new byte[0];
        }

    }

    @Override
    public String get(int i, String b) throws RemoteException {
        String message = null;
        CellPair toRemove = null;

        String hashB = new String(md.digest(b.getBytes()));

        for (CellPair pair : this.cells[i].getCellPairs()) {
            if (pair.getTag().equals(hashB)) {
                message = pair.getValue();
                toRemove = pair;
            }
        }

        if (toRemove != null) {
            this.cells[i].removePair(toRemove);
            return message;
        }
        return null;
    }


    public boolean add(int index, String value, String tag) throws RemoteException {
        //todo remove token control, done by mixednetwork
        //if(verifyToken(token)){
        CellPair newPair = new CellPair(value, tag);
        this.cells[index].addPair(newPair);
        return true;
        //}else return false;
    }

    private boolean verifyAuthToken(String token){
        return true;

        //todo verify
        //todo save used tokens and check
        /*
        if(this.usedTokens.contains(token)) return false;

        //this.signature
        return true;*/
    }
}
