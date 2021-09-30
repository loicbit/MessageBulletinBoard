package MessageBulletinBoard.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserGui {
    private JPanel panel;
    private JTextField textFieldNameUser;
    private JButton buttonConfirmNameUser;
    private JTextField textField2;
    private JButton buttonConfirmContactName;
    private JComboBox comboBox1;
    private JTextArea textArea1;
    private JTextField textField3;
    private JButton buttonSendMessage;
    private JButton buttonConnect;

    private UserServer userServer;

    public UserGui() {
        buttonConfirmNameUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userServer = new UserServer(textFieldNameUser.getText());
            }
        });
        buttonConfirmContactName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        buttonSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("User");

        frame.setContentPane(new UserGui().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
