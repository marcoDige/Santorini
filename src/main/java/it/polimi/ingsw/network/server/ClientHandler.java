package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.XMLInputStream;
import it.polimi.ingsw.view.server.VirtualView;
import it.polimi.ingsw.msgUtilities.server.RequestParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * This class manages a single client TCP socket connection.
 * @author marcoDige
 */

public class ClientHandler implements Runnable{

    //attributes

    private final Socket client;
    private final VirtualView virtualView;

    private Document request;

    //constructors

    public ClientHandler(Socket socket, VirtualView vrtV) {
        this.client = socket;
        this.virtualView = vrtV;
        this.request = null;
    }

    //methods

    /**
     * This method allows to receive an XML from connection with client, to start the Request process and send Answer to client
     * The communication go down when client send a "end" mode file.
     */

    @Override
    public void run() {

        System.out.println("Client " + client + " has connected!");

        try {
            InputStream in = client.getInputStream();

            while(true) {

                receiveXML(in);

                if(isEndMode()){
                    break;
                }else{
                    if(!isLoginRequest())
                        processRequest();
                }
            }

            in.close();
            System.out.println("Connection with " + client + " closed!");
            client.close();
        } catch(IOException  e) {
            System.err.println("Client " + client + " has disconnected!");
            try {
                client.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            virtualView.clientDown();
        }
    }

    private void receiveXML(InputStream in){
        DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        XMLInputStream xmlIn = new XMLInputStream(in);


        try {
            docBuilder = docBuilderFact.newDocumentBuilder();
            xmlIn.receive();
            request = docBuilder.parse(xmlIn);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method verify if the request mode is "end".
     * @return true -> "end" request mode
     *         false -> not "end" request mode
     */

    private boolean isEndMode(){
        return new RequestParser(request).parseEndRequest(virtualView);
    }

    /**
     * This method start a client request processing in server. It uses a RequestParser to start this process.
     */

    private void processRequest(){
        new RequestParser(request).parseRequest(virtualView);
    }

    /**
     * This method verify if the request mode is "login".
     * @return true -> "login" request mode
     *         false -> not "login" request mode
     */

    private boolean isLoginRequest() {return new RequestParser(request).parseLoginRequest(virtualView,client);}
}
