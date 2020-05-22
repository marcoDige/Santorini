package it.polimi.ingsw.model;

import it.polimi.ingsw.msgUtilities.server.AnswerMsgWriter;
import it.polimi.ingsw.msgUtilities.server.UpdateMsgWriter;
import org.w3c.dom.Document;

//TODO : javadoc

public class ModelMsgContainer {

    //attributes

    private final UpdateMsgWriter updateMsgWriter;
    private final AnswerMsgWriter answerMsgWriter;
    private Document updateMsg, answerMsg;

    //constructors

    public ModelMsgContainer(String username, String mode){
        this.updateMsgWriter = new UpdateMsgWriter();
        this.answerMsgWriter = new AnswerMsgWriter();
        updateMsg = updateMsgWriter.setStandardUpdateValues(username,mode);
        answerMsg = answerMsgWriter.setStandardAnswerValues(username,mode,"accepted");
    }

    //methods

    //TODO : javadoc

    public Document getAnswerMsg() {
        return answerMsg;
    }

    //TODO : javadoc

    public Document getUpdateMsg(){
        return updateMsg;
    }

    //TODO : javadoc

    public void updateMove(int index, int startX, int startY, int x, int y){
        answerMsg = answerMsgWriter.moveUpdate(index,startX,startY,x,y);
        updateMsg = updateMsgWriter.moveUpdate(index,startX,startY,x,y);
    }

    //TODO : javadoc

    public void updateBuild(int index, int startX, int startY, int x, int y, int level){
        answerMsg = answerMsgWriter.buildUpdate(index,startX,startY,x,y,level);
        updateMsg = updateMsgWriter.buildUpdate(index,startX,startY,x,y,level);
    }

    //TODO : javadoc

    public void nextStepIndication(String nextStep){
        answerMsg = answerMsgWriter.nextStepTurnIndication(nextStep);
    }

    //TODO : javadoc

    public void endOfTurnRemoveAndBuildUpdate(int startX,int startY,int level){
        answerMsg = answerMsgWriter.endOfTurnRemoveAndBuildUpdate(startX,startY,level);
        updateMsg = updateMsgWriter.endOfTurnRemoveAndBuildUpdate(startX,startY,level);
    }
}
