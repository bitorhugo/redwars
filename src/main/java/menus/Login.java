package menus;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.client.exchange.ExchangeEnum;
import engine.Game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.awt.Point;

public class Login implements ActionListener{
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
        // username.addKeyListener(this);
        // username.addFocusListener(new FocusListener(){
        //         @Override
        //         public void focusGained(FocusEvent arg0) {
        //             if (username.getText().equals("Username")) {
        //                 username.setText("");
        //             }
        //         }
        //         @Override
        //         public void focusLost(FocusEvent arg0) {
        //             if (username.getText().equals("")) {
        //                 username.setText("Username");
        //             }
        //         }
        //     });
        // secret.addKeyListener(this);
        // secret.addFocusListener(new FocusListener(){
        //         @Override
        //         public void focusGained(FocusEvent arg0) {
        //             if (secret.getText().equals("Secret")) {
        //                 secret.setText("");
        //             }
        //         }
        //         @Override
        //         public void focusLost(FocusEvent arg0) {
        //             if (secret.getText().equals("")) {
        //                 secret.setText("Secret");
        //             }
        //         }
        //     });
		Login.addActionListener(this);
		Register.addActionListener(this);
		Exit.addActionListener(this);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == Login) {
            String action = "login";
            String u = username.getText();
            String s = secret.getText();
            this.handleMessages(action, u, s);
            // try {
            //     Game.session = Game.remoteService.login(u, s);
            //     Game.u = u;
            //     new StartMenu();
            // } catch (RemoteException e1) {
            //     String error = e1.getCause().toString();
            //     Game.error.ShowError(error);
            // }
        }

        if (src == Register) {
            String action = "register";
            String u = username.getText();
            String s = secret.getText();
            this.handleMessages(action, u, s);
            // try {
            //     Game.session = Game.remoteService.register(u, s);
            //     Game.u = u;
            //     new StartMenu();
            // } catch (RemoteException e1) {
            //     String error = e1.getCause().toString();
            //     Game.error.ShowError(error);
            // }            
        }

        if (src == Exit) {System.exit(0);}
    }

    // @Override
    // public void keyPressed(KeyEvent arg0) {
    //     char c = arg0.getKeyChar(); // retrieve keyboard input from user
    //     if (c == '\n' || c == '\r') {
    //         String u = username.getText();
    //         String s = secret.getText();
    //         String action = "login";
    //         this.handleMessages(action, u, s);
    //         // try {
    //         //     Game.session = Game.remoteService.login(u, s);
    //         //     Game.u = u;
    //         //     new StartMenu();
    //         // } catch (RemoteException e1) {
    //         //     String error = e1.getCause().toString();
    //         //     Game.error.ShowError(error);
    //         // }
    //     }
    // }
 
    // @Override
    // public void keyReleased(KeyEvent arg0) {}

    // @Override
    // public void keyTyped(KeyEvent arg0) {}

    private void handleMessages(String action, String username, String secret) {
        String message = action + ";" + username + ";" + secret;
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x-expires", 60000); // queue time-to-live = 60s 
            Game.chan.queueDeclare(username, false, false, false, args);
            System.out.println(" [*] Waiting for messages.");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String msg = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + msg + "'");
                switch (msg) {
                case "ok":
                        new StartMenu();
                        break;
                default:
                    switch (action) {
                    case "login":
                        Game.error.ShowError("ERROR: User not found!");
                        break;
                    case "register":
                        Game.error.ShowError("ERROR: User already registered!");
                        break;
                    }
                }
            };
            Game.chan.basicConsume(username, true, deliverCallback, consumerTag -> { });
                
            Game.chan.basicPublish(ExchangeEnum.AUTHEXCHANGENAME.getValue(), "", null, message.getBytes("UTF-8"));
            System.out.println("INFO: Success! Message " + message + " sent to Exchange auth.");

        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}
