package MessageBulletinBoard.client;

import MessageBulletinBoard.data.CellLocationPair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.security.Key;
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
    private HashMap<String, UserClient>  userClient = new HashMap();


    //AssymEncrypt assymEncrypt = new AssymEncrypt();

    //private BulletinBoardClient boardClient;

    private HashMap<String, String> conversation = new HashMap();
    private String nameUser;

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
                try {
                    addContact(textFieldNewContact.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    connectContact(currentContact);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    sendMessage(currentContact, textFieldMessage.getText());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
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

        this.textAreaConversation.setBorder(new JTextField().getBorder());
    }

    private void setUser(String nameUser){
        this.textFieldNameUser.setEnabled(false);
        this.buttonConfirmNameUser.setEnabled(false);
        this.nameUser = nameUser;

        try{
            this.userServer = new UserServer(nameUser);
        }catch(Exception ex){
            System.out.println(ex);
            //todo: print notification
            //      use different exceptions to notify
        }
    }

    private void addContact(String nameContact) throws Exception {
        //todo : fix init with new, if you can't find them
        comboBoxContacts.addItem(nameContact);

        connectContact(nameContact);
    }

    private void connectContact(String nameContact) throws Exception {
        //todo: connect new user
        Boolean exchanged = false;

        if(!this.userClient.containsKey(nameContact)){
            this.userClient.put(nameContact, new UserClient(nameContact, this.nameUser));
        }

        if(!this.isConnected(nameContact)){
            try{
                exchanged =  this.userClient.get(nameContact).symmetricKeyExchange(nameContact);
                Key publicKeyOther = this.userServer.getPublicKeyContact(nameContact);
                this.userClient.get(nameContact).setPublicKeyContact(publicKeyOther);

                this.userClient.get(nameContact).sendPublicKeys();

            }catch(RemoteException ex){
                //todo: note contact not available to connect
            }

            if(exchanged){
                this.buttonConfirmContactName.setEnabled(false);
            }

        }else{
            //todo message already connected
        }
    }

    private void sendMessage(String name, String message) throws RemoteException {
        //todo handle null client
        if(this.userClient.containsKey(name) && this.userClient.get(name).isConnected()){
            boolean sent=  this.userClient.get(name).sendMessageBoard(message);

            if(!sent){
                //todo: print error
            }
        }else{
            //todo: print error message
        }
    }

    private void getMessage(String name) throws RemoteException {
        String currentConv = "";

        if(this.userClient.containsKey(name) && this.isConnected(name)){
            String newMessage = this.userClient.get(name).getMessageBoard();
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

    private boolean isConnected(String contactName){
        boolean statusClient =  this.userClient.get(contactName).isConnected();
        boolean statusServer = this.userServer.isConnected(contactName);

        if(!statusClient && statusServer){
            CellLocationPair cellAB = this.userServer.getFirstCellAB(contactName);
            CellLocationPair cellBA = this.userServer.getFirstCellBA(contactName);

            this.userClient.get(contactName).setFirstCellPair(cellAB, cellBA);


            if(cellAB != null && cellBA != null) return true;
            else return false;

        }else if(statusClient){
            return true;
        }

        //todo check if contact is connected via client or server
        return false;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("User");

        frame.setContentPane(new UserGui().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);



    }
}
