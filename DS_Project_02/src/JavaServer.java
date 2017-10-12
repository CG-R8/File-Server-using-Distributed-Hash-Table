import org.apache.thrift.server.TServer;

import chord_auto_generated.*;
import chord_auto_generated.FileStore.Processor;

import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

// Generated code

import java.util.HashMap;

public class JavaServer {

	public static MHandler mcHandler;
	public static FileStore.Processor FSProcessor;

	public static int port;

	public static void main(String[] args) {
		try {

			mcHandler = new MHandler(args[0]);
			FSProcessor = new FileStore.Processor(mcHandler);
			port = Integer.parseInt(args[0]);
			Runnable simple = new Runnable() {
				public void run() {
					// simple(processor);
					simple1(FSProcessor);
				}
			};
			new Thread(simple).start();
		} catch (Exception x) {
			x.printStackTrace();
		}

	}


	public static void simple1(Processor fSProcessor2) {
		try {
			TServerTransport serverTransport = new TServerSocket(port);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(fSProcessor2));
			System.out.println("Starting the server on port :"+port);
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void secure(FileStore.Processor processor) {
	}
}
