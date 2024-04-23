package ClientReader;

import java.util.Scanner;
import  java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import LireDernierLigne.LireDerniereLigneFichier;
import sendFinout.*;
import AjouterLigneFichier.AjouterLigneFichier;

import com.rabbitmq.client.*;

public class Main {
    private static final String EXCHANGE_NAME = "READER";

    public static void main(String[] args) throws Exception {
        // initializing the scanner
        Scanner scanner = new Scanner(System.in);
        String text ;

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("SERVER");

        //initializing the ajouterLigneFichier
        String path = "ClientReader";
        AjouterLigneFichier ajouterLigneFichier = new AjouterLigneFichier(path);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel(); // Corrected method name

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println("Hello here reader customer  server :");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(message);

        };
        Vector<String> messages = new Vector<String>(3);

        while(true){
            text = scanner.nextLine();
            sendFinout.send(text);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        }
    }
    }


