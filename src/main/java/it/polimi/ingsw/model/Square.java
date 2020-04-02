package it.polimi.ingsw.model;

/**
 * Square represents a single box of the entire board/table.
 * xPosition and yPosition can't be changed once square has been created.
 * @author aledimaio
 */

public class Square {

    //attributes

    /**
     * Square class have attributes to represents its position in the board/table (xPosition and yPosition), the worker
     * on it, the level and if there is a dome or not
     */

    private int level;
    private boolean dome;
    private int xPosition;
    private int yPosition;
    private Worker worker;

    //constructors

    /**
     * Class constructors which constructs a Square with a specified coordinate.
     * @param xPosition is the square position in table
     * @param yPosition is the square position in table
     */

    public Square(int xPosition, int yPosition){
        this.level = 0;
        this.dome = false;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    //methods

    public void setWorker(Worker worker) {
        if(this.isFree())
            this.worker = worker;
    }

    public void setDome(boolean d) {
        this.dome = d;
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public Worker getWorker() {
        return worker;
    }

    public int getLevel() {
        return level;
    }

    public boolean getDome() {
        return dome;
    }

    /**
     * This method allows to remove the worker from current square, it is useful when a worker moves in another square.
     * @return the worker that method removed
     */

    public Worker removeWorker() {
        Worker w = worker;
        worker = null;
        return w;
    }

    /**
     * This method checks if the Square is not occupied by a worker.
     */

    public boolean isFree() {
        return (worker == null);
    }

    /**
     * This method checks if in the square there is a complete tower (the level is 3 and there is a dome).
     */

    public boolean isCompleteTower() {
        return (level == 3 && dome);
    }

    /**
     * This method allows to build on the square (increment the level)
     */

    public void buildLevel() {
            if(isCompleteTower())
                throw new IllegalStateException("Can't build: the tower is complete!");
            else if (level == 3)
                dome = true;
            else
                ++level;
    }


}
