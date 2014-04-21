package org.dmendezg.mqtt.ssl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.dmendezg.mqtt.Publisher;

public class SSLSocketFactoryGenerator {
	public static SSLSocketFactory getSocketFactory(String propsFilePath)
			throws Exception {

		Properties props = new Properties();
		props.load(Publisher.class.getClassLoader().getResourceAsStream(
				propsFilePath));

		String caCrtFile = props.getProperty("ssl.ca.certificate.path");
		String crtFile = props.getProperty("ssl.client.certificate.path");
		String keyFile = props.getProperty("ssl.client.key.path");
		String password = props.getProperty("ssl.client.key.passphrase");

		Security.addProvider(new BouncyCastleProvider());

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		X509Certificate caCert = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Files
						.readAllBytes(Paths.get(caCrtFile))));

		X509Certificate cert = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Files
						.readAllBytes(Paths.get(crtFile))));

		File privateKeyFile = new File(keyFile);
		PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
				.build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
				.setProvider("BC");

		Object object = pemParser.readObject();
		KeyPair kp;

		if (object instanceof PEMEncryptedKeyPair) {
			kp = converter.getKeyPair(((PEMEncryptedKeyPair) object)
					.decryptKeyPair(decProv));
		} else {
			kp = converter.getKeyPair((PEMKeyPair) object);
		}

		pemParser.close();

		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(caKs);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", kp.getPrivate(), password.toCharArray(),
				new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
				.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		SSLContext context = SSLContext.getInstance("TLSv1");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();

	}
}