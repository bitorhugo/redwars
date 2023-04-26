package edu.ufp.inf.sd.rmi.red.server;

import edu.ufp.inf.sd.rmi.red.model.db.VolatileDB;
import edu.ufp.inf.sd.rmi.red.server.RedServer;
import edu.ufp.inf.sd.rmi.red.server.cluster.ClusterImpl;
import edu.ufp.inf.sd.rmi.red.server.cluster.ClusterRI;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryImpl;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryRI;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
public class RedServer implements Serializable {

    private transient Connection conn;
    private transient SetupContextRMI contextRMI;
    private GameFactoryRI gameFactoryStub;
    private ClusterRI clusterStub;
    private VolatileDB db = new VolatileDB();
    private Map<UUID, Lobby> lobbies = Collections.synchronizedMap(new HashMap<>());

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

    public VolatileDB getDB() {
        return this.db;
    }

    public Map<UUID, Lobby> getLobbies() {
        return this.lobbies;
    }

    public void setLobbies(Map<UUID, Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    public GameFactoryImpl getGameFactory() {
        return (GameFactoryImpl) this.gameFactoryStub;
    }

    private void connectToCluster() {
        try {
            this.clusterStub.connect(this);
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not connect to cluster");
            e.printStackTrace();
        }
    }
    
    // if cluster is unavailable it means that you're the first server spawned
    // if it is available, the service is already online and you'll be joining the cluster as an aditional server
    private void lookupService(String service) {
        try {
            switch (service) {
            case "GameFactory":
                this.gameFactoryStub = (GameFactoryRI) contextRMI.getRegistry().lookup(service);
                break;
            case "Cluster":
                this.clusterStub = (ClusterRI) contextRMI.getRegistry().lookup(service);
                break; 
            }
        } catch (RemoteException | NotBoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Service {0} not bound", service);
            rebindService(service);
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
            System.exit(-1);
        }
    }

    private void rebindService(String serviceNameOnRegistry) {
        try {
            if (this.contextRMI.getRegistry() != null) {
                switch(serviceNameOnRegistry) {
                case "GameFactory":
                    this.gameFactoryStub = new GameFactoryImpl(this.db, this.lobbies, this.conn);
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
        // TODO: refactor channels
        // TODO: servers attach to work qeueu ad then fanout to clients
        RedServer red = new RedServer(args);
        red.connectRabbitServices(args[0]);
        System.out.println("Cluster: " + red.clusterStub);
        System.out.println("Factory: " + red.gameFactoryStub);
        red.connectToCluster();
    }
    
}

