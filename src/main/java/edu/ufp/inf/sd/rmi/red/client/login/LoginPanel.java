package edu.ufp.inf.sd.rmi.red.client.login;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import engine.Game;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel implements ActionListener {

    private JLabel userIDLabel;
    private JButton playGameButton;
    
    public LoginPanel() {
        this.setLayout(new GridBagLayout());
        this.setup();
    }

    private void setup() {
        userIDLabel = new javax.swing.JLabel();
        userIDLabel.setText("User ID");
        this.add(userIDLabel);

        this.playGameButton = new JButton();
        this.playGameButton.addActionListener(this);
        this.add(playGameButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == this.playGameButton) {
            SwingUtilities.getWindowAncestor(this.playGameButton).setVisible(false);
            new Game(); 
        }
    }
    
}
