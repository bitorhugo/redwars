package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import edu.ufp.inf.sd.rmi.red.model.db.GameDBI;
import edu.ufp.inf.sd.rmi.red.model.token.Token;

public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private Token token;
    private GameDBI db;

    public GameSession(Token token, GameDBI db) throws RemoteException {
        super();
        this.token = token;
        this.db = db;
    }

    public Token getToken() {
        return this.token;
    }

    @Override
    public void attach() throws RemoteException {
        
    }

    @Override
    public void detach() throws RemoteException {
        
    }

    @Override
    public List<Integer> availableGames() throws RemoteException {
        return this.db.select().orElseThrow();
    }

    @Override
    public int createGame(String mapname) throws RemoteException {
        var id = this.db.insert(mapname).orElseThrow();
        System.out.println("Map ID:" + id);
        return id;
    }

    @Override
    public void cancelGame(int id) throws RemoteException {
        this.db.delete(id);
    }

}
