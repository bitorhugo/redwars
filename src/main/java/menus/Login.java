package menus;

import javax.swing.JButton;
import javax.swing.JTextField;

import engine.Game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.rmi.RemoteException;
import java.awt.Point;

public class Login implements ActionListener {
    public JTextField username = new JTextField("Username");
    public JTextField secret = new JTextField("Secret");
    public JButton Login = new JButton("Login");
    public JButton Register = new JButton("Register");
    public JButton Exit = new JButton("Exit");


    public Login() {
        Point size = MenuHandler.PrepMenu(400,280);
        MenuHandler.HideBackground();
        SetBounds(size);
        AddListeners();
        AddGui();
    }

	private void SetBounds(Point size) {
		username.setBounds(size.x, size.y+10+38, 100, 32);
		secret.setBounds(size.x+150,size.y+10+38, 100, 32);
		Login.setBounds(size.x,size.y+10+38*3, 100, 32);
		Register.setBounds(size.x,size.y+10+38*4, 100, 32);
        Exit.setBounds(size.x,size.y+10+38*6, 100, 32);
	}
	private void AddGui() {
        Game.gui.add(username);
        Game.gui.add(secret);
        Game.gui.add(Login);
        Game.gui.add(Register);
        Game.gui.add(Exit);
	}

	private void AddListeners() {
        username.addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent arg0) {
                    if (username.getText().equals("Username")) {
                        username.setText("");
                    }
                }
                @Override
                public void focusLost(FocusEvent arg0) {
                    if (username.getText().equals("")) {
                        username.setText("Username");
                    }
                }
            });
        secret.addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent arg0) {
                    if (secret.getText().equals("Secret")) {
                        secret.setText("");
                    }
                }
                @Override
                public void focusLost(FocusEvent arg0) {
                    if (secret.getText().equals("")) {
                        secret.setText("Secret");
                    }
                }
            });
		Login.addActionListener(this);
		Register.addActionListener(this);
		Exit.addActionListener(this);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == Login) {
            String u = username.getText();
            String s = secret.getText();
            try {
                Game.session = Game.remoteService.login(u, s);
                Game.u = u;
                new StartMenu();
            } catch (RemoteException e1) {
                String error = e1.getCause().toString();
                Game.error.ShowError(error);
            }
        }

        if (src == Register) {
            String u = username.getText();
            String s = secret.getText();
            try {
                Game.session = Game.remoteService.register(u, s);
                Game.u = u;
                new StartMenu();
            } catch (RemoteException e1) {
                String error = e1.getCause().toString();
                Game.error.ShowError(error);
            }            
        }

        if (src == Exit) {System.exit(0);}
    }
    
}
