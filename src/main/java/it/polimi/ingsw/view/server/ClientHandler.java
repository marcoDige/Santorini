package it.polimi.ingsw.view.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{

    //attributes

    private Socket client;

    //constructors

    public ClientHandler(Socket socket) {
        this.client = socket;
    }

    //methods

    @Override
    public void run() {

        File file = new File("src/main/resources/arrivedRequest.xml");

        try {
            InputStream in = client.getInputStream();
            FileOutputStream out = new FileOutputStream(file, false);

            //byte[] buffer = new byte[(int) file.length()];
            byte[] buffer = new byte[16000];
            int r = in.read(buffer);
            out.write(buffer,0, r);

            out.flush();
            out.close();
            in.close();
            client.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
