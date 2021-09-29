package MessageBulletinBoard.bulletinboard;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BulletinBoardServer implements BulletinBoardInterface{
    public BulletinBoardServer(){}

    public static void main(String[] args){
        try {
            BulletinBoardServer obj = new BulletinBoardServer();
            BulletinBoardInterface stub = (BulletinBoardInterface) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry(2001);

            registry.bind("rmi:bulletinboard", stub);

            System.err.println("Server ready");
        }catch (Exception e) {
            System.out.println("BulletinBoard Server failed: " + e);
        }
    }

    @Override
    public String get(int i, String b) throws RemoteException {
        return null;
    }

    @Override
    public void add(int index, String value, String tag) throws RemoteException {

    }
}
