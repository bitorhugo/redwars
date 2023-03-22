package edu.ufp.inf.sd.rmi.red.client;

import edu.ufp.inf.sd.rmi.red.client.login.LoginPanel;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;
import engine.Game;
import engine.Gui;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import java.awt.GridBagLayout;

/**
 * @author Vitor Santos
 */
public class RedClient {
    // RedClient is the base Frame of GUI

    private SetupContextRMI contextRMI;
    private GameFactoryRI stub;


    public RedClient (String args[]) {
        this.initContext(args);
        this.lookup();
        this.startGame();
    }

    /**
     * Sets up the frame and calls first panel
     */
    private void startGame() {
        // this.setSize(400, 400);
        // this.setLayout(new GridBagLayout());
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // this.setLocationRelativeTo(null);
        // this.setVisible(true);
        // new Game();
        try {
            System.out.println(this.stub.register("bitor", "123"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    
    private void initContext(String args[]) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            // http:localhost:1099/{ServiceName}
            this.contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (Exception e) {
            Logger.getLogger(RedClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void lookup() {
        Registry reg = this.contextRMI.getRegistry();
        if (reg == null) {
            System.out.println("Registry is null");
        }
        String serviceUrl = contextRMI.getServicesUrl(0);
        try {
            this.stub = (GameFactoryRI) reg.lookup(serviceUrl);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String[] args) {
        new RedClient(args);
        // java.awt.EventQueue.invokeLater(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (args.length >= 3) {
        //             new RedClient(args);
        //         } else {
        //             System.out.println(RedClient.class + ": call must have the following args: <rmi_ip> <rmi_port> <rmi_service_prefix>");
        //         }
        //     }
        // });
    }
}

