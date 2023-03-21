package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.model.db.DB;
import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DB db;
    
    public GameFactoryImpl()  throws RemoteException {
        super();
    }

    public GameFactoryImpl(DB db)  throws RemoteException {
        super();
        this.db = db;
    }

    @Override
    public SessionToken login(User user) throws RemoteException {
        return this.db.selectToken(user).orElseThrow();
    }

    
}
