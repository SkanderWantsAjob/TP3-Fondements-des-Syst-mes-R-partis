package ClientReaderV2;
import java.util.regex.*;
import java.util.Scanner;
import  java.util.Vector;
import sendFinout.*;
import AjouterLigneFichier.AjouterLigneFichier;

import com.rabbitmq.client.*;

public class Main {
    private static final String EXCHANGE_NAME = "READER2";

    public static void main(String[] args) throws Exception {
        // initializing the scanner
        Scanner scanner = new Scanner(System.in);
        String text ;

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("SERVER2");

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

        while(true){
            text = scanner.nextLine();
            sendFinout.send(text);
            Thread.sleep(100);

            // Vector pour stocker les numéros de ligne
            Vector<Integer> lineNumbers = new Vector<Integer>();

            // Initialiser lineNumbers avec les numéros de ligne extraits des messages
            for (String message : messages) {
                lineNumbers.add(extractInteger(message));
            }

            // Tri des messages et des numéros de ligne en parallèle en utilisant le tri à bulle
            boolean switched = true;
            while (switched) {
                switched = false;
                for (int i = 0; i < lineNumbers.size() - 1; i++) {
                    if (lineNumbers.get(i) > lineNumbers.get(i + 1)) {
                        // Échanger les positions des numéros de ligne
                        int tempLineNumber = lineNumbers.get(i);
                        lineNumbers.set(i, lineNumbers.get(i + 1));
                        lineNumbers.set(i + 1, tempLineNumber);

                        // Échanger les positions des messages
                        String tempMessage = messages.get(i);
                        messages.set(i, messages.get(i + 1));
                        messages.set(i + 1, tempMessage);

                        switched = true;
                    }
                }
            }
            int multiplicityNumber =1;
            Vector<String> textFile = new Vector<String>();

            for (int i=0; i<messages.size()-1;i++) {
                if(messages.get(i).equals(messages.get(i+1))){
                    multiplicityNumber ++;
                }else{
                    if(multiplicityNumber >= 2){
                        textFile.add(messages.get(i));
                    }
                    multiplicityNumber = 1;
                }
            }
            // Vérifier le dernier message
            if (multiplicityNumber >= 2) {
                textFile.add(messages.lastElement());
            }

            for(String t : textFile){
                System.out.println(t);
            }

            lineNumbers.clear();
            textFile.clear();
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


