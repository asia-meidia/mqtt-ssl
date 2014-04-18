package org.dmendezg.mqtt.client;

import java.sql.Timestamp;

import javax.net.SocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class Client implements MqttCallback {

	private String clientId;
	private MqttClient client;
	private String brokerUrl;
	private MqttConnectOptions conOpt;
	private boolean clean;
	private String password;
	private String userName;

	public Client(String brokerUrl, String clientId, boolean cleanSession,
			String userName, String password, boolean ssl,
			SocketFactory socketFactory) throws MqttException {
		this.brokerUrl = brokerUrl;
		this.clientId = clientId;
		this.clean = cleanSession;
		this.userName = userName;
		this.password = password;

		String tmpDir = System.getProperty("java.io.tmpdir");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(
				tmpDir);

		try {

			conOpt = new MqttConnectOptions();
			conOpt.setCleanSession(clean);

			if (ssl = true) {
				conOpt.setSocketFactory(socketFactory);
			}

			if (password != null) {
				conOpt.setPassword(this.password.toCharArray());
			}
			if (userName != null) {
				conOpt.setUserName(this.userName);
			}
			client = new MqttClient(this.brokerUrl, this.clientId, dataStore);
			client.setCallback(this);

		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connectionLost(Throwable cause) {
		System.out.println("Client " + clientId + ": " + "Connection to "
				+ brokerUrl + " lost!" + cause);
		System.exit(1);
	}

	public void messageArrived(String topic, MqttMessage message)
			throws Exception {
		String time = new Timestamp(System.currentTimeMillis()).toString();
		System.out.println("Client " + clientId + ": "
				+ "Message received for topic " + topic + " and QoS "
				+ message.getQos() + " at time " + time);
		System.out.println("Client " + clientId + ": " + "Message payload: "
				+ new String(message.getPayload()));

	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Client " + clientId + ": " + "Delivery Completed");
	}

	public void connect() throws MqttSecurityException, MqttException {
		System.out.println("Client " + clientId + ": " + "Connecting to "
				+ client.getServerURI());
		client.connect(conOpt);
		System.out.println("Client " + clientId + ": " + "Connected");
	}

	public void publish(String topic, MqttMessage message)
			throws MqttSecurityException, MqttException {
		String time = new Timestamp(System.currentTimeMillis()).toString();
		System.out.println("Client " + clientId + ": " + "Publishing at: "
				+ time + " to topic \"" + topic + "\" qos " + message.getQos());
		client.publish(topic, message);
		System.out.println("Client " + clientId + ": " + "Message Published");
	}

	public void subscribe(String topicName, int qos) throws MqttException {
		System.out.println("Client " + clientId + ": "
				+ "Subscribing to topic \"" + topicName + "\" qos " + qos);
		client.subscribe(topicName, qos);
	}

	public void disconnect() throws MqttException {
		client.disconnect();
		System.out.println("Client " + clientId + ": " + "Disconnected");
	}
}
