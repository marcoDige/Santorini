package it.polimi.ingsw.model.decorators;

import it.polimi.ingsw.model.Error;
import it.polimi.ingsw.model.Power;
import it.polimi.ingsw.model.PowerDecorator;
import it.polimi.ingsw.model.Worker;

import java.util.ArrayList;

/**
 * This class implements the power which allows worker to move into a square occupied by another worker and switch positions whit him.
 * This power decorates Apollo's power.
 * @author marcoDige
 */

public class MoveSwap extends PowerDecorator {

    //attributes

    public MoveSwap(Power p) {
        super(p);
    }

    //methods

    /**
     * This method overrides checkMove (Power Decorator) decorating decoratedPower with MoveSwap rules.
     * @param w is the worker that wants to move
     * @param x is the x square coordinate where the worker wants to move
     * @param y is the y square coordinate where the worker wants to move
     * @return an ArrayList that is empty if the move is legal, otherwise it contains the errors that prevent the worker from moving.
     */

    @Override
    public ArrayList<Error> checkMove(Worker w, int x, int y) {
        ArrayList<Error> errors = super.checkMove(w, x, y);
        if(!errors.isEmpty()) errors.remove(Error.NOT_FREE);
        return errors;
    }

    /**
     * This method overrides updateMove (PowerDecorator) decorating decoratedPower with MoveSwap rules.
     * @param w is the worker that moves
     * @param x is the x square coordinate where the worker moves
     * @param y is the y square coordinate where the worker moves
     */

    @Override
    public void updateMove(Worker w, int x, int y) {
        Worker opw = getBoard().getSquare(x, y).removeWorker();
        super.updateMove(w, x, y);
        opw.updateWorkerPosition(w.getLastSquareMove());
    }
}
