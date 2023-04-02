package edu.ufp.inf.sd.rmi.red.client;


import edu.ufp.inf.sd.rmi.red.server.gamesession.GameSessionRI;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;
import engine.Game;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;


/**
 * @author Vitor Santos
 */
public class RedClient extends JFrame {
    // RedClient is the base Frame of GUI

    private SetupContextRMI contextRMI;
    private GameFactoryRI stub;
    private GameSessionRI session;
    private Scanner in = new Scanner(System.in);

    public RedClient (String args[]) {
        this.initContext(args);
        this.lookup();
        // this.login();
        this.startGame();
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

    private void login() {
        System.out.println("------Choose Option------");
        System.out.println("(L) Login\t(R) Register");
        String choice = this.in.nextLine();
        System.out.println("Enter Username: ");
        String username = this.in.nextLine();
        System.out.println("Enter Secret: ");
        String secret = this.in.nextLine();
        switch (choice.toLowerCase()) {
        case "l":
            try {
                this.session = this.stub.login(username, secret);
                System.out.println(session);
                this.startGame();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            break;
        case "r":
            try {
                this.session = this.stub.register(username, secret);
                this.startGame();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            break;
        }
    }


    /**
     * Starts new Game 
     */
    private void startGame() {
        new Game(this.stub);
    }

    
    public static void main(final String[] args) {
        RedClient client = new RedClient(args);
        // client.startGame();
    }
}

