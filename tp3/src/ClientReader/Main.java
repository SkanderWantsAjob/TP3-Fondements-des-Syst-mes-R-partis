package ClientReader;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import sendFinout.*;
import AjouterLigneFichier.AjouterLigneFichier;

import com.rabbitmq.client.*;

public class Main {
    private static final String EXCHANGE_NAME = "READCLIENT";
    private static final String QUEUE_NAME = "Reader"; // Replace with the queue name used by ReplicaClientRead

    public static void main(String []args) throws Exception{
        // initializing the scanner
        Scanner scanner = new Scanner(System.in);

        // initializing the AjouterLigneFichier
        AjouterLigneFichier ajoutLigne = new AjouterLigneFichier("ClientReader");

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("READ");

        // Set up RabbitMQ connection and channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        System.out.println("Hello! You are the reader customer. \n write ‘Read Last’ to read the last line :\n ");

        String message;
        AtomicBoolean firstMessageReceived = new AtomicBoolean(false);
        while(true){

            // Read user input
            message = scanner.nextLine();

            // sending it to all the channels connected to the exchange READ
            sendFinout.send(message);

            // Check if first message is received
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String ligneContent = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received from Replica: " + ligneContent);
            };
            GetResponse r = channel.basicGet(QUEUE_NAME, true);
            System.out.println(r);
            }

        }
    }
