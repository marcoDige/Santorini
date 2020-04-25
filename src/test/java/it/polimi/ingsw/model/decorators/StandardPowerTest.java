package it.polimi.ingsw.model.decorators;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.enums.Color;
import it.polimi.ingsw.model.enums.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for StandardPower class
 * @author marcoDige & pierobartolo
 */
class StandardPowerTest {

    Board b;
    Player p1, p2;
    Worker wmp1, wfp1, wmp2, wfp2;
    Power p;

    /**
     * Setup for testing :
     *  - 2 players
     *  - all workers set on board
     *  - StandardPower Object
     */

    @BeforeEach
    public void setUp() {
        b = new Board();
        p1 = new Player("marcoDige", Color.AZURE);
        p2 = new Player("pierobartolo", Color.WHITE);
        wmp1 = p1.getWorkers().get(0);
        wfp1 = p1.getWorkers().get(1);
        wmp2 = p2.getWorkers().get(0);
        wfp2 = p2.getWorkers().get(1);

        wmp1.setWorkerOnBoard(b.getSquare(1, 1));
        wfp1.setWorkerOnBoard(b.getSquare(3, 4));
        wmp2.setWorkerOnBoard(b.getSquare(0, 1));
        wfp2.setWorkerOnBoard(b.getSquare(3, 2));

        p = new StandardPower(1, 1, b);
    }

    /**
     * This method tests if parameters generate IllegalArgumentException when checkMove is called.
     */

    @Test
    void checkMoveException() {
        assertThrows(IllegalArgumentException.class, () -> p.checkMove(wfp1, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> p.checkMove(wfp1, 1, 8));
        assertThrows(IllegalArgumentException.class, () -> p.checkMove(wfp1, 5, 4));
        assertThrows(IllegalArgumentException.class, () -> p.checkMove(null, 3, 4));
    }

    @Test
    void checkValidMove() {
        //chose worker for the turn
        wmp1.isMovingOn();

        //move on the same level
        assertTrue(p.checkMove(wmp1, 2, 1).isEmpty());

        //move up
        b.getSquare(1, 0).buildLevel(1);
        assertTrue(p.checkMove(wmp1, 1, 0).isEmpty());

        //move down 2 level
        b.getSquare(1, 0).buildLevel(2);
        wmp1.updateWorkerPosition(b.getSquare(1, 0));
        assertTrue(p.checkMove(wmp1, 2, 0).isEmpty());
    }

    @Test
    void checkMoveNotFree() {
        //chose worker for the turn
        wmp1.isMovingOn();

        ArrayList<Error> errors = p.checkMove(wmp1, 0, 1);

        // (0,1) position is occupy by wmp2
        assertTrue(errors.contains(Error.NOT_FREE));
        assertEquals(1, errors.size());
    }

    @Test
    void checkMovesNotAdjacent() {
        //chose worker for the turn
        wmp1.isMovingOn();

        ArrayList<Error> errors = p.checkMove(wmp1, 3, 1);

        // wmp1 is in (1,1) position that isn't adjacent to (3,1) position
        assertTrue(errors.contains(Error.NOT_ADJACENT));
        assertEquals(1, errors.size());
    }

    @Test
    void checkMovesInvalidLevel(){
        //chose worker for the turn
        wmp1.isMovingOn();

        b.getSquare(0,0).buildLevel(2);

        // wmp1 is on level 0 square. He is trying to move into a level 2 square
        ArrayList<Error> errors = p.checkMove(wmp1, 0, 0);
        assertTrue(errors.contains(Error.INVALID_LEVEL_MOVE));
        assertEquals(1, errors.size());
    }

    @Test
    void checkMovesExceeded(){
        //chose worker for the turn
        wmp1.isMovingOn();

        p.updateMove(wmp1,1,2);

        // wmp1 is trying to move 2 times, for standard power it is not possible
        ArrayList<Error> errors = p.checkMove(wmp1, 1, 3);
        assertTrue(errors.contains(Error.MOVES_EXCEEDED));
        assertEquals(1, errors.size());
    }

    @Test
    void checkMoveIsDome(){
        //chose worker for the turn
        wmp1.isMovingOn();

        b.getSquare(0,0).setDome(true);

        //wmp1 is trying to move into a square occupied by dome
        ArrayList<Error> errors = p.checkMove(wmp1, 0, 0);
        assertTrue(errors.contains(Error.IS_DOME));
        assertEquals(1, errors.size());
    }

    @Test
    void checkMoveAfterBuild(){
        //chose worker for the turn
        wmp1.isMovingOn();

        p.updateBuild(wmp1,0,0, 1);

        //wmp1 is trying to move after a build move
        ArrayList<Error> errors = p.checkMove(wmp1,0,0);
        assertTrue(errors.contains(Error.MOVE_AFTER_BUILD));
        assertEquals(1, errors.size());
    }

    @Test
    void checkIsntWorkerChosen(){
        //chose worker for the turn
        wmp1.isMovingOn();

        //wfp1 is trying to move
        ArrayList<Error> errors = p.checkMove(wfp1,4,4);
        assertTrue(errors.contains(Error.ISNT_WORKER_CHOSEN));
        assertEquals(1, errors.size());
    }

    /**
     * This method tests all errors that checkMove can return together (except IS_FREE).
     */

    @Test
    void checkAllIllegalMoves() {
        p.updateMove(wmp1, 1, 2);
        b.getSquare(4,4).buildLevel(3);
        p.updateBuild(wfp1, 4, 4,4);

        ArrayList<Error> errors = p.checkMove(wmp1, 4, 4);
        assertTrue(errors.contains(Error.MOVES_EXCEEDED));
        assertTrue(errors.contains(Error.MOVE_AFTER_BUILD));
        assertTrue(errors.contains(Error.NOT_ADJACENT));
        assertTrue(errors.contains(Error.INVALID_LEVEL_MOVE));
        assertTrue(errors.contains(Error.IS_DOME));
        assertTrue(errors.contains(Error.ISNT_WORKER_CHOSEN));
        assertEquals(6, errors.size());
    }

    /**
     * This method tests if parameters generate IllegalArgumentException when checkBuild is called.
     */

    @Test
    void checkBuildException() {
        assertThrows(IllegalArgumentException.class, () -> p.checkBuild(wfp1, -1, 0,1));
        assertThrows(IllegalArgumentException.class, () -> p.checkBuild(wfp1, 1, 8,1));
        assertThrows(IllegalArgumentException.class, () -> p.checkBuild(wfp1, 5, 4,1));
        assertThrows(IllegalArgumentException.class, () -> p.checkBuild(null, 3, 4,1));
        assertThrows(IllegalArgumentException.class, () -> p.checkBuild(wfp1,1,2,0));
    }

    @Test
    void checkBuildLegal() {
        //chose worker for the turn
        wmp1.isMovingOn();

        //worker has to move before build
        p.updateMove(wmp1, 2, 1);
        assertTrue(p.checkBuild(wmp1, 1, 1, 1).isEmpty());
    }

    @Test
    void checkBuildNotFree() {
        //chose worker for the turn
        wfp2.isMovingOn();

        //worker has to move before build
        p.updateMove(wfp2, 3, 3);
        ArrayList<Error> errors = p.checkBuild(wfp2,3,4, 1);

        // (3,1) position is occupied by wfp1
        assertTrue(errors.contains(Error.NOT_FREE));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildsExceeded(){
        //chose worker for the turn
        wmp2.isMovingOn();

        //worker has to move before build
        p.updateMove(wmp2,0,2);

        p.updateBuild(wmp2,0,3,1);

        //worker is trying to build 2 times, for standard power it is not possible
        ArrayList<Error> errors = p.checkBuild(wmp2,1,3, 1);
        assertTrue(errors.contains(Error.BUILDS_EXCEEDED));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildNotAdjacent(){
        //chose worker for the turn
        wfp1.isMovingOn();

        //worker has to move before build
        p.updateMove(wfp1,4,4);

        //wfp1 is in (3,4) position that isn't adjacent to (4,2) position
        ArrayList<Error> errors = p.checkBuild(wfp1,4,2, 1);
        assertTrue(errors.contains(Error.NOT_ADJACENT));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildIsDome(){
        //chose worker for the turn
        wfp1.isMovingOn();

        //worker has to move before build
        p.updateMove(wfp1,3,3);
        b.getSquare(4,4).setDome(true);

        //worker is trying to build onto a square where there is a dome
        ArrayList<Error> errors = p.checkBuild(wfp1,4,4, 1);
        assertTrue(errors.contains(Error.IS_DOME));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildBeforeMove(){
        //chose worker for the turn
        wfp1.isMovingOn();

        //worker is trying to build before his move
        ArrayList<Error> errors = p.checkBuild(wfp1,4,4, b.getSquare(4,4).getLevel() + 1);
        assertTrue(errors.contains(Error.BUILD_BEFORE_MOVE));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildInvalidLevel(){
        //chose worker for the turn
        wfp1.isMovingOn();

        //worker has to move before build
        p.updateMove(wfp1,3,3);

        //worker is trying to build onto a square where there is a dome
        ArrayList<Error> errors = p.checkBuild(wfp1,4,4, 2);
        assertTrue(errors.contains(Error.INVALID_LEVEL_BUILD));
        assertEquals(1, errors.size());
    }

    @Test
    void checkBuildIsntWorkerChosen(){
        //chose worker for the turn
        wmp1.isMovingOn();

        //worker has to move before build
        p.updateMove(wmp1,1,2);

        //worker is trying to build onto a square where there is a dome
        ArrayList<Error> errors = p.checkBuild(wfp1,4,4, 1);
        assertTrue(errors.contains(Error.ISNT_WORKER_CHOSEN));
        assertEquals(1, errors.size());
    }

    /**
     * This method tests all errors that checkMove can return together (except IS_FREE).
     */

    @Test
    void checkAllIllegalBuild(){
        p.updateBuild(wmp1, 1, 1,1);
        b.getSquare(3,3).buildLevel(3);
        p.updateBuild(wfp2, 3, 3,4);

        ArrayList<Error> errors = p.checkBuild(wmp1, 3, 3,2);
        assertTrue(errors.contains(Error.BUILDS_EXCEEDED));
        assertTrue(errors.contains(Error.BUILD_BEFORE_MOVE));
        assertTrue(errors.contains(Error.NOT_ADJACENT));
        assertTrue(errors.contains(Error.IS_DOME));
        assertTrue(errors.contains(Error.INVALID_LEVEL_BUILD));
        assertTrue(errors.contains(Error.ISNT_WORKER_CHOSEN));
        assertEquals(6, errors.size());
    }

    @Test
    void checkWin() {
        assertThrows(IllegalArgumentException.class, () -> p.checkWin(null));

        //creating a ladder to win (we test checkWin with wmp1)
        Square l2, l3;
        l2 = b.getSquare(2, 0);
        l3 = b.getSquare(3, 0);

        //level 2
        for (int i = 0; i < 2; i++)
            l2.buildLevel(i+1);

        //level 3
        for (int i = 0; i < 3; i++)
            l3.buildLevel(i+1);

        assertEquals(2, l2.getLevel());
        assertEquals(3, l3.getLevel());

        //set wmp1 on level 2 position;
        wmp1.updateWorkerPosition(l2);
        assertEquals(wmp1, l2.getWorker());
        assertFalse(p.checkWin(wmp1));

        //move wmp2 on level 3 position;
        p.updateMove(wmp1,3,0);
        assertEquals(wmp1, l3.getWorker());

        // worker win (his move is from level 2 to level 3)
        assertTrue(p.checkWin(wmp1));
    }

    /**
     * This method tests all cases updateMove can create (exceptions and update model state)
     */

    @Test
    void updateMove() {
        assertThrows(IllegalArgumentException.class, () -> p.updateMove(wfp1, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> p.updateMove(wfp1, 1, 8));
        assertThrows(IllegalArgumentException.class, () -> p.updateMove(wfp1, 5, 4));
        assertThrows(IllegalArgumentException.class, () -> p.updateMove(null, 3, 4));
        assertThrows(IllegalStateException.class, () -> p.updateMove(wfp1, 3, 4));

        //move wmp1 to (2,1)
        p.updateMove(wmp1, 2, 1);
        assertEquals(wmp1, b.getSquare(2, 1).getWorker());
        assertEquals(wmp1.getCurrentSquare(), b.getSquare(2, 1));
        assertEquals(b.getSquare(1, 1), wmp1.getLastSquareMove());
        assertNull(wmp1.getLastSquareMove().getWorker());
        assertEquals(1, b.getNMoves());
        assertTrue(b.getSquare(1, 1).isFree());

        //move wmp1 to (2,2)
        p.updateMove(wmp1, 2, 2);
        assertEquals(wmp1, b.getSquare(2, 2).getWorker());
        assertEquals(wmp1.getCurrentSquare(), b.getSquare(2, 2));
        assertEquals(b.getSquare(2, 1), wmp1.getLastSquareMove());
        assertNull(wmp1.getLastSquareMove().getWorker());
        assertEquals(2, b.getNMoves());
        assertTrue(b.getSquare(2, 1).isFree());
    }

    /**
     * This method tests all cases updateMove can create (exceptions and update model state)
     */

    @Test
    void updateBuild() {
        assertThrows(IllegalArgumentException.class, () -> p.updateBuild(wfp1, -1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> p.updateBuild(wfp1, 1, 8, 1));
        assertThrows(IllegalArgumentException.class, () -> p.updateBuild(wfp1, 5, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> p.updateBuild(wfp1,3,4,5));
        assertThrows(IllegalArgumentException.class, () -> p.updateMove(null, 3, 4));
        assertThrows(IllegalStateException.class, () -> p.updateMove(wfp1, 3, 4));

        //wmp1 builds on (2,1)
        p.updateBuild(wmp1, 2, 1, b.getSquare(2,1).getLevel() + 1);
        assertEquals(wmp1.getLastSquareBuild(), b.getSquare(2, 1));
        assertEquals(1, b.getSquare(2, 1).getLevel());
        assertFalse(b.getSquare(2, 1).getDome());
        assertEquals(1, b.getNBuild());
    }

    @Test
    void endOfTurnException(){
        assertThrows(IllegalArgumentException.class, () -> p.endOfTurn(null));
        wmp1.removeFromGame();
        wfp1.removeFromGame();
        assertThrows(IllegalArgumentException.class, () -> p.endOfTurn(p1.getWorkers()));
    }

    @Test
    void endOfTurn(){
        //chose worker for the turn
        wmp1.isMovingOn();

        //check that player have to do at least a standard move to finis his turn
        assertFalse(p.endOfTurn(p1.getWorkers()));
        p.updateMove(wmp1,1,2);
        assertFalse(p.endOfTurn(p1.getWorkers()));

        p.updateBuild(wmp1,1,1,1);

        //check that player can finish his turn after a standard move
        assertTrue(p.endOfTurn(p1.getWorkers()));

        //check that all flags in model are reset
        assertEquals(0, b.getNMoves());
        assertEquals(0, b.getNBuild());
        assertFalse(wmp1.getIsMoving());
    }

}