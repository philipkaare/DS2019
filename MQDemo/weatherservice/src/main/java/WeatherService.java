import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    private static final String EXCHANGE_NAME = "weather";

    private static String getWeather() throws Exception
    {
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=Copenhagen,dk&APPID=9f692ccb9e5a170d9c429ff5ee7d38a9");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel())
        {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            while (true) {

                String message = getWeather();

                channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_BASIC, message.getBytes("UTF-8"));
                System.out.println("Sent '" + message);
                Thread.sleep(10000);

            }
        }

    }
}
