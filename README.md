SERVER
-

The example we are going to follow is very simple. We have a MQTT server, one publisher and one subscriber. The publisher will send a message to the server with a certain topic and the subscriber, that will subscribed to that same topic will receive it. We will include security in two steps, first to the connections with SSL certificates and finally with user/password authentication.

But first things first. Let’s start with the MQTT server. We will use Mosquitto, which is an open source implementation of an MQTT message broker. You can download it here for windows.

    http://mosquitto.org/files/binary/win32/mosquitto-1.2.3-install-win32.exe

Notice that once you execute the installation, Mosquitto will run as a service so you can go to Services in Windows and tell it to run automatically. Now you can go to the installation directory (i.e. C:\Program Files (x86)\mosquitto), open the command line and execute “mosquitto.exe”. Minimize the window.

The message broker is running now. But we need to secure the connections and also provide a list of users that will be allowed to use the broker. So lets move forward with the certification generation for the server. We will use OpenSSL for this (Yes, I know what just happened but they have fixed, haven’t they?) 

Download the version openssl-1.0.1g or later.

    http://slproweb.com/download/Win32OpenSSL_Light-1_0_1g.exe

It will add the bin folder under the installation to the path. If not do it manually. The following steps are from Paolo Patiernos’s Blog (http://www.embedded101.com/Blogs/PaoloPatierno/tabid/106/entryid/366/MQTT-over-SSL-TLS-with-the-M2Mqtt-library-and-the-Mosquitto-broker.aspx). You have a thorough explanation of the next steps there. Please keep track of all the passphrases that you generate during these steps since they will be asked several times.

These instructions create a certificate for the Certification Authority and the Server.

    openssl req -new -x509 -days 3650 -keyout m2mqtt_ca.key -out m2mqtt_ca.crt

    openssl genrsa -des3 -out m2mqtt_srv.key 1024
    openssl req -out m2mqtt_srv.csr -key m2mqtt_srv.key -new
    openssl x509 -req -in m2mqtt_srv.csr -CA m2mqtt_ca.crt -CAkey m2mqtt_ca.key -CAcreateserial -out m2mqtt_srv.crt -days 3650

We will also create certificates for the Publisher and Subscriber.

    openssl genrsa -des3 -out m2mqtt_pub.key 1024
    openssl req -out m2mqtt_pub.csr -key m2mqtt_pub.key -new
    openssl x509 -req -in m2mqtt_pub.csr -CA m2mqtt_ca.crt -CAkey m2mqtt_ca.key -CAcreateserial -out m2mqtt_pub.crt -days 3650

    openssl genrsa -des3 -out m2mqtt_sub.key 1024
    openssl req -out m2mqtt_sub.csr -key m2mqtt_sub.key -new
    openssl x509 -req -in m2mqtt_sub.csr -CA m2mqtt_ca.crt -CAkey m2mqtt_ca.key -CAcreateserial -out m2mqtt_sub.crt -days 3650

Now that we have a certificate for the server, let’s configure it, so that it runs with it. For that we will fill out the configuration file as follows. 

mosquitto.conf

    bind_address localhost
    port 8883
    password_file passfile
    cafile {PATH_TO_FILE}\m2mqtt_ca.crt
    certfile {PATH_TO_FILE}\m2mqtt_srv.crt
    keyfile {PATH_TO_FILE}\m2mqtt_srv.key
    tls_version tlsv1
    require_certificate true

You can see also that we will manage a password_file. The content of it is generated as follows. Run a command windows on Mosquitto’s installation folder and execute.

    mosquitto_passwd -c passfile subscriber
 
Insert password and confirm it. Same thing for publisher, only that in this case we will not create the file (no -c option)

    mosquitto_passwd passfile publisher

There it is, now everything is prepared. We can run mosquitto with the following command so that it gets configured with the configuration file we just created.

    mosquitto –c mosquitto.conf

Notice that it will ask for the password of the server’s key.

CLIENTS: PUBLISHER AND SUBSCRIBER
-

Let’s move forward with the clients, the publisher and subscriber. Create a new Maven project in Eclipse and add the next dependendencies and repositories in the pom.xml. You can see there that we will use the eclipse paho library for the clients and the bouncycastle for SSL certificates.

pom.xml

     <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    	<modelVersion>4.0.0</modelVersion>
    
    	<groupId>org.dmendezg.mqtt</groupId>
    	<artifactId>example</artifactId>
    	<version>0.0.1-SNAPSHOT</version>
    	<packaging>jar</packaging>
    	<repositories>
    		<repository>
    			<id>spring-milestones</id>
    			<url>http://repo.springsource.org/libs-milestone/</url>
    		</repository>
    	</repositories>
    	<dependencies>
    		<dependency>
    			<groupId>org.eclipse.paho</groupId>
    			<artifactId>mqtt-client</artifactId>
    			<version>0.4.0</version>
    		</dependency>
    		<dependency>
    			<groupId>org.bouncycastle</groupId>
    			<artifactId>bcprov-jdk15on</artifactId>
    			<version>1.48</version>
    		</dependency>
    		<dependency>
    			<groupId>org.bouncycastle</groupId>
    			<artifactId>bcpkix-jdk15on</artifactId>
    			<version>1.48</version>
    		</dependency>
    	</dependencies>
    </project>

We will have a base Client, and a SSL helper. I have created these so that the configuration gets easier, but you are free to use the library's client directly. Finally two more objects. The first one for a subscriber that connects to the server and waits for messages published into its topic list. And the Publisher that sends a message to a topic, that of course happens to be the same to the one from which the Subscriber is receiving from.

In order to run the publisher and subscriber properly we will need to fill the publisher and subscriber properties file with the information for the certificates we generated before.

publisher.properties

    ssl.ca.certificate.path={path_to_file}/m2mqtt_ca.crt
    ssl.client.certificate.path={path_to_file}/m2mqtt_pub.crt
    ssl.client.key.path={path_to_file}/m2mqtt_pub.key
    ssl.client.key.passphrase={password}

subscriber.properties

    ssl.ca.certificate.path={path_to_file}/m2mqtt_ca.crt
    ssl.client.certificate.path={path_to_file}/m2mqtt_sub.crt
    ssl.client.key.path={path_to_file}/m2mqtt_sub.key
    ssl.client.key.passphrase={password}
    
Now you just need to run both the subscriber and publisher. And don't forget to have the sever running!

