package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.model.db.DBI;
import edu.ufp.inf.sd.rmi.red.model.session.Session;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DBI db;
    
    public GameFactoryImpl()  throws RemoteException {
        super();
    }

    public GameFactoryImpl(DBI db)  throws RemoteException {
        super();
        this.db = db;
    }

    @Override
    public Session login(String username, String secret) throws RemoteException {
        User u = this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
        return u.getSession();
    }

    @Override
    public Session register(String username, String secret) throws RemoteException {
        User u = this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
        return u.getSession();
    }

    
}
