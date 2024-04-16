package ClientReaderV2;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import sendFinout.*;
import AjouterLigneFichier.AjouterLigneFichier;

import com.rabbitmq.client.*;

public class Main {
    private static final String EXCHANGE_NAME = "READCLIENT";
    private static final String QUEUE_NAME = "Reader";

    public static void main(String []args) throws Exception{
        // initializing the scanner
        Scanner scanner = new Scanner(System.in);

        // initializing the AjouterLigneFichier
        AjouterLigneFichier ajoutLigne = new AjouterLigneFichier("ClientReaderV2");

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("READ");

        // Set up RabbitMQ connection and channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        System.out.println("Hello! You are the reader customer v2. \n write ‘Read All’ to read the all lines :\n ");

        String message;
        while(true){

            // Read user input
            message = scanner.nextLine();

            // sending it to all the channels connected to the exchange READ
            sendFinout.send(message);

            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                    String receivedMessage = new String(delivery.getBody(), "UTF-8");
                    System.out.println("Received message from ReplicaClientRead: " + receivedMessage);

                    // writing it in the file fichier.txt in the repository ClientWriter
                    ajoutLigne.ajouterLigne(receivedMessage);

                }, consumerTag -> {
            });
        }
    }
}
