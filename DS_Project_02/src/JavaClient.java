import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import chord_auto_generated.FileStore;
import chord_auto_generated.NodeID;
import chord_auto_generated.RFile;
import chord_auto_generated.RFileMetadata;
import chord_auto_generated.SystemException;
import chord_auto_generated.FileStore.Client;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class JavaClient {
	public static void main(String[] args) {
		// if (args.length != 3) {
		// System.out.println("Please enter simple/secure [ip] [port]");
		// System.exit(0);
		// }
		try {
			TTransport transport;
			// transport = new TSocket("simple", Integer.valueOf("9091"));
			transport = new TSocket("localhost", 9091);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			FileStore.Client client = new FileStore.Client(protocol);
//			perform(client);
			write(client);
			read(client);
			transport.close();
		} catch (TException x) {
			x.printStackTrace();
		}
	}

	private static void read(Client client) throws SystemException, TException {

		
		String key = "B9836ED79978A750B8D0F3F55A822B504BF0E666F9FCABCE66CD7F038E7CA94F";
		key = key.toLowerCase();

		NodeID succNode = client.findSucc(key);
		String predIP = succNode.ip;
//		predIP = "127.0.0.1";
		int predPort = succNode.port;

		try {
			TSocket transport = new TSocket(predIP, predPort);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			chord_auto_generated.FileStore.Client client1 = new chord_auto_generated.FileStore.Client(protocol);
			System.out.println("Reading file location : "+predPort);
			
			RFile rFile = new RFile();

			rFile=client1.readFile("Example.txt","chetan");
			
			System.out.println(rFile.getContent());
			System.out.println(rFile.getMeta().getVersion());
			System.out.println(rFile.getMeta().getContentHash());
			
			System.out.println("Reading file Done----------");

			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
			
	}

	private static void perform(FileStore.Client client) throws TException {
		System.out.println("Hi...from perform");
		String user = "chetan";
		String filename = "exmaple.txt";
		String keyString = user + ":" + filename;
		String key = sha_256(keyString);
		key = "B9836ED79978A750B8D0F3F55A822B504BF0E666F9FCABCE66CD7F038E7CA94F";
		key = "0000000000000000000000000000000000000000000000000000000000000000";
		key = key.toLowerCase();
		NodeID succNode = client.findSucc(key);
		System.out.println("=======================================================");
		System.out.println("KEY : " + key);
		System.out.println("=======================================================");
		System.out.println("Node ID : " + succNode.id + ": NODE IP : " + succNode.ip + "NODE PORT : " + succNode.port);
	}
	
	private static void write(Client client) throws SystemException, TException {
		
		String key = "B9836ED79978A750B8D0F3F55A822B504BF0E666F9FCABCE66CD7F038E7CA94F";
		key = key.toLowerCase();

		NodeID succNode = client.findSucc(key);
		String predIP = succNode.ip;
//		predIP = "127.0.0.1";
		int predPort = succNode.port;

		try {
			TSocket transport = new TSocket(predIP, predPort);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			chord_auto_generated.FileStore.Client client1 = new chord_auto_generated.FileStore.Client(protocol);
			System.out.println("Writting file location : "+predPort);
			
			RFile rFile = new RFile();
			
//			rFile.setContent("This is the NEW file created ");
			rFile.setContent("More Updated Files content");

			RFileMetadata localMeta = new RFileMetadata();
			localMeta.setFilename("Example.txt");
			localMeta.setOwner("chetan"); // Is this the client or server probably Client
			localMeta.setVersion(0);
			rFile.setMeta(localMeta );
			client1.writeFile(rFile);
			System.out.println("Writting file Done----------");

			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	public static String sha_256(String currentID) {
		System.out.println("-------->" + currentID);
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(currentID.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}