package edu.ufp.inf.sd.rmi.red.model.lobby;

import java.util.ArrayList;
import java.util.List;

public class Lobby {

    private List<String> players = new ArrayList<>(2);
    private String mapname;
    
    public Lobby(String mapname, String player) {
        this.mapname = mapname;
        this.players.add(player);
    }

    public void addPlayers(String username) {
        switch (mapname) {
        case "small":
            this.addPlayerSmallMap(username);
            break;
        case "fours":
            this.addPlayerBigMap(username);
            break;
        }
    }

    private void addPlayerSmallMap(String username) {
        if (!this.players.contains(username) &&
            this.players.size() < 2) {
            this.players.add(username);
        }
    }

    private void addPlayerBigMap(String username) {
        if (!this.players.contains(username) &&
            this.players.size() < 4) {
            this.players.add(username);
        }        
    }
    
    public void removePlayer(String username) {
        if (this.players.contains(username)) {
            this.players.remove(username);
        }
    }
    
}
