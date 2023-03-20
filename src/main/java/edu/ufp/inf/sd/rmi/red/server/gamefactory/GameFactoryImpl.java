package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    public GameFactoryImpl()  throws RemoteException {
        super();
    }

    @Override
    public void hello() throws RemoteException {
        System.out.println("Hello From Game Factory Impl");
        
    }
    
    
}
