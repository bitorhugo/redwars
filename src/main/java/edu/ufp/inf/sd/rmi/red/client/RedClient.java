package edu.ufp.inf.sd.rmi.red.client;

import engine.Game;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Exchange;

import edu.ufp.inf.sd.rmi.red.client.exchange.ExchangeEnum;



/**
 * @author Vitor Santos
 */
public class RedClient {

    private transient Connection conn;
    private transient Channel chan;

    
    public RedClient (String args[]) {
        this.connectToBroker(args[0]);
        this.setupRabbitContext();
        this.startGame();
    }



    private void connectToBroker (String host) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        try {
            this.conn = factory.newConnection();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Connection {0} created.", this.conn);
            this.chan = this.conn.createChannel();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Channel {0} created.", this.chan);
        } catch (IOException | TimeoutException e) {
            System.err.println("ERROR: Not able to open connection with RabbitMQ Services");
            System.exit(-1);
        }
    }

    private void setupRabbitContext() {
        try {
            this.chan.exchangeDeclare(ExchangeEnum.AUTHEXCHANGENAME.getValue(), "fanout");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Exchange {0} declared.",
                                                            ExchangeEnum.AUTHEXCHANGENAME.getValue());
            this.chan.exchangeDeclare(ExchangeEnum.LOBBIESEXCHANGENAME.getValue(), "fanout");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Success! Exchange {0} declared.",
                                                            ExchangeEnum.LOBBIESEXCHANGENAME.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        new Game();
    }

    
    public static void main(final String[] args) {
        new RedClient(args);
    }
}

