package Replica;
import java.util.regex.*;
import AjouterLigneFichier.AjouterLigneFichier ;
import LireDernierLigne.LireDerniereLigneFichier;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import sendFinout.SendFinout;

public class Replica {
    private static final String EXCHANGE_NAME = "SERVER";

    public static void main(String[] argv) throws Exception {

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("READER");

        //initializing the ajouterLigneFichier
        String path = "Replica/rep"+argv[0];
        AjouterLigneFichier ajouterLigneFichier = new AjouterLigneFichier(path);

        //initializing the lireDernierLigneFichier
        LireDerniereLigneFichier lireDerniereLigneFichier = new LireDerniereLigneFichier(path);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection(argv[0]);
        Channel channel = connection.createChannel(); // Corrected method name

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println("Hello here replica "+argv[0]+" server , you can see the message received and they are automatically stocked in rep "+argv[0]+"  :");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            if( message.equals("Read Last") ){
                System.out.println("Reader customer wants to read the last Line ! ");
                try {
                    sendFinout.send(lireDerniereLigneFichier.lireLigne());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                if(testStringFormat(message)){
                    ajouterLigneFichier.ajouterLigne(message);
                    System.out.println("le ligne :\""+message+"\" est ajouté avec succés dans rep"+argv[0]+"/fichier.txt !");

                }
                else{
                    System.out.println("Commande non reconnue a été envoyé par ClientReader !");
                }
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    public static boolean testStringFormat(String input) {
        // Expression régulière pour correspondre à "nombre espaces texte"
        String pattern = "\\s*+\\d+\\s+\\S+.*";

        // Création de l'objet Pattern
        Pattern p = Pattern.compile(pattern);

        // Création de l'objet Matcher
        Matcher m = p.matcher(input);

        // Vérification de la correspondance
        return m.matches();
    }
}


