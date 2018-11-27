import java.net.*;
import java.io.*;
import java.util.*;
import java.util.*;

public class ProxyCache2 {
    /** Port for the proxy */
    private static int port;
    /** Socket for client connections */
    private static ServerSocket socket;
    /** Socket for client connections */
    private static Map<String, String> cache = new Hashtable<String, String>();
    /** Hashtable for cacheing */

    public static synchronized void caching(HttpRequest req, HttpResponse resp) throws IOException{
    	File fi;
    	DataOutputStream outFi;
    	fi = new File("cache/","cached_"+System.currentTimeMillis());
    	outFi = new DataOutputStream(new FileOutputStream(fi));
    	outFi.writeBytes(resp.toString()); 
    	outFi.write(resp.body); 
    	outFi.close();
    	cache.put(req.URI, fi.getAbsolutePath());
    	System.out.println("Caching from: "+req.URI+" para "+fi.getAbsolutePath());
    }

  	public static synchronized byte[] uncaching(String str) throws IOException{
  		File fi;
  		FileInputStream fiStream;
  		String hashfile;
  		byte[] bytesCached;
  		if((hashfile = cache.get(str))!=null){
  			fi = new File(hashfile);
  			fiStream = new FileInputStream(fi);
  			bytesCached = new byte[(int)fi.length()];
  			fiStream.read(bytesCached);
  			System.out.println("Requested object in cache, returning to the client");
  			return bytesCached;
  		}
  		else {
  			System.out.println("Requested object not cached");
  			return bytesCached = new byte[0];
  		}
    }

    /** Create the ProxyCache object and the socket */
    public static void init(int p) {
	    port = p;
	    try {
	        socket = new ServerSocket(port);/* Fill in */
	    } catch (IOException e) {
	        System.out.println("Error creating socket: " + e);
	        System.exit(-1);
	    }
    }

    public static void handle(Socket client) {
	    Socket server = null;
	    HttpRequest request = null;
	    HttpResponse response = null;

	    /* Process request. If there are any exceptions, then simply
	     * return and end this request. This unfortunately means the
	     * client will hang for a while, until it timeouts. */

	    /* Read request */
	    try {
	        BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));/* Fill in */
	        request = new HttpRequest(fromClient);/* Fill in */
	    } catch (IOException e) {
	        System.out.println("Error reading request from client: " + e);
	        return;
	    }

	    /* Send request to server */
	    try {
	        /* Open socket and write request to socket */
	        server = new Socket(request.getHost(), request.getPort());/* Fill in */
	        DataOutputStream toServer = new DataOutputStream(server.getOutputStream());/* Fill in */
	        toServer.writeBytes(request.toString());/* Fill in */
	    } catch (UnknownHostException e) {
	        System.out.println("Unknown host: " + request.getHost());
	        System.out.println(e);
	        return;
	    } catch (IOException e) {
	        System.out.println("Error writing request to server: " + e);
	        return;
	    }

	    /* Read response and forward it to client */
        try {
		    byte[] cache = ProxyCache2.uncaching(request.URI);
		    if (cache.length==0) {
		        DataInputStream fromServer = new DataInputStream(server.getInputStream()); /* Criar inputstream do servidor */
		        response = new HttpResponse(fromServer); /* Criar objecto com a response do servidor */
		        DataOutputStream toClient = new DataOutputStream(client.getOutputStream());

		        toClient.writeBytes(response.toString()); /* Escreve headers */
		        toClient.write(response.body); /* Escreve body */
		        /* Write response to client. First headers, then body */

		        ProxyCache2.caching(request, response); /* Guardar em cache */

		        client.close();
		        server.close();
		        /* Insert object into the cache */
		        /* Fill in (optional exercise only) */
		    }
		    else {
			    DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
			    toClient.write(cache);
			    client.close();
			    server.close();
		    }

	    } catch (IOException e) {
	        //System.out.println("Error writing response to client: " + e);
        }
    }


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	    int myPort = 0;
	    File cachedir = new File("cache/");
	    if (!cachedir.exists()){cachedir.mkdir();}
	
	    try {
	        myPort = Integer.parseInt(args[0]);
	    } catch (ArrayIndexOutOfBoundsException e) {
	        System.out.println("Need port number as argument");
	        System.exit(-1);
	    } catch (NumberFormatException e) {
	        System.out.println("Please give port number as integer.");
	        System.exit(-1);
	    }
	
	    init(myPort);

	    /** Main loop. Listen for incoming connections and spawn a new
	     * thread for handling them */
	    Socket client = null;
	
	    while (true) {
	        try {
		        client = socket.accept();/* Fill in */
		        handle(client);
	        } catch (IOException e) {
		    System.out.println("Error reading request from client: " + e);
		    /* Definitely cannot continue processing this request,
		     * so skip to next iteration of while loop. */
		    continue;
	        }
	    }

    }
}
