

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
  public static void main(String [] args) {

//    if (args.length != 3) {
//      System.out.println("Please enter simple/secure [ip] [port]");
//      System.exit(0);
//    }

    try {
      TTransport transport;
//        transport = new TSocket("simple", Integer.valueOf("9091"));
		transport  = new TSocket("localhost",9091);

        transport.open();

      TProtocol protocol = new  TBinaryProtocol(transport);
      FileStore.Client client = new FileStore.Client(protocol);

      perform(client);

      transport.close();
    } catch (TException x) {
      x.printStackTrace();
    } 
  }

  private static void perform(FileStore.Client client) throws TException
  {
		System.out.println("Hi...from perform");
		String user = "chetan";
		String filename = "exmaple.txt";
		String keyString = user+":"+filename;
		String key = sha_256(keyString);
		
		NodeID succNode = client.findSucc(key);
		System.out.println("=======================================================");
		System.out.println("KEY : "+key);
		System.out.println("=======================================================");
		System.out.println("Node ID : "+succNode.id+": NODE IP : "+succNode.ip);
		

  }
	public static String sha_256(String currentID) {
		// TODO Auto-generated method stub
		// https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
		MessageDigest digest;
		String encoded = "";
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(currentID.getBytes(StandardCharsets.UTF_8));
			encoded = Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return encoded;	
	}
}