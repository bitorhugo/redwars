package edu.ufp.inf.sd.rmi.red.model.lobby;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;

public interface SubjectRI extends Remote {
    public List<String> players() throws RemoteException;
    public UUID getID() throws RemoteException;
    public String getMapname() throws RemoteException;
    public void attach(ObserverRI obs) throws RemoteException;
    public void detach(ObserverRI obs) throws RemoteException;
    public void setSate(String state) throws RemoteException;
    public String getSate() throws RemoteException;
}
