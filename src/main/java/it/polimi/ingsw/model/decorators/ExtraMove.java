package it.polimi.ingsw.model.decorators;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.enums.Error;

import java.util.ArrayList;

/**
 * This class implements the power to move one more time under some conditions.
 * @author pierobartolo
 */
public class ExtraMove extends PowerDecorator {

    //attributes
    /**
     * (If true) The worker may move again but not back to its initial space.
     */
    public boolean notMoveBack;

    /**
     * (If true) The worker may move again if it moves into a perimeter space.
     */
    public boolean onPerimeter;

    //constructors

    public ExtraMove(Power p, boolean nmb, boolean op) {
        super(p);
        this.notMoveBack = nmb;
        this.onPerimeter = op;
    }



    //methods

    /**
     * This method overrides checkMove (Power Decorator) decorating decoratedPower with ExtraMove rules.
     * @param w is the worker that wants to move
     * @param x is the x square coordinate where the worker wants to move
     * @param y is the y square coordinate where the worker wants to move
     * @return an ArrayList that is empty if the move is legal, otherwise it contains the errors that prevent the worker from moving.
     */

    @Override
    public ArrayList<Error> checkMove(Worker w, int x, int y) {
        ArrayList<Error> errors = decoratedPower.checkMove(w, x, y);
        if (!errors.contains(Error.MOVES_EXCEEDED) && decoratedPower.getBoard().getNMoves() != 0 && notMoveBack) { // ExtraMove
            if(w.getLastSquareMove().getXPosition() == x && w.getLastSquareMove().getYPosition() == y)
                errors.add(Error.EXTRA_MOVE_NOT_BACK);
        }

        int cx = w.getCurrentSquare().getXPosition(), cy = w.getCurrentSquare().getYPosition();
        if(onPerimeter && (cx == 0 || cx == Board.SIZE-1 || cy == 0 || cy == Board.SIZE-1))
            errors.remove(Error.MOVES_EXCEEDED);

        return errors;
    }

    /**
     * This method overrides updateMove (PowerDecorator) and allows to modify nexStepIndication into xml request answer.
     * @param w is the worker that moves
     * @param x is the x square coordinate where the worker moves
     * @param y is the y square coordinate where the worker moves
     */

    @Override
    public void updateMove(Worker w, int x, int y){
        decoratedPower.updateMove(w,x,y);
        int cx = w.getCurrentSquare().getXPosition();
        int cy = w.getCurrentSquare().getYPosition();
        if(onPerimeter && (cx == 0 || cx == Board.SIZE-1 || cy == 0 || cy == Board.SIZE-1))
            getBoard().getMsgContainer().nextStepIndication("move/build");
    }

}
