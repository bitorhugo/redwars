package edu.ufp.inf.sd.rmi.red.client.login;

import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.GridBagLayout;

public class LoginPanel extends JPanel {

    private JLabel userIDLabel;
    
    public LoginPanel() {
        this.setLayout(new GridBagLayout());
        this.setup();
    }

    private void setup() {
        userIDLabel = new javax.swing.JLabel();
        userIDLabel.setText("User ID");
        this.add(userIDLabel);
    }
    // TODO: add jwt token to login
    
}
