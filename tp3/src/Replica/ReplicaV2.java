package Replica;
import java.util.Vector;
import java.util.regex.*;
import LireTousFichier.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import sendFinout.SendFinout;

public class ReplicaV2 {
    private static final String EXCHANGE_NAME = "SERVER";

    public static void main(String[] argv) throws Exception {

        //initializing the sendFinout class
        SendFinout sendFinout = new SendFinout("READER");

        //initializing the lireTousLigneFichier
        String path = "Replica/rep"+argv[0];
        ReadAllFile lireTousFichier  = new ReadAllFile(path);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection(argv[0]);
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println("Hello here replica "+argv[0]+" server version 2 , you will see the lines sent to customer read 2 when he wants to read all the file :");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            if( message.equals("Read All") ){
                System.out.println("Reader customer version 2  wants to read all Lines ! ");
                try {
                    Vector<String> lines = lireTousFichier.read();
                    for(String line : lines){
                        sendFinout.send(line);
                    }
                } catch (Exception e) {
                    System.out.println("there's an exception in the sendFinout lastLigne to the cleint reader 2! ");
                    throw new RuntimeException(e);
                }
            }
            else{
                System.out.println("Commande non reconnue a été envoyé par ClientReader2 !");
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}


