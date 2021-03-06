package it.polimi.ingsw.model.decorators;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.enums.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for NoWinPerimeter class
 * @author aledimaio
 */

class NoWinPerimeterTest {

    Board b;
    Player p1, p2, p3;

    /**
     * Setup for testing:
     * - 1 Player --> Hera
     * - 2 Player --> Apollo
     * - 3 Player --> Zeus
     * - all workers set on board
     */

    @BeforeEach
    void setUp() {

        b = new Board();
        GodsFactory factory = new GodsFactory(b);
        p1 = new Player("pierobartolo", Color.ORANGE);
        p2 = new Player("marcodige",Color.GREY);
        p3 = new Player("aledimaio",Color.AZURE);
        ArrayList<God> gods = factory.getGods( new ArrayList<>(Arrays.asList(20,1,30)));
        p1.setGod(Objects.requireNonNull(gods.get(0))); // Hera
        p2.setGod(Objects.requireNonNull(gods.get(1))); // Apollo
        p3.setGod(Objects.requireNonNull(gods.get(2))); // Zeus

        // p1 sets workers
        p1.getWorkers().get(0).setWorkerOnBoard(b.getSquare(0,0));
        p1.getWorkers().get(1).setWorkerOnBoard(b.getSquare(1,1));

        // p2 sets workers
        p2.getWorkers().get(0).setWorkerOnBoard(b.getSquare(4,4));
        p2.getWorkers().get(1).setWorkerOnBoard(b.getSquare(3,1));

        // p3 sets workers
        p3.getWorkers().get(0).setWorkerOnBoard(b.getSquare(1,3));
        p3.getWorkers().get(1).setWorkerOnBoard(b.getSquare(2,4));

    }

    /**
     * This method tests if Hera can win with the standard win condition.
     */

    @Test
    void checkStandardWinForHera() {

        b.getSquare(1,1).buildLevel(2);
        b.getSquare(2,2).buildLevel(3);

        assertEquals(p1.getWorkers().get(0), b.getSquare(0,0).getWorker());

        //chose worker for the turn
        p1.getWorkers().get(1).isMovingOn();

        p1.move(p1.getWorkers().get(1), 2,2);

        assertTrue(p1.getGod().getPower().checkWin(p1.getWorkers().get(1)));

    }

    /**
     * This method check that Hera can win moving into a perimeter space. when it's coming from a perimeter space.
     */

    @Test
    void checkHeraWinConditionHerself_Perimeter(){
        //chose worker for the turn
        p1.getWorkers().get(0).isMovingOn();

        // Build levels
        b.getSquare(0,0).buildLevel(2);
        b.getSquare(0,1).buildLevel(3);
        assertEquals(2,b.getSquare(0,0).getLevel());
        assertEquals(3,b.getSquare(0,1).getLevel());

        // Move and win
        assertTrue(p1.move(p1.getWorkers().get(0),0,1).isEmpty());

        // Check victory
        assertTrue(p1.getGod().getPower().checkWin(p1.getWorkers().get(0)));

    }

    /**
     * This method check that Hera can win moving into a perimeter space when it's coming from a regular square.
     */

    @Test
    void checkHeraWinConditionHerself_notPerimeter(){
        //chose worker for the turn
        p1.getWorkers().get(1).isMovingOn();

        // Build levels
        b.getSquare(1,1).buildLevel(2);
        b.getSquare(0,1).buildLevel(3);
        assertEquals(2,b.getSquare(1,1).getLevel());
        assertEquals(3,b.getSquare(0,1).getLevel());

        // Move and win
        assertTrue(p1.move(p1.getWorkers().get(1), 0,1).isEmpty());

        // Check victory
        assertTrue(p1.getGod().getPower().checkWin(p1.getWorkers().get(1)));

    }

    /**
     * This method check if standard win condition still works for other players (Apollo) that move into a non-perimeter space.
     */

    @Test
    void checkOthersStandardWinCondition_Apollo(){
        //chose worker for the turn
        p2.getWorkers().get(0).isMovingOn();

        // Build levels
        b.getSquare(4,4).buildLevel(2);
        b.getSquare(3,3).buildLevel(3);
        assertEquals(2,b.getSquare(4,4).getLevel());
        assertEquals(3,b.getSquare(3,3).getLevel());

        // Move and win
        assertTrue(p2.move(p2.getWorkers().get(0),3,3).isEmpty());

        // Check victory
        assertTrue(p2.getGod().getPower().checkWin(p2.getWorkers().get(0)));

    }

    /**
     * This method check if standard win condition still works for other players (Zeus) that move into a non-perimeter space.
     */

    @Test
    void checkOthersStandardWinCondition_Zeus(){
        //chose worker for the turn
        p3.getWorkers().get(0).isMovingOn();

        // Build levels
        b.getSquare(1,3).buildLevel(2);
        b.getSquare(1,2).buildLevel(3);
        assertEquals(2,b.getSquare(1,3).getLevel());
        assertEquals(3,b.getSquare(1,2).getLevel());

        // Move and win
        assertTrue(p3.move(p3.getWorkers().get(0),1,2).isEmpty());

        // Check victory
        assertTrue(p3.getGod().getPower().checkWin(p3.getWorkers().get(0)));

    }


    /**
     * This method tests the impossibility to win for a player moving into a perimeter space
     * case 1: moving from a perimeter space
     * Hera's power: An opponent cannot win by moving into a perimeter space.
     */

    @Test
    void checkCannotWinOnPerimeter_fromPerimeter(){
        //chose worker for the turn
        p2.getWorkers().get(0).isMovingOn();

        // Build levels
        b.getSquare(4,4).buildLevel(2);
        b.getSquare(3,4).buildLevel(3);
        assertEquals(2,  b.getSquare(4,4).getLevel());
        assertEquals( 3,b.getSquare(3,4).getLevel());

        // Move and notWin
        assertTrue(p2.move(p2.getWorkers().get(0),3,4).isEmpty());

        // Check that the player didn't win
        assertFalse(p2.getGod().getPower().checkWin(p2.getWorkers().get(0)));

    }

    /**
     * This method tests the impossibility to win for a player moving into a perimeter space
     * case 2: moving from a non-perimeter space (regular space)
     * Hera's power: An opponent cannot win by moving into a perimeter space.
     */

    @Test
    void checkCannotWinOnPerimeter_fromRegular(){
        //chose worker for the turn
        p3.getWorkers().get(0).isMovingOn();

        // Build levels
        b.getSquare(1,3).buildLevel(2);
        b.getSquare(1,4).buildLevel(3);
        assertEquals(2,b.getSquare(1,3).getLevel());
        assertEquals(3,b.getSquare(1,4).getLevel());

        // Move and notWin
        assertTrue(p3.move(p3.getWorkers().get(0), 1,4).isEmpty());

        // Check that the player didn't win
        assertFalse(p3.getGod().getPower().checkWin(p3.getWorkers().get(0)));

    }

}