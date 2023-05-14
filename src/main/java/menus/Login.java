package menus;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DeliverCallback;

import edu.ufp.inf.sd.rmi.red.server.queuenames.exchange.ExchangeEnum;
import edu.ufp.inf.sd.rmi.red.server.queuenames.rpc.RPCEnum;
import engine.Game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
            String u = username.getText();
            String s = secret.getText();
            try {
                if (this.call(u, s).compareTo("ok") == 0) {
                    Game.u = u;
                    Game.rpcStartGameGui += Game.u;
                    Game.rpcStartGame();
                    new StartMenu();
                }
            } catch (IOException | InterruptedException | ExecutionException e1) {
                e1.printStackTrace();
            }
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

    private String call(String username, String secret) throws IOException, InterruptedException, ExecutionException {

        String credentials = username + ";" + secret;
        
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = Game.chan.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
            .Builder()
            .correlationId(corrId)
            .replyTo(replyQueueName)
            .build();

        System.out.println("Sending [credentials] to server");
        
        Game.chan.basicPublish("", RPCEnum.RPC_LOGIN.getValue(), props, credentials.getBytes("UTF-8"));

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = Game.chan.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.complete(new String(delivery.getBody(), "UTF-8"));
                }
            }, consumerTag -> {
            });

        String result = response.get();
        Game.chan.basicCancel(ctag);
        System.out.println("Received [" + result + "] from server");
        return result;
    }

    private void handleMessages(String action, String username, String secret) {
        try {
            // open queue for receiving response from server
            Game.chan.queueDeclare(username, false, false, false, null);
            Game.chan.basicQos(1);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + response + "'");
                switch (response) {
                case "ok":
                    Game.u = username;
                    Game.rpcStartGameGui += Game.u;
                    Game.rpcStartGame();
                    Game.chan.queueDelete(Game.u, false, false);
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

            String credentials = username + ";" + secret;
            switch (action) {
                
            case "register":
                Game.chan.exchangeDeclare(ExchangeEnum.AUTHEXCHANGENAME.getValue(), "fanout");
                Game.chan.basicPublish(ExchangeEnum.AUTHEXCHANGENAME.getValue(), "", null, credentials.getBytes("UTF-8"));
                System.out.println("INFO: Success! Message " + credentials + " sent to Exchange AUTH.");
                break;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
