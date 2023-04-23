package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverRI extends Remote {
    public int getId() throws RemoteException;
    public String getUsername() throws RemoteException;
    public int getCommander() throws RemoteException;
    // public void update() throws RemoteException;
    public void startGame(String EXCHANGE_NAME) throws RemoteException;
}
