package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.data.CellLocationPair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class UserGui {
    private JPanel panel;
    private JTextField textFieldNameUser;
    private JButton buttonConfirmNameUser;
    private JTextField textFieldNewContact;
    private JButton buttonConfirmContactName;
    private JComboBox comboBoxContacts;
    private JTextArea textAreaConversation;
    private JTextField textFieldMessage;
    private JButton buttonSendMessage;
    private JButton buttonConnect;

    private UserServer userServer;
    private UserClient userClient;

    private BulletinBoardClient boardClient;

    private HashMap<String, String> conversation = new HashMap();

    public UserGui() {
        buttonConfirmNameUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUser(textFieldNameUser.getText());
            }
        });
        buttonConfirmContactName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addContact(textFieldNewContact.getText());
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                connectContact(currentContact);
            }
        });
        buttonSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                sendMessage(currentContact, textFieldMessage.getText());
            }
        });

        Timer t = new Timer();

        TimerTask messagePoller = new TimerTask() {
            @Override
            public void run() {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    getMessage(currentContact);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }


        };

        t.schedule(messagePoller, new Date(), 1000);
        comboBoxContacts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateConversation();
            }
        });
    }

    private void setUser(String nameUser){
        this.textFieldNameUser.setEnabled(false);
        this.buttonConfirmNameUser.setEnabled(false);


        try{
            this.boardClient = new BulletinBoardClient(nameUser);
            this.userServer = new UserServer(nameUser);
            this.userClient = new UserClient(nameUser);

        }catch(Exception ex){
            System.out.println(ex);
            //todo: print notification
            //      use different exceptions to notify
        }
    }

    private void addContact(String nameContact){
        //todo : fix init with new if you can find them
        comboBoxContacts.addItem(nameContact);

        connectContact(nameContact);
    }

    private void connectContact(String nameContact){
        //todo: connect new user
        CellLocationPair nextReceiveLocation = null;

        if(!this.boardClient.nextCellLocationPairAB.containsKey(nameContact)){
            try{
                Boolean exchanged =  this.userClient.contactAsKeyExchange(nameContact);

                if(exchanged){
                    CellLocationPair cellAB =  this.userClient.getNextCellLocationPairAB(nameContact);
                    CellLocationPair cellBA = this.userClient.getNextCellLocationPairBA(nameContact);

                    this.boardClient.nextCellLocationPairAB.put(nameContact, cellAB);
                    this.boardClient.nextCellLocationPairBA.put(nameContact, cellBA);
                }

            }catch(RemoteException ex){
                //todo: note contact not available to connect
            }

            if(nextReceiveLocation != null){
                this.buttonConfirmContactName.setEnabled(false);
            }
        }
    }

    private void sendMessage(String name, String message){
        //todo handle null client
        if(this.boardClient != null){
            if(!this.boardClient.isNextCellLocationPairABSetted(name)) {
                //CellLocationPair nextLocation = this.userServer.getFirstCellPairAB(name);
                //this.boardClient.setNextCellLocationPairAB(name, nextLocation);
            }
            try {
                this.boardClient.sendMessage(name, message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            catch(NullPointerException e){

            }
        }

    }

    private void getMessage(String name) throws RemoteException {
        String currentConv = "";

        if(this.boardClient != null){
            String newMessage = this.boardClient.getMessage(name);
            if(newMessage != null){
                newMessage = '\n'+ newMessage;

                if(this.conversation.containsKey(name)){
                    currentConv = this.conversation.get(name);
                    currentConv += newMessage;

                    this.conversation.replace(name, currentConv);
                }else{
                    this.conversation.put(name, newMessage);
                }

                updateConversation();
            }
        }
    }

    private void updateConversation(){
        String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());

        String textToShow = this.conversation.get(currentContact);

        this.textAreaConversation.setText(textToShow);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("User");

        frame.setContentPane(new UserGui().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
