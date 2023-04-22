package edu.ufp.inf.sd.rmi.red.server;

import edu.ufp.inf.sd.rmi.red.model.db.DB;
import edu.ufp.inf.sd.rmi.red.server.RedServer;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryImpl;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class RedServer {

    private Connection conn;
    
    private SetupContextRMI contextRMI;
    private GameFactoryRI stub;


    /**
     * 
     * @param args 
     */
    public RedServer (String args[]) {
        try {
            //============ List and Set args ============
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //============ Create a context for RMI setup ============
            this.contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
            this.conn = createConnection(args[0]).orElseThrow();
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    private void rebindService() {
        try {
            //Bind service on rmiregistry and wait for calls
            if (this.contextRMI.getRegistry() != null) {
                this.stub = new GameFactoryImpl(this.conn, new DB("/home/bitor/projects/redwars/main.db"));
                //Get service url (including servicename)
                String serviceUrl = this.contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going MAIL_TO_ADDR rebind service @ {0}", serviceUrl);
                //============ Rebind servant ============
                this.contextRMI.getRegistry().rebind(serviceUrl, this.stub); // GameFactoryRI -> GameFactoryRI stub
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "service bound and running. :)");
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
            }
        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Optional<Connection> createConnection(String host) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        Connection conn;
        try {
            conn = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            conn = null;
            System.err.println("ERROR: Not able to open connection with RabbitMQ Services");
        }
        return Optional.ofNullable(conn);
    }


    public static void main(String[] args) {
        // TODO: add clusters to build 3 servers
        if (args != null && args.length < 3) {
            System.err.println("usage: java [options] edu.ufp.sd._02_calculator.server.CalculatorServer <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        }
        RedServer red = new RedServer(args);
        red.rebindService();
    }
}

