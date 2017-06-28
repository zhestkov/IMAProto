package architecture;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		int port = 2222;
		//Server server = new Server(port);
		IncomingServer server = new IncomingServer(port);
		server.start();
//		String eml = "1";
//		File file = new File("C:\\Net\\Messages\\" + eml + ".eml");
//		long fileLength = file.length();
//		System.out.println(fileLength);
//		
//		String response = "";
//		String line = null;
//		try {
//			Scanner sc = new Scanner(file);
//			response += "BODY[] {" + fileLength + "}\r\n";
//			while (sc.hasNextLine()) {
//				line = sc.nextLine() + "\r\n";
//				response += line;
//			}
//			
//		} catch (FileNotFoundException e1) {
//			System.out.println("Message file doesn't exist.");
//			e1.printStackTrace();
//		}
//		System.out.println(response);
	}

}

//UID:reqFetch: FETCH 1:* (FLAGS) 
//The response was sent to client: * 1 FETCH (FLAGS (\Recent) )
//The response was sent to client:  OK FETCH completed
//The response was sent to client: 5 OK UID FETCH completed
//Clent says: 6 logout
