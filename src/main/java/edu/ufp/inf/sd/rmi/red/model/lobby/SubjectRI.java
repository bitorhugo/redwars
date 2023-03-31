package edu.ufp.inf.sd.rmi.red.model.lobby;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.ufp.inf.sd.rmi.red.client.ObserverRI;

public interface SubjectRI extends Remote {
    public void attach(ObserverRI obs) throws RemoteException;
    public void detach(ObserverRI obs) throws RemoteException;
    public void setSate(String state) throws RemoteException;
    public String getSate() throws RemoteException;
}
