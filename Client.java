import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	static final String dir = "clientFiles"; // file directory

	private Socket socket; // socket for communicating with server
	private DataInputStream din; // data input stream for reading server data
	private DataOutputStream dout; // data output stream for writing data to server

	/**
	 * Constructs a new Client. Initialize a socket connection to "localhost:8888"
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 8888);
		din = new DataInputStream(socket.getInputStream());
		dout = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * upload a file to server
	 * 
	 * @param filename the file to upload
	 * @throws IOException
	 */
	public void put(String filename) throws IOException {
		if (Protocol.fileExists(dir + File.separator + filename)) {
			// read file content
			byte[] fileContent = Protocol.readFileFromDisk(dir + File.separator + filename);

			// send request
			Protocol.send(dout, "put " + filename, fileContent);

			// read response
			String response = Protocol.readMessage(din);
			if (response.equals("ok")) {
				// upload successfully
				System.out.println(fileContent.length + " bytes transfered.");
			} else {
				// show error from server
				System.out.println(response);
			}
		} else {
			// cancel, the file to be uploaded not found
			System.out.println("file not found.");
		}
	}

	/**
	 * download file from server
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void get(String filename) throws IOException {
		// send request
		Protocol.send(dout, "get " + filename);

		String response = Protocol.readMessage(din);
		if (response.equals("ok")) {
			// download successfully
			int length = Protocol.readFile(dir + File.separator + filename, din);
			System.out.println(length + " bytes transfered.");
		} else {
			// show error from server
			System.out.println(response);
		}
	}

	/**
	 * list out the files at server
	 * 
	 * @throws IOException
	 */
	public void list() throws IOException {
		// send request
		Protocol.send(dout, "list");

		// read response
		String response = Protocol.readMessage(din);

		if (response.isEmpty()) {
			// response is empty which means no file
			System.out.println("no files in server.");
		} else {
			// show file list
			System.out.println(response);
		}
	}

	/**
	 * close socket and disconnect with server
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		din.close();
		dout.close();
		socket.close();
	}

	public static void main(String[] args) {

		try {
			// create client and connect to server
			Client client = new Client();

			// parse arguments
			if (args.length == 1) {
				// if their is just one command, that should be list
				String cmd = args[0];
				if (cmd.equalsIgnoreCase("list")) {
					client.list();
				} else {
					// unknown command
					System.out.println("unknown command " + cmd);
				}
			} else if (args.length == 2) {
				// if there are two command, that should be get or put
				String cmd = args[0];
				String filename = args[1];
				
				if (cmd.equalsIgnoreCase("get")) {
					// do get
					client.get(filename);
				} else if (cmd.equalsIgnoreCase("put")) {
					// do put
					client.put(filename);
				} else {
					// unknown command
					System.out.println("unknown command " + cmd);
				}
			} else {
				// invalid arguments
				System.out.println("Invalid arguments.");
			}

			// close client
			client.close();
		} catch (IOException e) {
			System.out.println("connect server error: " + e.getMessage());
		}
	}
}