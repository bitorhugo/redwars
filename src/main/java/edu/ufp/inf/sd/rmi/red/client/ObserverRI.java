package edu.ufp.inf.sd.rmi.red.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverRI extends Remote {
    public void update() throws RemoteException;
    public void startGame() throws RemoteException;
}
