package it.polimi.ingsw.view.client.cli.graphicComponents;

public enum Box {
    VERTICAL_DIM (34),
    HORIZONTAL_DIM (95),
    TEXT_BOX_START (27),
    INPUT_BOX_START (32),
    TEXT_START (2),
    PLAYERS_BOX_START(80),
    BOARD_START_LEFT(20),
    BOARD_START_UP(3),
    SQUARE_DIMENSION(4),
    SQUARE_HORIZONTAL_DIM(8),
    ASCII_ART_START_LEFT(11),
    ASCII_ART_START_UP(11);

    private int escape;

    Box(int escape) {
        this.escape = escape;
    }

    public int escape(){
        return escape;
    }

}
