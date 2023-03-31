package edu.ufp.inf.sd.rmi.red.model.lobby;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Lobby implements Serializable {

    private UUID id;
    private List<String> players = new ArrayList<>();
    private String mapname;
    
    public Lobby(String mapname, String player) {
        this.id = UUID.randomUUID();
        this.mapname = mapname;
        this.players.add(player);
    }

    public String getMapname() {
        return this.mapname;
    }

    public UUID getID() {
        return this.id;
    }

    public void addPlayers(String username) {
        switch (mapname) {
        case "SmallVs":
            this.addPlayerSmallMap(username);
            break;
        case "FourCorners":
            this.addPlayerBigMap(username);
            break;
        }
    }

    // CAUTION: DURING DEV, ADDPLAYERS WILL ALLOW THE OWNER OF THE LOBBY TO ENTER AS ANTOHER PLAYER!!!!!
    private void addPlayerSmallMap(String username) {
        if (!this.players.contains(username) &&
            this.players.size() < 2) {
            this.players.add(username);
            System.out.println(username + " in lobby");
        }
    }

    private void addPlayerBigMap(String username) {
        if (!this.players.contains(username) &&
            this.players.size() < 4) {
            this.players.add(username);
            System.out.println(username + " in lobby");
        }        
    }
    
    public void removePlayer(String username) {
        if (this.players.contains(username)) {
            this.players.remove(username);
        }
    }

    public List<String> players() {
        return this.players;
    }
    
}
