import com. rabbitmq. client. ConnectionFactory;
import com. rabbitmq.client.Connection;
import com. rabbitmq. client.Channel;

import java.util.Vector;

public class sendFinout {

    public static void send(String EXCHANGE_NAME) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                String message = "hello word ! ";

                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));

                System.out.println(" [x] Sent '" + message + "'");
        }
    }

    public static void main( String []args) throws Exception{
        sendFinout.send("Write");
    }


}