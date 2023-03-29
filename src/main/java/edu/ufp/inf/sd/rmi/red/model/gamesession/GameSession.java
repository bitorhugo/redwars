package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.ufp.inf.sd.rmi.red.model.token.Token;

public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private Token token;
    // availableGames will contain all games that were created
    // the integer will represent the game ID
    // the String will be the absolute path on the server where the diretory of savegame.properties file of that specific game is stored
    // the save folder will be called 'saves' and each sub-directory name will be the ID of the game
    // e.g.
    // saves/
    //      0/savegame.properties
    //      1/savageame/properties
    Map<Integer, List<String>> availableGames = new ConcurrentHashMap<>();
    
    public GameSession(Token token) throws RemoteException {
        super();
        this.token = token;
        this.availableGames.put(0, new ArrayList<>());
        this.availableGames.put(1, new ArrayList<>());
        this.availableGames.put(2, new ArrayList<>());
    }

    
    
    @Override
    public void attach() throws RemoteException {
        System.out.println("hello from new game");
    }

    @Override
    public List<Integer> availableGames() throws RemoteException {
        return new ArrayList<Integer>(this.availableGames.keySet());
    }
        
}
