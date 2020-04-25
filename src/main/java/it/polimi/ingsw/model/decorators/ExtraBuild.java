package it.polimi.ingsw.model.decorators;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.enums.Error;

import java.util.ArrayList;

/**
 * This class implements the power to build one additional block under some conditions.
 * This power decorates Hephaestus, Demeter and Hestia's powers.
 * @author pierobartolo
 */

public class ExtraBuild extends PowerDecorator {

    //attributes
    /**
     * (If true) The worker may build one additional block on top of his first block (not a dome).
     */
    public boolean onlySameSpace;

    /**
     * (If true) The worker may build one additional block but not on top of his first block.
     */
    public boolean notSameSpace;

    /**
     * (If true) The worker may build one additional block but not on a perimeter space.
     */
    public boolean notPerimeter;

    //constructors

    public ExtraBuild(Power p, boolean oss, boolean nss, boolean np) {
        super(p);
        this.onlySameSpace = oss;
        this.notSameSpace = nss;
        this.notPerimeter = np;
    }


    //methods

    /**
     * This method overrides checkBuild (PowerDecorator) decorating decoratedPower with ExtraBuild rules.
     * @param w is the worker that wants to build
     * @param x is the x square coordinate where the worker wants to build
     * @param y is the y square coordinate where the worker wants to build
     * @param l is the level the worker wants to build
     * @return an ArrayList that is empty if the build is legal, otherwise it contains the errors that prevent the worker from building.
     */
    @Override
    public ArrayList<Error> checkBuild(Worker w, int x, int y, int l) {
        ArrayList<Error> errors = decoratedPower.checkBuild(w, x, y, l);
        if (!errors.contains(Error.BUILDS_EXCEEDED) && decoratedPower.getBoard().getNBuild() != 0) { // Extra Build
            if (onlySameSpace) {
                if(w.getLastSquareBuild().getXPosition() != x || w.getLastSquareBuild().getYPosition() != y)
                    errors.add(Error.EXTRA_BUILD_ONLY_SAME_SPACE);
                if(l == 4)
                    errors.add(Error.EXTRA_BUILD_NOT_DOME);
            }
            if (notSameSpace && (w.getLastSquareBuild().getYPosition() == x && w.getLastSquareBuild().getYPosition() == y)) {
                errors.add(Error.EXTRA_BUILD_NOT_SAME_SPACE);
            }
        if (notPerimeter && (x == 0 || x == Board.SIZE-1 || y == 0 || y == Board.SIZE-1)) {
                errors.add(Error.EXTRA_BUILD_NOT_PERIMETER);
            }
        }
        return errors;

    }

}
