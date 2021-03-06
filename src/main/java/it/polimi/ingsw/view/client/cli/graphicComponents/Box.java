package it.polimi.ingsw.view.client.cli.graphicComponents;

/**
 * This class represents dimensions of various component in the cli
 * @author aledimaio
 */

public enum Box {
    VERTICAL_DIM (42),
    HORIZONTAL_DIM (95),
    TEXT_BOX_START (36),
    INPUT_BOX_START (41),
    TEXT_START (2),
    PLAYERS_BOX_START(67),
    PLAYER_BOX_START_LINE(19),
    GODS_BOX_START(67),
    GODS_BOX_START_LINE(1),
    BOARD_START_LEFT(5),
    BOARD_START_UP(3),
    SQUARE_DIMENSION(5),
    SQUARE_HORIZONTAL_DIM(10),
    SQUARE_VERTICAL_DIM(5),
    ASCII_ART_START_LEFT(16),
    ASCII_ART_START_UP(11),
    CREDITS_START_LEFT(10),
    CREDITS_START_FROM_UP(25);

    private int escape;

    Box(int escape) {
        this.escape = escape;
    }

    /**
     * This method allows to get directly the int value desired
     * @return the desired value as a Int
     */

    public int escape(){
        return escape;
    }

}
