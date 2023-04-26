package edu.ufp.inf.sd.rmi.red.server.cluster;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ufp.inf.sd.rmi.red.model.user.User;
import edu.ufp.inf.sd.rmi.red.server.RedServer;
import edu.ufp.inf.sd.rmi.red.server.gamefactory.GameFactoryImpl;
import edu.ufp.inf.sd.rmi.red.server.lobby.Lobby;


public class ClusterImpl extends UnicastRemoteObject implements ClusterRI {

    List<RedServer> servers = Collections.synchronizedList(new ArrayList<>());
    RedServer master;
    
    public ClusterImpl() throws RemoteException {
        super();
    }

    @Override
    public void connect(RedServer server) throws RemoteException {
        if (this.servers.isEmpty()) {
            this.servers.add(server);
            this.master = server;
        }
        else { // in the case of not being the first server on cluster, data needs to be synced a.k.a deep copied
            try {
                // create deep copy of data using GSON serialization/deserialization methods
                ObjectMapper mapper = new ObjectMapper();

                TypeReference<HashMap<String, User>> usersTypeRef = new TypeReference<HashMap<String, User>>() {};
                Map<String, User> clonedUsers = mapper.readValue(mapper.writeValueAsString(this.master.getDB().getUsers()), usersTypeRef);
                
                // TypeReference<GameFactoryImpl> lobbiesTypeRef = new TypeReference<GameFactoryImpl>() {};
                // Map<UUID, Lobby> clonedLobbies = mapper.readValue(mapper.writeValueAsString(lobbies),
                //                                                   lobbiesTypeRef);

                var lobbies = this.master.getLobbies();
                
                server.getDB().setUsers(clonedUsers);
                server.setLobbies(lobbies);
                
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Server DB synced", server);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Server Lobbies synced", server);
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Not able to sync servers");
                System.exit(-1);
            }
            this.servers.add(server);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Server {0} added", server);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of servers in cluster = {0}", servers.size());                
    }

    @Override
    public void disconnect(RedServer server) throws RemoteException {
        this.servers.remove(server);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Server {0} removed", server);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of servers in cluster = {0}", servers.size());        
    }

    @Override
    public List<RedServer> getServers() throws RemoteException {
        return this.servers;
    }

    @Override
    public boolean hasServer(RedServer server) throws RemoteException {
        return this.servers.contains(server);
    }
    
}
