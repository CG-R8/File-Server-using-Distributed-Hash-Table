import java.util.List;
import java.util.TreeMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import chord_auto_generated.FileStore.Iface;
import chord_auto_generated.NodeID;
import chord_auto_generated.RFile;
import chord_auto_generated.SystemException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;

public class MHandler implements Iface {
	TreeMap<Integer, NodeID> myDHT = new TreeMap<Integer, NodeID>();
	TreeMap<String, List<fingerTable>> nodesFingerTable = new TreeMap<String, List<fingerTable>>();
	private String currentIp;
	public int currentPort;
	private NodeID currentNode = null;
	private static List<NodeID> node_ID_list;

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
		int i = 0;
		for (NodeID nodeID : node_list) {
			myDHT.put(i++, nodeID);
		}
	}

	@Override
	public NodeID findSucc(String key) throws SystemException, TException {
		System.out.println("In the Find Succ");
		NodeID n_dash = new NodeID();
		currentNode = new NodeID();
		currentPort = JavaServer.port;
		try {
			currentNode = getCurrentNode();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.out.println("Going to find Pred");
		try {
			n_dash = findPred(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Node ID : " + n_dash.id + ": NODE IP : " + n_dash.ip + "NODE PORT : " + n_dash.port);
		String predIP = n_dash.ip;
		int predPort = n_dash.port;
		NodeID succNode = new NodeID();
		try {
			TSocket transport = new TSocket(predIP, predPort);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			chord_auto_generated.FileStore.Client client = new chord_auto_generated.FileStore.Client(protocol);
			try {
				System.out.println("Geting the succ. from found pred");
				succNode = client.getNodeSucc();
			} catch (SystemException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Node ID : " + succNode.id + ": NODE IP : " + succNode.ip + "NODE PORT : " + succNode.port);
		return succNode;
	}

	@Override
	public NodeID findPred(String key) throws SystemException, TException {
		System.out.println("In the Find Pred");
		int counter = 0;
		NodeID n_dash = new NodeID();
		boolean isRPC_Done = false;
		try {
			n_dash = getCurrentNode();
			while ((!compareIDs_FirstNode(n_dash, key)) && counter < 5) {
				System.out.print(counter + " : ");
				n_dash = closest_preceding_fing(n_dash, key);
				System.out.println("First HOP is here : " + n_dash.getPort());
				n_dash = nextRpcCall(n_dash, key);
				isRPC_Done = true;
				counter++;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("FOUND: " + n_dash.id + " Port:" + n_dash.port);
		return n_dash;
	}

	private NodeID getCurrentNode() throws UnknownHostException {
		NodeID currentNode = new NodeID();
		currentNode.port = JavaServer.port;
		// currentNode.ip=InetAddress.getLocalHost().getHostAddress();
		// TODO change workaround for Ip address
		currentNode.ip = "127.0.0.1";
		String preHashingString = currentNode.ip + ":" + currentNode.port;
		currentNode.id = sha_256(preHashingString);
		System.out.println("CURRENT Node ID : " + currentNode.id + ": NODE IP : " + currentNode.ip + "NODE PORT : "
				+ currentNode.port);
		return currentNode;
	}

	public NodeID closest_preceding_fing(NodeID n_dash, String key) {
		int m = 255, i = 0;
		NodeID iteratorNode = new NodeID();
		System.out.println("In the closest_preceding_fing");
		try {
			for (i = m; i >= 1; i--) {
				iteratorNode = node_ID_list.get(i);
				// iteratorNode = myDHT.get(i);
				System.out.println("=====> " + i);
				if (compareIDs(iteratorNode, key, n_dash)) {
					return iteratorNode;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return n_dash;
	}

	@Override
	public NodeID getNodeSucc() throws SystemException, TException {
		if (node_ID_list.size() == 0) {
			try {
				SystemException exception = new SystemException();
				exception.setMessage("Calling same port again, Last entry is same as current node.Infinit loop");
				// System.exit(0);
				throw exception;
			} catch (SystemException e) {
				e.printStackTrace();
			}
		}
		return this.node_ID_list.get(0);
	}

	private NodeID nextRpcCall(NodeID targetNode, String key) throws SystemException, TException {
		NodeID predNode = new NodeID();
		currentPort = JavaServer.port;
		System.out.println("Calling NEXT RPC : " + targetNode.getPort() + "FROM : " + currentPort);
		if (currentPort == targetNode.getPort()) {
			try {
				SystemException exception = new SystemException();
				exception.setMessage("Calling same port again, Last entry is same as current node.Infinit loop");
				// System.exit(0);
				throw exception;
			} catch (SystemException e) {
				e.printStackTrace();
			}
		}
		String host = targetNode.getIp();
		int port = targetNode.getPort();
		System.out.println("HOST : " + host + " PORT: " + port);
		try {
			TSocket transport = new TSocket(host, port);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			chord_auto_generated.FileStore.Client client = new chord_auto_generated.FileStore.Client(protocol);
			try {
				System.out.println("Connecting client findPred");
				predNode = client.findPred(key);
			} catch (SystemException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return predNode;
	}

	private boolean compareIDs_FirstNode(NodeID n_dash, String key) {
		try {
			NodeID n_dash_successor = new NodeID();
			n_dash_successor = node_ID_list.get(0);
			System.out.println("-------------------[n_dash < Key > n_dash_successor]--------------");
			System.out.print(n_dash.id + ":" + n_dash.port);
			System.out.print(" < ");
			System.out.print(key);
			System.out.print(" > ");
			System.out.println(n_dash_successor.id + ":" + n_dash_successor.port);
			System.out.println("------------------------------------------------------------------");
			String first = n_dash.id;
			String second = key;
			String third = n_dash_successor.id;
			int left = (first.compareTo(second));
			int right = second.compareTo(third);
			int extreme = first.compareTo(third);
			String zero="0000000000000000000000000000000000000000000000000000000000000000";
			if (extreme > 0)
				if(zero.compareTo(second)<=0  &&  (right < 0))
				{//1
					return ((left > 0) && (right < 0));
				}else//29
				return ((left < 0) && (right > 0));
			else if (extreme < 0)
				return ((left < 0) && (right < 0));
			else
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean compareIDs(NodeID iteratorNode, String key, NodeID n_dash) {
		try {
			System.out.println("-------------------[NODE_ID < IteratorNode > KEY]------------------");
			System.out.print(n_dash.id + ":" + n_dash.port);
			System.out.print(" < ");
			System.out.print(iteratorNode.id + ":" + iteratorNode.port);
			System.out.print(" > ");
			System.out.println(key);
			System.out.println("------------------------------------------------------------------");
			String first = n_dash.id;
			String second = iteratorNode.id;
			String third = key;
			//TODO have to write equality condition too
			int left = (first.compareTo(second));
			int right = second.compareTo(third);
			int extreme = first.compareTo(third);
			String zero="0000000000000000000000000000000000000000000000000000000000000000";
			if (extreme > 0)
				if(zero.compareTo(second)<=0  &&  (right < 0))
				{//1
					return ((left > 0) && (right < 0));
				}else//29
				return ((left < 0) && (right > 0));
			else if (extreme < 0)
				return ((left < 0) && (right < 0));
			else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

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
}
