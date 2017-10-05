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
import tutorial.*;
import shared.*;

import java.util.HashMap;

public class JavaServer {

	public static MHandler mcHandler;
	public static FileStore.Processor FSProcessor;

	public static int port;

	public static void main(String[] args) {
		try {

			mcHandler = new MHandler();
			FSProcessor = new FileStore.Processor(mcHandler);

			// port= Integer.valueOf(args[0]);
//			port = 8081;
			port = 8080;
			System.out.println(":::port "+port);

			Runnable simple = new Runnable() {
				public void run() {

					// simple(processor);
					simple1(FSProcessor);
				}
			};
			/*
			 * Runnable secure = new Runnable() { public void run() { secure(processor); }
			 * };
			 */
			new Thread(simple).start();
			// new Thread(secure).start();

		} catch (Exception x) {
			x.printStackTrace();
		}

	}

	public static void simple(FileStore.Processor FSProcessor) {
		try {
			TServerTransport serverTransport = new TServerSocket(port);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(FSProcessor));

			// Use this for a multithreaded server
			// TServer server = new TThreadPoolServer(new
			// TThreadPoolServer.Args(serverTransport).processor(processor));

			System.out.println("Starting the simple server...");
			server.serve();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple1(Processor fSProcessor2) {
		try {
			TServerTransport serverTransport = new TServerSocket(port);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(fSProcessor2));

			// Use this for a multithreaded server
			// TServer server = new TThreadPoolServer(new
			// TThreadPoolServer.Args(serverTransport).processor(processor));

			System.out.println("Starting the simple server...");
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void secure(Calculator.Processor processor) {
	}
}