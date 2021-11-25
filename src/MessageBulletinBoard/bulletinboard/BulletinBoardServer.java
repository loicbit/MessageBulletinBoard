package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.data.BulletinCell;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import MessageBulletinBoard.bulletinboard.*;
import MessageBulletinBoard.data.CellPair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.LinkedList;
import java.util.List;

public class BulletinBoardServer implements BulletinBoardInterface{
    private BulletinCell cells[] = null;
    private int NUMBER_CELLS = 300;

    static Registry registry = null;
    private MessageDigest md;
    private Signature signature = null;

    private List<String> usedTokens = new LinkedList<>();

    public BulletinBoardServer() throws NoSuchAlgorithmException {
        this.cells = new BulletinCell[NUMBER_CELLS];
        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);

        for(int i=0; i< NUMBER_CELLS; i++){
            this.cells[i] = new BulletinCell();
        }
        initSignature();

    }

    private void initSignature() throws NoSuchAlgorithmException {
        this.signature = Signature.getInstance("SHA256withRSA");


        //todo ask pubkey
        //signature.initVerify(publicKey);
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

    @Override
    public boolean add(int index, String value, String tag, String token) throws RemoteException {
        if(verifyToken(token)){
            CellPair newPair = new CellPair(value, tag);
            this.cells[index].addPair(newPair);
            return true;
        }else return false;
    }

    private boolean verifyToken(String token){
        return true;

        //todo verify
        //todo save used tokens and check
        /*
        if(this.usedTokens.contains(token)) return false;

        //this.signature
        return true;*/
    }
}
