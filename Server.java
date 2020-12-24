import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	static final String dir = "serverFiles"; // file directory

	/**
	 * start server and keep waiting the connections from client
	 */
	public void runServer() {
		try {
			// we use port 8888 here.
			ServerSocket serverSock = new ServerSocket(8888);

			// create thread pool
			ExecutorService es = Executors.newFixedThreadPool(10);

			while (true) {
				// Accept; blocking; will not return until a client has made contact.
				Socket sock = serverSock.accept();

				// run a thread for this client
				Runnable task = new Handler(sock);
				es.execute(task);
			}
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	/**
	 * append one line of log to log.txt
	 * 
	 * @param log the line of log text
	 */
	public synchronized static void saveLog(String log) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter("log.txt", true));
			System.out.println(log);
			writer.println(log);
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	/**
	 * thread for talking with client
	 */
	public static class Handler implements Runnable {
		private Socket socket; // the socket for communicating with client
		private InetAddress inet; // the ip address of client
		private DataInputStream din; // the data input stream
		private DataOutputStream dout; // the output stream

		/**
		 * creates a new handler
		 * 
		 * @param socket the socket of client
		 * @throws IOException
		 */
		public Handler(Socket socket) throws IOException {
			this.socket = socket;
			inet = socket.getInetAddress();
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
		}

		/**
		 * process the request from client
		 */
		public void run() {
			try {
				// read request
				String request = Protocol.readMessage(din);

				// generates log text
				Date date = new Date();
				String log = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(date) + ":" + inet.getHostAddress() + ":"
						+ request;

				// append log
				saveLog(log);

				// parse request to tokens
				String[] tokens = request.split(" ");

				// the first token is command(should be list, get, or put)
				String cmd = tokens[0];

				if (cmd.equalsIgnoreCase("list")) {
					// handle list command

					// read file list
					List<String> fileList = Protocol.getFileList(dir);

					// compose response string
					String response = "";
					for (String f : fileList) {
						response += f + System.lineSeparator();
					}

					// send response
					Protocol.send(dout, response);
				} else if (cmd.equalsIgnoreCase("get")) {
					// handle get command

					// the second token of request is filename
					String filename = tokens[1];
					String filepath = dir + File.separator + filename;
					if (Protocol.fileExists(filepath)) {
						// read file
						byte[] fileContent = Protocol.readFileFromDisk(filepath);

						// send file
						Protocol.send(dout, "ok", fileContent);
					} else {
						// send error string
						Protocol.send(dout, "file not found.");
					}
				} else if (cmd.equalsIgnoreCase("put")) {
					// handle put command

					// the second token of request is filename
					String filename = tokens[1];

					// read and save file from socket
					String filepath = dir + File.separator + filename;
					Protocol.readFile(filepath, din);

					// send response
					Protocol.send(dout, "ok");
				} else {
					// Unknown command
					Protocol.send(dout, "unkown command.");
				}

			} catch (Exception ex) {

			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.runServer();
	}
}