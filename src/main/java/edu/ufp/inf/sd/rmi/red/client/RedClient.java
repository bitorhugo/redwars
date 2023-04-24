package edu.ufp.inf.sd.rmi.red.client;


import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;
import engine.Game;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * @author Vitor Santos
 */
public class RedClient {

    private SetupContextRMI contextRMI;
    private GameFactoryRI stub;

    public RedClient (String args[]) {
        this.initContext(args);
        this.lookup();
        this.startGame();
    }


    private void initContext(String args[]) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
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
        try {
            String serviceName = "GameFactory";
            this.stub = (GameFactoryRI) reg.lookup(serviceName);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts new Game 
     */
    private void startGame() {
        new Game(this.stub);
    }

    
    public static void main(final String[] args) {
        new RedClient(args);
    }
}

