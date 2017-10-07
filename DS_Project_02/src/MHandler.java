import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.thrift.TException;

import chord_auto_generated.FileStore.Iface;
import chord_auto_generated.NodeID;
import chord_auto_generated.RFile;
import chord_auto_generated.SystemException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MHandler implements Iface {

	TreeMap<String, NodeID> myDHT = new TreeMap<String, NodeID>();
	TreeMap<String, List<fingerTable>> nodesFingerTable = new TreeMap<String, List<fingerTable>>();
	private String currentIp;
	private int currentPort;
	private NodeID currentNode = null;
	private List<NodeID> node_ID_list;

	// nodesFingerTable.put("NODE_ID_HASH_Value", new fingerTable("NS01"));
	@Override
	public void writeFile(RFile rFile) throws SystemException, TException {
		// TODO Auto-generated method stub

	}

	@Override
	public RFile readFile(String filename, String owner) throws SystemException, TException {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public void setFingertable(List<NodeID> node_list) throws TException {
		System.out.println("Set Finger table");
		System.out.println(node_list);
		node_ID_list = node_list;

		for (NodeID nodeID : node_list) {
			myDHT.put(nodeID.getId(), nodeID);
		}
		// System.out.println(myDHT);
	}

	@Override
	public NodeID findSucc(String key) throws SystemException, TException {
		// TODO Auto-generated method stub
		System.out.println("In the Find Succ");
		try {
			currentIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		currentPort = JavaServer.port;
		String currentID = (currentIp + ":" + currentPort);
		String encoded = sha_256(currentID);
		System.out.println("Setting current Nodes : " + encoded);
		currentNode = new NodeID();
		// currentNode.setIp(currentIp);
		// currentNode.setPort(currentPort);
		// currentNode.setId(encoded);
		try {
			currentNode.id = encoded;
			currentNode.port = currentPort;
			currentNode.ip = currentIp;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}
		System.out.println("Going to find Pred");
		try {
			NodeID n_dash = new NodeID();
			n_dash = findPred(key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getNodeSucc();
	}

	public String sha_256(String currentID) {

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

	@Override
	public NodeID findPred(String key) throws SystemException, TException {
		// TODO Auto-generated method stub
		System.out.println("In the Find Pred");

		NodeID n_dash = new NodeID();
		try {
			n_dash = currentNode;
			while (!compareIDs(n_dash, key)) {
				n_dash = closest_preceding_fing(n_dash, key);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return n_dash;
	}

	private boolean compareIDs(NodeID n_dash, String key) {
		NodeID n_dash_successor = new NodeID();

		n_dash_successor = node_ID_list.get(0);
		System.out.println("In the compareIDs");

		// the value 0 if the argument string is equal to this string;
		// a value less than 0 if this string is lexicographically less than the string
		// argument
		// value greater than 0 if this string is lexicographically greater than the
		// string argument.
		if (key.compareTo(n_dash.id) > 0 && key.compareTo(n_dash_successor.id) < 0) {
			return true;
			// Key lies between the current nodes Id and its successsors id.
		}

		return false;
	}

	private boolean compareIDs(NodeID iteratorNode, String key, NodeID n_dash) {
		if (iteratorNode.id.compareTo(n_dash.id) > 0 && iteratorNode.id.compareTo(key) < 0) {
			return true;
			// iteratoNode lies between the nodes Id and Key.
		}
		return false;
	}

	public NodeID closest_preceding_fing(NodeID n_dash, String key) {
		int m = 255, i = 0;
		NodeID iteratorNode = new NodeID();
		System.out.println("In the closest_preceding_fing");

		try {
			for (i = m; i >= 1; i--) {
				iteratorNode = node_ID_list.get(i);
				// iteratorNode = myDHT.get(i);
				if (compareIDs(iteratorNode, key, n_dash)) {
					return iteratorNode;
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return n_dash;

		/*
		 * for(i=m downto 1) if()finger[i].node E (n,id)) return finger[i].node
		 * 
		 * return n;
		 */
	}

	@Override
	public NodeID getNodeSucc() throws SystemException, TException {
		return this.node_ID_list.get(0);
	}

}
