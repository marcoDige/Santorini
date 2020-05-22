package it.polimi.ingsw.msgUtilities.server;

import it.polimi.ingsw.model.enums.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;

public class UpdateMsgWriter {
    
    //attributes

    private Document document;

    //constructor

    public UpdateMsgWriter(){
        try {
            this.document = this.getDocument(this.getClass().getResourceAsStream("/xml/server/updateMsg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //methods

    //support methods

    public Document setStandardUpdateValues(String user, String mod){
        Node mode = document.getElementsByTagName("Mode").item(0);
        Node username = document.getElementsByTagName("Author").item(0);

        mode.setTextContent(mod);
        username.setTextContent(user);

        return document;
    }

    private Node appendTagWithAttribute(Node father, String tagName, String textContent, String attributeName, String attributeValue){
        Element tag = document.createElement(tagName);
        tag.setAttribute(attributeName,attributeValue);
        tag.setTextContent(textContent);
        father.appendChild(tag);
        return document.getElementsByTagName(tagName).item(0);
    }

    private Node appendTag(Node father, String tagName, String textContent){
        Element tag = document.createElement(tagName);
        tag.setTextContent(textContent);
        father.appendChild(tag);
        return document.getElementsByTagName(tagName).item(0);
    }

    private Node appendTag(Node father, String tagName){
        Element tag = document.createElement(tagName);
        father.appendChild(tag);
        return document.getElementsByTagName(tagName).item(0);
    }

    private Node appendTag(int index, Node father,String tagName){
        Element tag = document.createElement(tagName);
        father.appendChild(tag);
        return document.getElementsByTagName(tagName).item(index);
    }

    private Node initializeTagList(String tagName){
        return document.getElementsByTagName(tagName).item(0);
    }

    /**
     * This method creates the document object and parses file's path
     * @return the parsed xml file
     * @throws Exception error during xml parsing
     */

    private Document getDocument(InputStream stream) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(stream);
    }

    //action methods

    public Document loginUpdate(String user, Color c){
        setStandardUpdateValues(user,"newPlayer");
        Node updateTag = initializeTagList("Update");

        appendTag(updateTag,"Username",user);
        appendTag(updateTag,"Color",Color.labelOfEnum(c));
        return document;
    }

    public Document startGameUpdate(String user){
        setStandardUpdateValues(user,"startGame");

        initializeTagList("Update");
        return document;
    }

    public Document createGodsUpdate(String user, ArrayList<Integer> ids){
        setStandardUpdateValues(user,"createGods");
        Node updateTag = initializeTagList("Update");
        Node godsTag = appendTag(updateTag,"Gods");

        for(int i = 0; i < ids.size(); i++){
            appendTagWithAttribute(godsTag,"God",String.valueOf(ids.get(i)),"n",String.valueOf(i));
        }
        return document;
    }

    public Document choseGodUpdate(String user, int godId){
        setStandardUpdateValues(user,"choseGod");
        Node updateTag = initializeTagList("Update");

        appendTag(updateTag,"godId",String.valueOf(godId));
        return document;

    }

    public Document choseStartingPlayerUpdate(String user, String playerChosen){
        setStandardUpdateValues(user,"choseStartingPlayer");
        Node updateTag = initializeTagList("Update");

        appendTag(updateTag,"StartingPlayer",playerChosen);
        return document;
    }

    public Document setupOnBoardUpdate(String user, String workerGender, int x, int y){
        setStandardUpdateValues(user,"setWorkerOnBoard");
        Node updateTag = initializeTagList("Update");

        appendTag(updateTag,"WorkerGender",workerGender);
        appendTag(updateTag,"xPosition",String.valueOf(x));
        appendTag(updateTag,"yPosition",String.valueOf(y));
        return document;
    }

    public Document moveUpdate(int index, int startX, int startY, int x, int y){
        Node updateTag = initializeTagList("Update");

        Node position = appendTag(index, updateTag,"Position");
        appendTag(position,"startXPosition",String.valueOf(startX));
        appendTag(position,"startYPosition",String.valueOf(startY));
        appendTag(position,"xPosition",String.valueOf(x));
        appendTag(position,"yPosition",String.valueOf(y));
        return document;
    }

    public Document buildUpdate(int index, int startX, int startY, int x, int y, int level){
        Node updateTag = initializeTagList("Update");

        Node height = appendTag(index, updateTag,"Height");
        appendTag(height,"startXPosition",String.valueOf(startX));
        appendTag(height,"startYPosition",String.valueOf(startY));
        appendTag(height,"xPosition",String.valueOf(x));
        appendTag(height,"yPosition",String.valueOf(y));
        appendTag(height,"Level",String.valueOf(level));
        return document;
    }

    public Document endOfTurnRemoveAndBuildUpdate(int startX, int startY,int level){
        Node updateTag = initializeTagList("Update");

        Node removeAndBuild = appendTag(updateTag,"RemoveAndBuild");
        appendTag(removeAndBuild,"startXPosition",String.valueOf(startX));
        appendTag(removeAndBuild,"startYPosition",String.valueOf(startY));
        appendTag(removeAndBuild,"Level",String.valueOf(level));
        return document;
    }

    public Document extraUpdate(String mode){
        setStandardUpdateValues("null",mode);
        return document;
    }

    public Document winLoseUpdate(String winner,String mode){
        setStandardUpdateValues(winner,mode);
        return document;
    }

    public Document loseUpdate(String loser, String mode){
        setStandardUpdateValues(loser,mode);
        return document;
    }
}
