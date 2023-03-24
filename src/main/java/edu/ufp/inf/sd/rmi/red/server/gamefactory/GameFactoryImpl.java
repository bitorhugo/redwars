package edu.ufp.inf.sd.rmi.red.server.gamefactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.ufp.inf.sd.rmi.red.model.db.DBI;
import edu.ufp.inf.sd.rmi.red.model.gamesession.GameSessionRI;
import edu.ufp.inf.sd.rmi.red.model.gamesession.RemoteGameSessionExpiredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserAlreadyRegisteredException;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI {

    private DBI db;
    
    public GameFactoryImpl() throws RemoteException {
        super();
    }

    public GameFactoryImpl(DBI db) throws RemoteException {
        super();
        this.db = db;
    }

    @Override
    public GameSessionRI login(String username, String secret) throws RemoteException {
        User u = this.db.select(username, secret).orElseThrow(RemoteUserNotFoundException::new);
        return u.getSession().orElseThrow(RemoteGameSessionExpiredException::new);
    }

    @Override
    public GameSessionRI register(String username, String secret) throws RemoteException {
        User u = this.db.insert(username, secret).orElseThrow(RemoteUserAlreadyRegisteredException::new);
        return u.getSession().orElseThrow(RemoteGameSessionExpiredException::new);
    }

    
}
