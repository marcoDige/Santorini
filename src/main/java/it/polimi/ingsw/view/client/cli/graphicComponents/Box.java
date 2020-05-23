package it.polimi.ingsw.view.client.cli.graphicComponents;

public enum Box {
    VERTICAL_DIM (37),
    HORIZONTAL_DIM (95),
    TEXT_BOX_START (29),
    INPUT_BOX_START (34),
    TEXT_START (2),
    PLAYERS_BOX_START(67),
    PLAYER_BOX_START_LINE(19),
    GODS_BOX_START(67),
    GODS_BOX_START_LINE(1),
    BOARD_START_LEFT(14),
    BOARD_START_UP(4),
    SQUARE_DIMENSION(4),
    SQUARE_HORIZONTAL_DIM(8),
    ASCII_ART_START_LEFT(16),
    ASCII_ART_START_UP(11),
    CREDITS_START_LEFT(10),
    CREDITS_START_FROM_UP(25);

    private int escape;

    Box(int escape) {
        this.escape = escape;
    }

    public int escape(){
        return escape;
    }

}
