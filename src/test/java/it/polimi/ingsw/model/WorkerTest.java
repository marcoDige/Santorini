package it.polimi.ingsw.model;


import it.polimi.ingsw.model.enums.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test suite for Worker class
 * @author aledimaio
 */

class WorkerTest {

    Board gameBoard;
    Worker worker;

    @BeforeEach
    void setUp() {
        gameBoard = new Board();
        worker = new Worker(Color.GREY, "female");
    }

    @Test
    void removeFromGame() {
        worker.setWorkerOnBoard(gameBoard.getSquare(4,4));
        worker.removeFromGame();
        assertNull(worker.getCurrentSquare());
        assertNull(worker.getLastSquareMove());
        assertNull(worker.getLastSquareBuild());
    }

    @Test
    void setWorkerOnBoard() {
        // Check invalid argument exception
        assertThrows(IllegalArgumentException.class, () -> worker.setWorkerOnBoard(null));

        // Check normal functioning
        worker.setWorkerOnBoard(gameBoard.getSquare(1,1));
        assertEquals(worker, gameBoard.getSquare(1,1).getWorker());
        assertEquals(gameBoard.getSquare(1,1),worker.getCurrentSquare());

        Worker w2 = new Worker(Color.ORANGE,"female");
        w2.setWorkerOnBoard(gameBoard.getSquare(1,2));
        gameBoard.getSquare(1,1).removeWorker();

        //Check that it's impossible to set worker onto occupied square
        assertThrows(IllegalStateException.class, () -> worker.setWorkerOnBoard(gameBoard.getSquare(1,2)));
    }

    @Test
    void updateWorkerPosition() {
        // Check invalid argument exception
        assertThrows(IllegalArgumentException.class, () -> worker.updateWorkerPosition(null));

        // Check normal functioning
        // Set the worker on the board
        worker.setWorkerOnBoard(gameBoard.getSquare(2,1));
        assertEquals(worker,gameBoard.getSquare(2,1).getWorker());
        assertEquals(worker.getCurrentSquare(),gameBoard.getSquare(2,1));

        worker.updateWorkerPosition(gameBoard.getSquare(2,2));
        assertEquals(worker, gameBoard.getSquare(2,2).getWorker());
        assertEquals(worker.getLastSquareMove(), gameBoard.getSquare(2,1));
        assertEquals(worker.getCurrentSquare(), gameBoard.getSquare(2,2));

        //check that it's impossible to update worker position with an occupied position
        Worker w2 = new Worker(Color.ORANGE,"female");
        w2.setWorkerOnBoard(gameBoard.getSquare(1,2));

        assertThrows(IllegalStateException.class, () -> worker.updateWorkerPosition(gameBoard.getSquare(1,2)));
    }

    @Test
    void OnOffIsMoving(){
        worker.isMovingOn();
        assertTrue(worker.getIsMoving());

        worker.isMovingOff();
        assertFalse(worker.getIsMoving());
    }
}