package it.polimi.ingsw.msgUtilities.client;

import it.polimi.ingsw.model.enums.Color;
import it.polimi.ingsw.view.client.View;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.*;
import java.util.*;

/** This class is responsible for parsing the msgIn XML (client-side) and call View methods to start
 * the request processing.
 * @author pierobartolo
 */

public class MsgInParser {

    //attributes

    private Document document;
    private View view;

    public MsgInParser(Document document, View view){
        this.document = document;
        this.view = view;
    }


    //method

    /**
     * This method reads the server's answer and notifies the view with the data
     */

    public boolean parseDisconnectionMessage(){
        String answerType = document.getFirstChild().getNodeName();
        if(answerType.equals("UpdateMsg")){
            String mode = Objects.requireNonNull(evaluateXPath("/UpdateMsg/Mode/text()")).get(0);
            if(mode.equals("disconnection")){
                view.anotherClientDisconnection();
                return true;
            }
            if(mode.equals("disconnectionForLobbyNoLongerAvailable")){
                view.disconnectionForLobbyNoLongerAvailable();
                return true;
            }
        }
        return false;
    }

    public void parseIncomingMessage(){
        String answerType = document.getFirstChild().getNodeName();

        switch(answerType){
            case "UpdateMsg":
                parseUpdate();
                break;
            case "Answer":
                parseAnswer();
                break;
            case "ToDo":
                parseToDo();
                break;
            default:
                break;
        }


    }

    private void parseUpdate(){
        String mode = Objects.requireNonNull(evaluateXPath("/UpdateMsg/Mode/text()")).get(0);
        String username = Objects.requireNonNull(evaluateXPath("/UpdateMsg/Author/text()")).get(0);

        switch (mode){
            case "newPlayer" :
                String color =  Objects.requireNonNull(evaluateXPath("/UpdateMsg/Update/Color/text()")).get(0);
                String user =  Objects.requireNonNull(evaluateXPath("/UpdateMsg/Update/Username/text()")).get(0);
                view.updateNewUserLogged(user,Color.valueOfLabel(color));
                break;
            case "startGame" :
                view.showMatchStarted();
                break;
            case "createGods" :
                ArrayList<Integer> ids = new ArrayList<>();
                for(int i = 0; i < view.getPlayerNumber(); i++){
                    ids.add(Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/Gods/God[@n=\"" + i + "\"]/text()")).get(0)));
                }
                view.showGodsChallengerSelected(username,ids);
                break;
            case "choseGod" :
                int godId = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/godId/text()")).get(0));
                view.updateGodSelected(username,godId);
                break;
            case "choseStartingPlayer":
                String starter = Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/StartingPlayer/text()")).get(0);
                view.showStartingPlayer(starter);
                break;
            case "setWorkerOnBoard":
                String workerGender = Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/WorkerGender/text()")).get(0);
                int x = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/xPosition/text()")).get(0));
                int y = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/UpdateMsg/Update/yPosition/text()")).get(0));
                view.updatePlaceWorkerOnBoard(username,workerGender,x,y);
                break;
            case "move":
                NodeList positionsNode = document.getElementsByTagName("Position");
                List<HashMap<String,String>> positions = getActionData(positionsNode);
                //TODO notify view
                break;
            case "build":
                NodeList heightsNode = document.getElementsByTagName("Height");
                List<HashMap<String,String>> heights = getActionData(heightsNode);
                //TODO notify view
                break;
            case "endOfTurn":
                NodeList removeAndBuildNode = document.getElementsByTagName("RemoveAndBuild");
                List<HashMap<String,String>> removeAndBuild = getActionData(removeAndBuildNode);
                //TODO notify view
                break;
            case "youWinDirectly":
                //TODO notify view
                break;
            case "youLoseForDirectWin":
                //TODO notify view
                break;
            case  "youWinForAnotherLose":
                //TODO notify view
                break;
            case "youLoseForBlocked":
                //TODO notify view



        }
    }

    private void parseAnswer(){
        String mode = Objects.requireNonNull(evaluateXPath("/Answer/Mode/text()")).get(0);
        String username = Objects.requireNonNull(evaluateXPath("/Answer/Username/text()")).get(0);
        String outcome = Objects.requireNonNull(evaluateXPath("/Answer/Outcome/text()")).get(0);
        String nextStep;
        switch (mode){
            case "login" :
                if(outcome.equals("accepted")){
                    String color =  Objects.requireNonNull(evaluateXPath("/Answer/Update/Color/text()")).get(0);
                    String user =  Objects.requireNonNull(evaluateXPath("/Answer/Update/Username/text()")).get(0);
                    Map<String, Color> users = new HashMap<>();
                    NodeList components = document.getElementsByTagName("Component");
                    for (int j = 0; j < components.getLength(); j++) {
                            Node component = components.item(j);
                            if(!component.getChildNodes().item(0).getTextContent().equals(user))
                                users.put(component.getChildNodes().item(0).getTextContent(),Color.valueOfLabel(component.getChildNodes().item(1).getTextContent()));
                        }

                    view.updateLoginDone(users,user,Color.valueOfLabel(color));
                }
                else{
                    view.setUsername(true);
                }
                break;
            case "startGame" :
                if(outcome.equals("accepted")){
                    view.showMatchStarted();
                }
                else{
                    List<String> errors = getErrorList();
                }
                break;
            case "createGods" :
                if(outcome.equals("accepted")){
                    ArrayList<Integer> ids = new ArrayList<>();
                    for(int i = 0; i < view.getPlayerNumber(); i++){
                        ids.add(Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/Answer/Update/Gods/God[@n=\"" + i + "\"]/text()")).get(0)));
                    }
                    view.showGodsChoiceDone(ids);
                }
                else{
                    List<String> errors = getErrorList();
                }
                break;
            case "choseGod" :
                if(outcome.equals("accepted")){
                    int godId = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/Answer/Update/godId/text()")).get(0));
                    view.updateMyGodSelected(godId);
                }
                else{
                    List<String> errors = getErrorList();
                }
                break;
            case "choseStartingPlayer":
                if(outcome.equals("accepted")){
                    String starter = Objects.requireNonNull(evaluateXPath( "/Answer/Update/StartingPlayer/text()")).get(0);
                    view.showStartingPlayer(starter);

                }
                else{
                    List<String> errors = getErrorList();
                }
                break;
            case "setWorkerOnBoard":
                if(outcome.equals("accepted")){
                    String workerGender = Objects.requireNonNull(evaluateXPath( "/Answer/Update/WorkerGender/text()")).get(0);
                    int x = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/Answer/Update/xPosition/text()")).get(0));
                    int y = Integer.parseInt(Objects.requireNonNull(evaluateXPath( "/Answer/Update/yPosition/text()")).get(0));
                    view.updatePlaceMyWorkerOnBoard(workerGender,x,y);
                }
                else{
                    String workerGender = Objects.requireNonNull(evaluateXPath( "/Answer/Update/WorkerGender/text()")).get(0);
                    List<String> errors = getErrorList();
                    view.setWorkerOnBoard(workerGender,true);
                }
                break;
            case "move":
                if(outcome.equals("accepted")){
                    nextStep =  Objects.requireNonNull(evaluateXPath("/Answer/TurnNextStep/text()")).get(0);
                    NodeList positionsNode = document.getElementsByTagName("Position");
                    List<HashMap<String,String>> positions = getActionData(positionsNode);
                    System.out.println(positions);
                    //TODO notify view
                }
                else{
                    List<String> errors = getErrorList();
                    System.out.print(errors);
                }
                break;
            case "build":
                if(outcome.equals("accepted")){
                    nextStep =  Objects.requireNonNull(evaluateXPath("/Answer/TurnNextStep/text()")).get(0);
                    NodeList heightsNode = document.getElementsByTagName("Height");
                    List<HashMap<String,String>> heights = getActionData(heightsNode);
                    //TODO notify view
                }
                else{
                    List<String> errors = getErrorList();

                }
                break;

            case "endOfTurn":
                if(outcome.equals("accepted")){
                    nextStep =  Objects.requireNonNull(evaluateXPath("/Answer/TurnNextStep/text()")).get(0);
                    NodeList removeAndBuildNode = document.getElementsByTagName("RemoveAndBuild");
                    List<HashMap<String,String>> removeBuild = getActionData(removeAndBuildNode);
                    //TODO notify view
                }
                else{
                    List<String> errors = getErrorList();

                }


        }

    }

    private void parseToDo(){
        String action = Objects.requireNonNull(evaluateXPath("/ToDo/Action/text()")).get(0);
        switch(action){
            case "login":
                view.setUsername(false);
                break;
            case "canStartMatch":
                view.startMatch();
                break;
            case "createGods":
                view.selectGods();
                break;
            case "choseStartingPlayer":
                view.selectStartingPlayer();
                break;
            case "setupMaleWorkerOnBoard":
                view.setWorkerOnBoard("male",false);
                break;
            case "setupFemaleWorkerOnBoard":
                view.setWorkerOnBoard("female",false);
                break;
            case "wait":
                String waitFor = Objects.requireNonNull(evaluateXPath("/ToDo/Info/WaitFor/text()")).get(0);
                String inActionPlayer = Objects.requireNonNull(evaluateXPath("/ToDo/Info/InActionPlayer/text()")).get(0);
                view.showWaitMessage(waitFor,inActionPlayer);
                break;
            case "yourTurn":
                String possibleOperation = Objects.requireNonNull(evaluateXPath("/ToDo/Info/possibleOperation/text()")).get(0);
                //TODO notify view with info
                break;
            case "choseGod":
                NodeList gods = document.getElementsByTagName("God");
                ArrayList<Integer> godIds = new ArrayList<>();
                for(int i=0; i< gods.getLength(); i++){
                    godIds.add(Integer.parseInt(gods.item(i).getTextContent()));
                }
                view.selectGod(godIds);
        }

    }


    private List<String> getErrorList(){
        NodeList errorsNode = document.getElementsByTagName("Errors");
        Node errorNode = errorsNode.item(0);
        List<String>  errors = new ArrayList<>();
        NodeList allErrors = errorNode.getChildNodes();
        for (int j = 0; j < allErrors.getLength(); j++) {
            Node error = allErrors.item(j);
            errors.add(error.getNodeName());
        }
        return Collections.unmodifiableList(errors);
    }

    private List<HashMap<String,String>> getActionData(NodeList primaryNode){
        List<HashMap<String,String>> result = new ArrayList<>();
        for (int j = 0; j < primaryNode.getLength(); j++) {
            Node dataNode = primaryNode.item(j);
            NodeList dataNode_child = dataNode.getChildNodes();
            HashMap<String,String> data = new HashMap<>();
            for(int i = 0; i < dataNode_child.getLength();i++){
                Node child = dataNode_child.item(i);
                data.put(child.getNodeName(), child.getTextContent());
            }
            result.add(data);
        }
        return result;
    }

    /**
     * This methods uses XPath expressions to find nodes in xml documents
     * @param xpathExpression is the expression that identifies the node in the document
     * @return a List<String> containing the strings that match the expression
     */

    private List<String> evaluateXPath(String xpathExpression) {
        try {
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();

            List<String> values = new ArrayList<>();
            // Create XPathExpression object
            XPathExpression expr = xpath.compile(xpathExpression);

            // Evaluate expression result on XML document
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i).getNodeValue());
            }
            return values;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

}
