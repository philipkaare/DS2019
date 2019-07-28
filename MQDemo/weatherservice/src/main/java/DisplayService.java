import com.rabbitmq.client.Channel;
        import com.rabbitmq.client.Connection;
        import com.rabbitmq.client.ConnectionFactory;
        import com.rabbitmq.client.DeliverCallback;
import org.json.*;

import java.util.HashMap;

public class DisplayService {
    private static final String EXCHANGE_NAME = "weather";
    private static final String QUEUE_NAME = "copenhagenWeather";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        channel.queueDelete(QUEUE_NAME);
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String queueName = channel.queueDeclare(QUEUE_NAME, false, false, false, new HashMap<>()).getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            JSONObject obj = new JSONObject(message);
            String main = obj.getJSONArray("weather").getJSONObject(0).getString("main");
            String description = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            double tempInC = obj.getJSONObject("main").getDouble("temp")-273.15;
            String formattedTempInC = String.format("%.1f", tempInC);
            System.out.println("Weather currently in Copenhagen: " + main + ", " + description + ", Temperature: " + formattedTempInC + "C");
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });


    }
}
