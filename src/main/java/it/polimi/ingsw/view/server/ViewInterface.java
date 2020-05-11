package it.polimi.ingsw.view.server;

import it.polimi.ingsw.model.enums.Error;
import it.polimi.ingsw.msgUtilities.server.UpdateMsgWriter;
import it.polimi.ingsw.network.MsgSender;
import org.w3c.dom.Document;
import java.util.List;

/**
 * This interface defines the view's method that controller can call.
 * @author marcoDige
 */

public interface ViewInterface {

    void onMoveAcceptedRequest(String username, Document answerMsg, Document updateMsg);
    void onBuildAcceptedRequest(String username, Document answerMsg, Document updateMsg);
    void onEndOfTurnAcceptedRequest(String username, Document answerMsg, Document updateMsg);
    void onRejectedRequest(String username, List<Error> errors, String mode);

    void toDoChoseStartingPlayer();
    void toDoTurn(String username, String firstOperation);
    void toDoSetupWorkerOnBoard(String username, String gender);
    void toDoChoseGod(String username, List<Integer> ids);

    public void directlyWinCase(String winnerUsername);
    public void match2PlayerLose(String loserUsername);
    public void match3PlayerLose(String loserUsername);
}
