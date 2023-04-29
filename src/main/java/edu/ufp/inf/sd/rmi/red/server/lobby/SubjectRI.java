package edu.ufp.inf.sd.rmi.red.server.lobby;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;

public interface SubjectRI extends Serializable {
    public List<ObserverRI> players() throws RemoteException;
    public UUID getID() throws RemoteException;
    public String getMapname() throws RemoteException;
    
    public void attach(ObserverRI obs) throws RemoteException;
    public void detach(ObserverRI obs) throws RemoteException;

    public void startGame() throws RemoteException;
    
    // public void setSate(String state, ObserverRI obs) throws RemoteException;
    // public void setSate(String state) throws RemoteException;
    // public String getSate() throws RemoteException;

    public String getQeueuName() throws RemoteException;
}
