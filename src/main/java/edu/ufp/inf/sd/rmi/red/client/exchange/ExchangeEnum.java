package edu.ufp.inf.sd.rmi.red.client.exchange;

public enum ExchangeEnum {
    
    AUTHEXCHANGENAME("auth"),
    LOBBIESEXCHANGENAME("lobbies");
    
    private String value;

    public String getValue() {
        return this.value;
    }
    
    private ExchangeEnum(String value) {
        this.value = value;
    }
    
}   
