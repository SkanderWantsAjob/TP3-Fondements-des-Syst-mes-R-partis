package ClientReader;

import java.util.Scanner;
import java.util.Vector;
import java.util.Scanner;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import sendFinout.*;
import AjouterLigneFichier.AjouterLigneFichier;

import com.rabbitmq.client.*;

public class Main {
    private static final String EXCHANGE_NAME = "READCLIENT";
    private static final String QUEUE_NAME = "Reader"; // Replace with the queue name used by ReplicaClientRead

    public static void main(String[] args) throws Exception {
        // initializing the scanner
        Scanner scanner = new Scanner(System.in);

        // initializing the AjouterLigneFichier
        AjouterLigneFichier ajoutLigne = new AjouterLigneFichier("ClientReader");

        // initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("READ");


        System.out.println("Hello! You are the reader customer. \n Write ‘Read Last’ to read the last line :\n ");

        String message;

        Vector<String> messagesRetreive = new Vector<>();
        AtomicInteger messageCount = new AtomicInteger();

        while (true) {
            // Set up RabbitMQ connection and channel
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

            messagesRetreive.clear();

            // Read user input
            message = scanner.nextLine();

            // sending it to all the channels connected to the exchange READV2
            sendFinout.send(message);


            channel.basicConsume(QUEUE_NAME, false, (consumerTag, delivery) -> {

                String receivedMessage = new String(delivery.getBody(), "UTF-8");

                //addind the result to lines
                messagesRetreive.add(receivedMessage);

                // Increase message count
                messageCount.getAndIncrement();

                // Acknowledge the message
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                // Check if reached 3 messages, then cancel consumer
                if (messageCount.get() >= 3) {
                    //channel.basicCancel(consumerTag);
                    channel.basicCancel(consumerTag);
                    System.out.println("these lines wil be aded to the ClientReader/fichier.txt : ");

                    int lineNum1 = extractFirstNumber(messagesRetreive.get(0));
                    int lineNum2 = extractFirstNumber(messagesRetreive.get(1));
                    int lineNum3 = extractFirstNumber(messagesRetreive.get(2));

                    System.out.println(lineNum3);
                    System.out.println(lineNum1);
                    System.out.println(lineNum2);

                    if(lineNum1 >= lineNum2){
                        if(lineNum1 >= lineNum3){
                            System.out.println(messagesRetreive.get(0));
                            ajoutLigne.ajouterLigne(messagesRetreive.get(0));
                        }else{
                            System.out.println(messagesRetreive.get(2));
                            ajoutLigne.ajouterLigne(messagesRetreive.get(2));
                        }
                    }else { // 1<2
                        if(lineNum2 >= lineNum3){
                            System.out.println(messagesRetreive.get(1));
                            ajoutLigne.ajouterLigne(messagesRetreive.get(1));
                        }else{
                            System.out.println(messagesRetreive.get(2));
                            ajoutLigne.ajouterLigne(messagesRetreive.get(2));
                        }
                    }
                }

            }, consumerTag -> {  });


        }

    }

    public static int extractFirstNumber(String input) {
        Scanner scanner = new Scanner(input);

        // Find the first occurrence of a number in the string
        while (scanner.hasNext()) {
            if (scanner.hasNextInt()) {
                int number = scanner.nextInt();
                scanner.close(); // Close the scanner
                return number;
            } else {
                scanner.next(); // Move to the next token if it's not an integer
            }
        }

        scanner.close(); // Close the scanner
        return -1; // Default value, you can change this according to your requirement
    }

}

