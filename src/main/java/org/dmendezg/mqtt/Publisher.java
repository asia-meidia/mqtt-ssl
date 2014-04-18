package org.dmendezg.mqtt;

import javax.net.SocketFactory;

import org.dmendezg.mqtt.client.Client;
import org.dmendezg.mqtt.ssl.SSLSocketFactoryGenerator;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Publisher {
	
	public static void main(String[] args) {

		int qos = 2;
		String broker = "localhost";
		int port = 8883;
		String clientId = "myPublisher";
		String pubisherTopic = "topic/foo";
		boolean cleanSession = true;
		boolean ssl = true;
		String password = "publisher"; // The password you set when you added the user to the passwords file.
		String userName = "publisher";
		String protocol = "ssl://";
		String payload = "Hello World";
		SocketFactory socketFactory = null;

		String url = protocol + broker + ":" + port;

		try {
			
			if (ssl) {
				socketFactory = SSLSocketFactoryGenerator.getSocketFactory("publisher.properties");
			}		
			
			Client client = new Client(url, clientId, cleanSession, userName,
					password, ssl, socketFactory);

			client.connect();

			MqttMessage message = new MqttMessage(payload.getBytes());
			message.setQos(qos);

			client.publish(pubisherTopic, message);

			client.disconnect();

		} catch (MqttException me) {

			System.out.println("There has been an error. ");
			System.out.println("Reason: " + me.getReasonCode());
			System.out.println("Message: " + me.getMessage());
			System.out
					.println("Localized Message: " + me.getLocalizedMessage());
			System.out.println("Cause: " + me.getCause());
			System.out.println("Exception Stack Trace: " + me);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
