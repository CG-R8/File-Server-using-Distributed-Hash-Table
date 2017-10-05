import java.util.List;

import org.apache.thrift.TException;

import chord_auto_generated.FileStore.Iface;
import chord_auto_generated.NodeID;
import chord_auto_generated.RFile;
import chord_auto_generated.SystemException;

public class MHandler implements Iface {

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
		System.out.println(node_list.toString());
		
	}

	@Override
	public NodeID findSucc(String key) throws SystemException, TException {
		// TODO Auto-generated method stub
		
		
		return null;
	}

	@Override
	public NodeID findPred(String key) throws SystemException, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeID getNodeSucc() throws SystemException, TException {
		// TODO Auto-generated method stub
		return null;
	}

}
