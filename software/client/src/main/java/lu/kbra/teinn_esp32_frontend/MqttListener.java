package lu.kbra.teinn_esp32_frontend;

import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttListener {

	public static final int PORT = 1883;

	private String broker = "tcp://localhost:1883"; // MQTT broker URL
	private String clientId = "JavaMQTTClient";

	private MqttClient client;

	public MqttListener(String host, int port, String clientId) {
		this.broker = "tcp://" + host + ":" + port;
		this.clientId = clientId;

		try {
			this.client = new MqttClient(broker, clientId, new MqttDefaultFilePersistence("/tmp"));

			// Define the connection options
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);

			// Connect to the broker
			System.out.println("Connecting to broker: " + broker);
			client.connect(connOpts);
			System.out.println("Connected");
		} catch (MqttException e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void registerListener(String topic, Consumer<MqttMessage> consumer) {
		try {
			client.subscribe(topic, (topicName, msg) -> {
				consumer.accept(msg);
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

}
