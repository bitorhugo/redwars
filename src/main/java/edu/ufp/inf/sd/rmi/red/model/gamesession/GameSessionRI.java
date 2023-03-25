package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameSessionRI extends Remote {
    public void test(String s) throws RemoteException;
}
