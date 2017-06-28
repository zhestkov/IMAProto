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
	}

}

//UID:reqFetch: FETCH 1:* (FLAGS) 
//The response was sent to client: * 1 FETCH (FLAGS (\Recent) )
//The response was sent to client:  OK FETCH completed
//The response was sent to client: 5 OK UID FETCH completed
//Clent says: 6 logout
