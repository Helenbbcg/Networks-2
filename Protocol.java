import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Protocol {

	/**
	 * check if a file exists
	 * 
	 * @param filepath the path of file
	 * @return true if the file exists, false otherwise
	 */
	public static boolean fileExists(String filepath) {
		File file = new File(filepath);
		return file.exists() && file.isFile();
	}

	/**
	 * get the list of file names in a folder
	 * 
	 * @param folder the file folder to check
	 * @return list of file
	 */
	public static List<String> getFileList(String folder) {
		File file = new File(folder);
		File[] files = file.listFiles();

		List<String> result = new ArrayList<String>();
		for (File f : files) {
			if (f.exists() && f.isFile()) {
				result.add(f.getName());
			}
		}

		return result;
	}

	/**
	 * convert a integer to 4 bytes
	 * 
	 * @param n
	 * @return byte array
	 */
	public static byte[] intToByte4(int n) {
		byte[] targets = new byte[4];
		targets[3] = (byte) (n & 0xFF);
		targets[2] = (byte) (n >> 8 & 0xFF);
		targets[1] = (byte) (n >> 16 & 0xFF);
		targets[0] = (byte) (n >> 24 & 0xFF);
		return targets;
	}

	/**
	 * read the file content to a byte array
	 * 
	 * @param fileName the name of file to read
	 * @return byte array
	 * @throws IOException
	 */
	public static byte[] readFileFromDisk(String fileName) throws IOException {
		File file = new File(fileName);
		byte[] bytesArray = new byte[(int) file.length()];

		InputStream inputStream = new FileInputStream(file);
		inputStream.read(bytesArray);
		inputStream.close();
		return bytesArray;
	}

	/**
	 * read some bytes from in stream
	 * 
	 * @param din    the input stream
	 * @param length the number of bytes to read
	 * @return byte array
	 * @throws IOException
	 */
	public static byte[] readBytes(DataInputStream din, int length) throws IOException {
		byte[] buffer = new byte[length];
		int sum = 0;
		while (sum < length) {
			int read = din.read(buffer, sum, length - sum);
			sum += read;
		}
		return buffer;
	}

	/**
	 * read text message from server
	 * 
	 * @param din the input stream
	 * @return the text message
	 * @throws IOException
	 */
	public static String readMessage(DataInputStream din) throws IOException {
		// read message length
		int length = din.readInt();

		// read message content
		byte[] buffer = readBytes(din, length);
		return new String(buffer);
	}

	/**
	 * read file from server and save it to disk
	 * 
	 * @param fileName the file name to be created
	 * @param din      the input stream
	 * @return the number of bytes read
	 * @throws IOException
	 */
	public static int readFile(String fileName, DataInputStream din) throws IOException {
		// read length of file
		int length = din.readInt();

		// read file content
		if (length > 0) {
			byte[] buffer = readBytes(din, length);

			// save file
			File file = new File(fileName);
			OutputStream outputStream = new FileOutputStream(file);
			outputStream.write(buffer, 0, length);
			outputStream.close();
		}

		return length;
	}

	/**
	 * send protocol message to server. each message contains a text
	 * 
	 * @param dout        the output stream
	 * @param textContent the content of text
	 * @throws IOException
	 */
	public static void send(DataOutputStream dout, String textContent) throws IOException {
		Integer len_text = textContent.getBytes().length;
		
		// send text length and content to server
		dout.write(intToByte4(len_text));
		dout.write(textContent.getBytes());

		dout.flush();
	}
	
	/**
	 * send protocol message to server. each message contains a text and a file(may
	 * be 0 length)
	 * 
	 * @param dout        the output stream
	 * @param textContent the content of text
	 * @param fileContent the content of file
	 * @throws IOException
	 */
	public static void send(DataOutputStream dout, String textContent, byte[] fileContent) throws IOException {
		Integer len_text = textContent.getBytes().length;
		Integer len_file = fileContent.length;

		// send text length and content to server
		dout.write(intToByte4(len_text));
		dout.write(textContent.getBytes());

		// send file length and content to server
		dout.write(intToByte4(len_file));
		dout.write(fileContent);
		dout.flush();
	}
}
