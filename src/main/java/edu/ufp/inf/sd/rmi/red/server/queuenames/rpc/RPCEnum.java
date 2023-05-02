package edu.ufp.inf.sd.rmi.red.server.queuenames.rpc;

public enum RPCEnum {
    
    RPC_SEARCH_LOBBIES("rpc-search-lobbies"),
    RPC_GET_PLAYERS("rpc-get-players"),
    RPC_START_GAME("rpc-start-game"),
    RPC_START_GAME_GUI("rpc-start-game-gui"),
    RPC_GET_WQ_NAME("rpc-get-wq-name"),
    RPC_LOGIN("rpc-login");
    
    private String value;

    public String getValue() {
        return this.value;
    }
    
    private RPCEnum(String value) {
        this.value = value;
    }
    
}   
