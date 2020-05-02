package it.polimi.ingsw.view.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MsgSender {
    private Socket socket;

    public MsgSender(Socket s){
        this.socket = s;
    }

    public void sendMsg() {
        File file = new File("src/main/resources/client/toSendRequest");

        try {

            OutputStream out = socket.getOutputStream();
            FileInputStream fileIn = new FileInputStream(file);

            byte[] buffer = new byte[2000];
            int r = fileIn.read(buffer);
            out.write(buffer, 0, r);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
