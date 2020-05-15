package it.polimi.ingsw.view.client.cli;

/**
 * This class represents a single square
 * @author aledimaio
 */

public class Square{

    private final int x;
    private final int y;
    private Worker worker;
    private int level;
    private boolean dome;

    /**
     * This attribute is used to check if the player has selected this square when a worker is on it
     */

    private boolean workerSelected = false;

    /**
     * When the constructor is invoked the square is initialized at level zero without any worker on it
     */

    public Square(int x, int y) {
        this.x = x;
        this.y = y;
        this.level = 0;
        this.dome = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Worker getWorker() {
        return worker;
    }

    public int getLevel() {
        return level;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * This method draw a single square
     */

    public void drawSquare(){

        //change color of single square based on level of square

        if(dome)
            System.out.print(ColorCode.LEVEL_DOME_BLUE_BACKGROUND.escape());
        else {
            switch (level) {
                case 0:
                    System.out.print(ColorCode.LEVEL_0_GREEN_BACKGROUND.escape());
                    break;
                case 1:
                    System.out.print(ColorCode.LEVEL_1_SAND_BACKGROUND.escape());
                    break;
                case 2:
                    System.out.print(ColorCode.LEVEL_2_GRAY_BACKGROUND.escape());
                    break;
                case 3:
                    System.out.print(ColorCode.LEVEL_3_WHITE_BACKGROUND.escape());
                    break;
            }
        }

        if (worker != null) {
            System.out.println(Escapes.SAVE_CURSOR_POSITION.escape() + worker.getColorCode().escape() + worker.getIcon() + "   "
                    + Escapes.RESTORE_CURSOR_POSITION.escape());
            for (int i = 1; i < Box.SQUARE_DIMENSION.escape() - 1 ; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + "    "
                        + Escapes.RESTORE_CURSOR_POSITION.escape());
                System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
            }
            System.out.print("   " + ColorCode.ANSI_BLACK.escape() + level + ColorCode.ANSI_RESET.escape());
        }
        else {
            for (int i = 0; i < Box.SQUARE_DIMENSION.escape() - 1 ; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + "    "
                        + Escapes.RESTORE_CURSOR_POSITION.escape());
                System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
            }
            System.out.print("   " + ColorCode.ANSI_BLACK.escape() + level + ColorCode.ANSI_RESET.escape());
        }
    }

}
