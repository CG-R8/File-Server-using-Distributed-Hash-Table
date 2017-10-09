import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;

import chord_auto_generated.FileStore;
import chord_auto_generated.NodeID;

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
			perform(client);
			transport.close();
		} catch (TException x) {
			x.printStackTrace();
		}
	}

	private static void perform(FileStore.Client client) throws TException {
		System.out.println("Hi...from perform");
		String user = "chetan";
		String filename = "exmaple.txt";
		String keyString = user + ":" + filename;
		String key = sha_256(keyString);
		key = "B9836ED79978A750B8D0F3F55A822B504BF0E666F9FCABCE66CD7F038E7CA94F";
		key = key.toLowerCase();
		NodeID succNode = client.findSucc(key);
		System.out.println("=======================================================");
		System.out.println("KEY : " + key);
		System.out.println("=======================================================");
		System.out.println("Node ID : " + succNode.id + ": NODE IP : " + succNode.ip + "NODE PORT : " + succNode.port);
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