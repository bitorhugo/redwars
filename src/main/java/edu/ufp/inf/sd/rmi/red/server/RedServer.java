package edu.ufp.inf.sd.rmi.red.server;

import edu.ufp.inf.sd.rmi.red.model.db.DB;
import edu.ufp.inf.sd.rmi.red.model.db.VolatileDB;
import edu.ufp.inf.sd.rmi.red.server.RedServer;
import edu.ufp.inf.sd.rmi.red.server.cluster.ClusterImpl;
import edu.ufp.inf.sd.rmi.red.server.cluster.ClusterRI;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryImpl;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
    private GameFactoryRI gameFactoryStub;
    private ClusterRI clusterStub;

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
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    private void connectToCluster() {
        try {
            clusterStub.connect(this);
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not connect to cluster");
            e.printStackTrace();
        }
    }

    private void lookupCluster(String service) {
        try {
            contextRMI.getRegistry().lookup(service);
        } catch (RemoteException | NotBoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Service {0} not bound", service);
            rebindService("Cluster");
        }
    }

    private void connectRabbitServices (String host) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        try {
            this.conn = factory.newConnection();
            System.out.println("INFO: Connection created " + conn);
        } catch (IOException | TimeoutException e) {
            System.err.println("ERROR: Not able to open connection with RabbitMQ Services");
        }
    }

    private void rebindService(String serviceNameOnRegistry) {
        try {
            if (this.contextRMI.getRegistry() != null) {
                switch(serviceNameOnRegistry) {
                case "GameFactory":
                    this.gameFactoryStub = new GameFactoryImpl(new VolatileDB(), this.conn);
                    this.contextRMI.getRegistry().rebind(serviceNameOnRegistry, this.gameFactoryStub);
                    break;
                case "Cluster":
                    this.clusterStub = new ClusterImpl();
                    this.contextRMI.getRegistry().rebind(serviceNameOnRegistry, this.clusterStub);
                    break;
                }
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "service {0} bound and running. :)", serviceNameOnRegistry);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
            }
        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length < 3) {
            System.err.println("usage: java [options] <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        }
        RedServer red = new RedServer(args);
        red.lookupCluster("Cluster");
        red.connectToCluster();
        red.connectRabbitServices(args[0]);
        red.rebindService("GameFactory");
    }
    
}

