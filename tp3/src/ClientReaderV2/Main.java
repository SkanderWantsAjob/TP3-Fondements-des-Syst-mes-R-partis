package ClientReaderV2;
import java.util.regex.*;
import java.util.Scanner;
import  java.util.Vector;
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
        String path = "ClientReaderV2";
        AjouterLigneFichier ajouterLigneFichier = new AjouterLigneFichier(path);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel(); // Corrected method name

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println("Hello here reader customer version 2 :");

        Vector<String> messages = new Vector<String>(3);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            messages.add(message); // Ajouter le message au vecteur
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

        String  lastLigne = "" ;
        while(true){
            text = scanner.nextLine();
            sendFinout.send(text);
            Thread.sleep(100);

            int lastLingeNumber = 0 , lastReadLigneNumber ;

            for (String msg: messages ) {
                lastReadLigneNumber = extractInteger(msg);

                // testing if the extraction operation is well done
                if (lastReadLigneNumber != -1) {

                    // we want to read the last ligne => the biggest lastReadLigneNumber
                    if(lastReadLigneNumber > lastLingeNumber){
                        lastLingeNumber = lastReadLigneNumber ;
                        lastLigne = msg ;
                    }

                } else {
                    System.out.println(" message recieved from the SERVER ne convient pas l'expression reguliere imposé ! ");
                }
            }
            if(lastLigne.equals("")){
                System.out.println(" there's no message recieve from Replica !  ");
            }else{
                System.out.println(" after recieving all the messages from the opened server (replica ) , the last ligne writed by the writer customer :");
                System.out.println(lastLigne+"\n");

            }

            messages.clear();
        }
    }
    public static int extractInteger(String message) {
        // Expression régulière pour extraire l'entier au début du message
        String pattern = "^\\s*(\\d+)\\s+.*";

        // Création de l'objet Pattern
        Pattern p = Pattern.compile(pattern);

        // Création de l'objet Matcher
        Matcher m = p.matcher(message);

        // Vérification de la correspondance
        if (m.find()) {
            // Si une correspondance est trouvée, extraire et retourner l'entier
            return Integer.parseInt(m.group(1));
        } else {
            // Si aucune correspondance n'est trouvée, retourner -1 ou une valeur par défaut
            return -1;
        }
    }
}


