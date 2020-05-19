package it.polimi.ingsw.network.client;

import it.polimi.ingsw.msgUtilities.client.MsgInParser;
import it.polimi.ingsw.network.MsgSender;
import it.polimi.ingsw.network.XMLInputStream;
import it.polimi.ingsw.view.client.View;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * This class manages a TCP socket connection (client side).
 * @author marcoDige
 */

public class EchoClient {

    //attributes

    private final String hostName;
    private final int port;
    private Socket server;
    private final View view;

    private Document msgIn;

    //constructor

    public EchoClient(String hostname, int port, View view){
        this.hostName = hostname;
        this.port = port;
        this.view = view;
        this.msgIn = null;
    }

    //methods

    /**
     * This method allows to receive an XML from connection with server and to start the message process.
     * The communication go down when server send a "disconnection" message.
     */

    public void start(){

        initializeClientConnection();

        try {
            InputStream in = server.getInputStream();

            while(true) {

                receiveXML(in);

                if(isDisconnectionMessage()){
                    break;
                }else{
                    processMsg();
                }
            }

            in.close();
            server.close();
        }catch (IOException | SAXException | ParserConfigurationException e){
            if (!server.isClosed()) serverDisconnection();
        }
    }

    /**
     * This method initializes a Socket connection on hostName-port.
     */

    public void initializeClientConnection(){
        try{
            server = new Socket(hostName, port);
        }catch (IOException e){
            view.serverNotFound();
        }
    }

    /**
     * This method allows to extract the incoming XML on the network, it takes advantage of the XMLInputStream class
     * and its methods.
     * @param in Input stream from connection
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */

    private void receiveXML(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        XMLInputStream xmlIn = new XMLInputStream(in);


        docBuilder = docBuilderFact.newDocumentBuilder();
        xmlIn.receive();
        msgIn = docBuilder.parse(xmlIn);
    }

    /**
     * This method verify if the message mode is "disconnection".
     * @return true -> "disconnection" message mode
     *         false -> not "disconnection" message mode
     */

    private boolean isDisconnectionMessage(){
        return new MsgInParser(msgIn,view).parseDisconnectionMessage();
    }

    /**
     * This method start a server message processing in client. It uses a MsgInParser to start this process.
     */

    private synchronized void processMsg(){
        new MsgInParser(msgIn,view).parseIncomingMessage();
    }

    //TODO : javadoc

    public void sendMsg(Document msg){
        new MsgSender(server,msg).sendMsg();
    }

    //TODO : javadoc

    public void serverDisconnection(){
        try {
            server.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        view.showServerDisconnection();
    }

    //TODO : javadoc

    public void anotherClientDisconnection(){
        try {
            server.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        view.showAnotherClientDisconnection();
    }

    //TODO : javadoc

    public void disconnectionForTimeout(){
        try {
            server.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        view.disconnectionForInputExpiredTimeout();
    }
}
