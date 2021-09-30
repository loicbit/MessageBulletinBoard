package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.bulletinboard.BulletinBoardServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class UserServer implements UserServerInterface{
    public UserServer(String nameUser){
        try {
            UserServerInterface stub = (UserServerInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = null;

            try{
                 registry = LocateRegistry.createRegistry(2001);
            }catch(Exception e) {
                 registry = LocateRegistry.getRegistry(2001);
            }


            String nameReg = "rmi:user/" + nameUser;
            registry.bind(nameReg, stub);

            System.err.println("Server ready");
        }catch (Exception e) {
            System.out.println("User Server failed: " + e);
        }
    }
    @Override
    public String initContact(String name, String publicKey) throws RemoteException {
        return null;
    }

    @Override
    public int getFirstCell(String name) throws RemoteException {
        return 0;
    }

    //private CellLocationPair generateFirstCell
}
