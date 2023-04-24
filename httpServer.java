import java.io.*;
import java.net.*;
import java.io.File;

public class httpServer {    

    public static void handleRequest(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            //define output stream to write back to GET request
            OutputStream out = socket.getOutputStream();

            String requestType = "";
            
            int ch = 0;
            while((ch = in.read()) != '\n') {
                requestType = requestType + ((char) ch);
            }

            String[] getFile = requestType.split(" ");
            File findingFile = new File(getFile[1].substring(1));

            if(requestType.contains("GET")) {
                if(findingFile.isFile()) {
                    //return non-error code

                    String contentType = "";
                    if(getFile[1].contains("html")) {
                        contentType = "text/html";
                    }
                    else if(getFile[1].contains("txt")) {
                        contentType = "text/plain";
                    }

                    int contentLength = (int) findingFile.length();

                    StringBuilder fileContent = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(getFile[1].substring(1)));
                    String emptyString = "";
                    while ((emptyString = reader.readLine()) != null) {
                        fileContent.append(emptyString);
                    }
                    reader.close();

                    String returnStatement = "HTTP/1.1 200 OK" + "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + fileContent.toString(); 

                    out.write(returnStatement.getBytes());
                }
                else {
                    //return error code
                    String errorCode = "HTTP/1.1 404 Not Found" + "\r\n\r\n" + "Error 404 File Not Found";
                    out.write(errorCode.getBytes());
                }
                
            } 
            else if (requestType.contains("POST")) {
                if(findingFile.isFile() && getFile[1].contains("txt")) {
                    //append text sent in body to the file
                    StringBuilder fileContent = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(getFile[1].substring(1)));
                    String emptyString = "";
                    while ((emptyString = reader.readLine()) != null) {
                        fileContent.append(emptyString);
                    }
                    reader.close();

                    //Find the header information
                    String requestString = "";
                    int character = 0;
                    while(((character = in.read()) != -1) && (requestString.indexOf("\r\n\r\n") == -1)) {
                        requestString = requestString + ((char) character);
                    }
                    String[] postRequest = requestString.split("\n");

                    //Find the content length
                    int contentLength = 0;
                    String contentType = "";
                    for(int i = 0; i < postRequest.length; i++) {
                        if(postRequest[i].contains("Length")) {
                            String[] contentLengthString = postRequest[i].split(" ");
                            contentLength = Integer.parseInt(contentLengthString[1].trim());
                        } 
                        else if(postRequest[i].contains("Type")) {
                            String[] contentTypeString = postRequest[i].split(" ");
                            contentType = contentTypeString[1].trim();
                        }
                    }

                    //find the content to add to file
                    String message = "" + (char) character;
                    int charc = 0;
                    while(message.length() != contentLength) {
                        charc = in.read();
                        message = message + ((char) charc);
                    }

                    FileWriter writer = new FileWriter(getFile[1].substring(1));
                    writer.write(fileContent.toString() + " " + message);
                    writer.close();

                    String returnStatement = "HTTP/1.1 200 OK" + "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + fileContent.toString() + " " + message; 
                    out.write(returnStatement.getBytes());
                }
                else {
                    String errorCode = "HTTP/1.1 405 Method Not Allowed" + "\r\n\r\n" + "Error 405 Method Not Allowed: Either you are using a non txt file or the file you are requesting to post to does not exist.";
                    out.write(errorCode.getBytes());
                }
            } 
            else if (requestType.contains("PUT")) {
                if(!findingFile.isFile()) {
                    //Create new file if file doesn't exist
                    findingFile.createNewFile();
                }
                //replace content with content sent in body to the file or create new file

                //Find the header information
                String requestString = "";
                int character = 0;
                while(((character = in.read()) != -1) && (requestString.indexOf("\r\n\r\n") == -1)) {
                    requestString = requestString + ((char) character);
                }
                String[] postRequest = requestString.split("\n");

                //Find the content length
                int contentLength = 0;
                String contentType = "";
                for(int i = 0; i < postRequest.length; i++) {
                    if(postRequest[i].contains("Length")) {
                        String[] contentLengthString = postRequest[i].split(" ");
                        contentLength = Integer.parseInt(contentLengthString[1].trim());
                    } 
                    else if(postRequest[i].contains("Type")) {
                        String[] contentTypeString = postRequest[i].split(" ");
                        contentType = contentTypeString[1].trim();
                    }
                }

                //find the content to add to file
                String message = "" + (char) character;
                int charc = 0;
                while(message.length() != contentLength) {
                    charc = in.read();
                    message = message + ((char) charc);
                }

                FileWriter writer = new FileWriter(getFile[1].substring(1));
                writer.write(message);
                writer.close();

                String returnStatement = "HTTP/1.1 200 OK" + "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + message; 

                out.write(returnStatement.getBytes());
            } 
            else if (requestType.contains("DELETE")) {
                findingFile.delete();

                String returnStatement = "HTTP/1.1 200 OK" + "\r\n\r\n"; 
                out.write(returnStatement.getBytes());
            }
            //System.out.println("this is closing");
            in.close();
            out.close();
            socket.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) throws Exception {
        ServerSocket server = new ServerSocket(80);

        Socket socket = null;

        while((socket = server.accept()) != null) {
            final Socket threadSocket = socket;
            new Thread( () -> handleRequest(threadSocket)).start();
        }

        //close the TCP Server
        System.out.println("closed");
        server.close();
    }
}