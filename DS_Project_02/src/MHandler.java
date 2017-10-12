import java.util.HashMap;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import chord_auto_generated.FileStore.Iface;
import chord_auto_generated.NodeID;
import chord_auto_generated.RFile;
import chord_auto_generated.RFileMetadata;
import chord_auto_generated.SystemException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;

public class MHandler implements Iface {
	public int currentPort;
	private List<NodeID> node_ID_list;
	public static HashMap<String, RFile> FilesInfo;

	public MHandler(String string) {
		currentPort = Integer.parseInt(string);
		FilesInfo = new HashMap<String, RFile>();
	}

	@Override
	public void writeFile(RFile rFile) throws SystemException, TException {
		// System.out.println("In the writting File function");
		RFileMetadata clientMeta = rFile.getMeta();
		String clientContent = rFile.getContent();
		String clientMetaFilename = clientMeta.getFilename();
		String clientMetaOwner = clientMeta.getOwner();
		String clientHash = clientMeta.getContentHash();
		String ownerFilenameHash = sha_256(clientMetaOwner + ":" + clientMetaFilename);
		try {
			if (FilesInfo.containsKey(ownerFilenameHash)) {
				RFile localRfile = new RFile();
				localRfile.setContent(clientContent);
				RFileMetadata localMeta = new RFileMetadata();
				localMeta.setContentHash(sha_256(clientContent));
				localMeta.setVersion(FilesInfo.get(ownerFilenameHash).getMeta().version + 1);
				localMeta.setOwner(clientMetaOwner);
				localMeta.setFilename(clientMetaFilename);
				localRfile.setMeta(localMeta);
				FilesInfo.put(ownerFilenameHash, localRfile);
				printRfile(localRfile);
			} else {
				// https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
				RFile localRfile = new RFile();
				localRfile.setContent(clientContent);
				RFileMetadata localMeta = new RFileMetadata();
				localMeta.setContentHash(sha_256(clientContent));
				localMeta.setFilename(clientMetaFilename);
				localMeta.setOwner(clientMetaOwner);
				localMeta.setVersion(0);
				localRfile.setMeta(localMeta);
				printRfile(localRfile);
				FilesInfo.put(ownerFilenameHash, localRfile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printRfile(RFile localRfile) {
		System.out.println("**********************************************************");
		System.out.println(localRfile.getContent());
		System.out.println(localRfile.getMeta());
		System.out.println("**********************************************************");
	}

	@Override
	public RFile readFile(String filename, String owner) throws SystemException, TException {
		String ownerFilenameHash = sha_256(owner + ":" + filename);
		SystemException exception = null;
		RFile localRfile = new RFile();
		if (FilesInfo.containsKey(ownerFilenameHash)) {
			localRfile = FilesInfo.get(ownerFilenameHash);
		} else {
			exception = new SystemException();
			exception.setMessage("File do not exist on server");
			throw exception;
		}
		return localRfile;
	}

	@Override
	public void setFingertable(List<NodeID> node_list) throws TException {
		if (node_list.isEmpty()) {
			SystemException exception = new SystemException();
			exception.setMessage("Finger table is Empty");
			throw exception;
		}
		System.out.println("Finger table for this server is set.");
		System.out.println(node_list);
		node_ID_list = node_list;
	}

	@Override
	public NodeID findSucc(String key) throws SystemException, TException {
		if (node_ID_list.isEmpty()) {
			try {
				SystemException exception = new SystemException();
				exception.setMessage("Finger table is Empty Please restart server");
				System.exit(0);
				throw exception;
			} catch (SystemException e) {
				e.printStackTrace();
			}
		}
		NodeID n_dash = new NodeID();
		try {
			n_dash = findPred(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String predIP = n_dash.ip;
		int predPort = n_dash.port;
		NodeID succNode = new NodeID();
		try {
			if (getCurrentNode().equals(n_dash)) {
				succNode = getNodeSucc();
			} else {
				try {
					TSocket transport = new TSocket(predIP, predPort);
					transport.open();
					TBinaryProtocol protocol = new TBinaryProtocol(transport);
					chord_auto_generated.FileStore.Client client = new chord_auto_generated.FileStore.Client(protocol);
					try {
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
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return succNode;
	}

	@Override
	public NodeID findPred(String key) throws SystemException, TException {
		NodeID n_dash = new NodeID();
		try {
			n_dash = getCurrentNode();
			while ((!compareIDs_FirstNode(n_dash, key))) {
				n_dash = closest_preceding_fing(n_dash, key);
				System.out.println("One of the HOP is here : " + n_dash.getPort());
				n_dash = rpcToNextHop(n_dash, key);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("RETURN FOUND: " + n_dash.id + " Port:" + n_dash.port);
		return n_dash;
	}

	private NodeID getCurrentNode() throws UnknownHostException {
		NodeID currentNode = new NodeID();
		currentNode.port = currentPort;
		// currentNode.ip=InetAddress.getLocalHost().getHostAddress();
		// TODO change workaround for Ip address
		currentNode.ip = "127.0.0.1";
		String preHashingString = currentNode.ip + ":" + currentNode.port;
		currentNode.id = sha_256(preHashingString);
		return currentNode;
	}

	public NodeID closest_preceding_fing(NodeID n_dash, String key) {
		int m = 255, i = 0;
		NodeID iteratorNode = new NodeID();
		try {
			for (i = m; i >= 1; i--) {
				iteratorNode = node_ID_list.get(i);
				if (compareIDs(iteratorNode, key, n_dash)) {
					return iteratorNode;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n_dash;
	}

	@Override
	public NodeID getNodeSucc() throws SystemException, TException {
		if (node_ID_list.size() == 0) {
			try {
				SystemException exception = new SystemException();
				exception.setMessage("Stuck in Loop: calling same port again and again");
				// System.exit(0);
				throw exception;
			} catch (SystemException e) {
				e.printStackTrace();
			}
		}
		return node_ID_list.get(0);
	}

	private NodeID rpcToNextHop(NodeID nextHop, String key) throws SystemException, TException {
		NodeID predNode = new NodeID();
		SystemException exception = new SystemException();
		if (nextHop.getPort() == currentPort) {
			try {
				exception.setMessage("Stuck in Loop: calling same port again and again");
				// System.exit(0);
				throw exception;
			} catch (SystemException e) {
				e.printStackTrace();
			}
		}
		try {
			TSocket transport = new TSocket(nextHop.getIp(), nextHop.getPort());
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			chord_auto_generated.FileStore.Client client = new chord_auto_generated.FileStore.Client(protocol);
			try {
				// System.out.println("Connecting client findPred");
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

	private boolean compareIDs_FirstNode(NodeID n_dash, String key) throws SystemException {
		try {
			NodeID n_dash_successor = new NodeID();
			try {
				n_dash_successor = node_ID_list.get(0);
			} catch (Exception e) {
				SystemException exception = new SystemException();
				exception.setMessage("Please restart the initialisation of finger tables process and server");
				throw exception;
			}
			String first = n_dash.id;
			String second = key;
			String third = n_dash_successor.id;
			int left = (first.compareTo(second));
			int right = second.compareTo(third);
			int extreme = first.compareTo(third);
			String zero = "0000000000000000000000000000000000000000000000000000000000000000";
			if (extreme > 0)
				if (zero.compareTo(second) <= 0 && (right < 0)) {// 1
					return ((left > 0) && (right < 0));
				} else// 29
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

	private boolean compareIDs(NodeID iteratorNode, String key, NodeID n_dash) {
		try {
			String first = n_dash.id;
			String second = iteratorNode.id;
			String third = key;
			// TODO have to write equality condition too
			int left = (first.compareTo(second));
			int right = second.compareTo(third);
			int extreme = first.compareTo(third);
			String zero = "0000000000000000000000000000000000000000000000000000000000000000";
			if (extreme > 0)
				if (zero.compareTo(second) <= 0 && (right < 0)) {// 1
					return ((left > 0) && (right < 0));
				} else// 29
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

	// Ref:
	// https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
	public String sha_256(String currentID) {
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
