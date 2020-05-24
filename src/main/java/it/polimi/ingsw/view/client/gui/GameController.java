package it.polimi.ingsw.view.client.gui;

import it.polimi.ingsw.model.enums.Color;
import it.polimi.ingsw.view.client.viewComponents.Board;
import it.polimi.ingsw.view.client.viewComponents.God;
import it.polimi.ingsw.view.client.viewComponents.Player;
import it.polimi.ingsw.view.client.viewComponents.Square;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class implements the GameScene Controller
 * @author pierobartolo & aledimaio
 */

public class GameController {

    @FXML
    public ImageView imageToDrag;
    @FXML
    public Button showInformationButton;
    @FXML
    public Label playerName;
    @FXML
    public Button nextButton;
    @FXML
    public Label numberOfPlayer;
    @FXML
    public Button prevButton;
    @FXML
    public Label informationBox;
    @FXML
    public ImageView blueButton;
    @FXML
    public ImageView redButton;
    @FXML
    public Label godDescription;
    @FXML
    public Label godName;
    @FXML
    public ImageView godImage;
    @FXML
    public ImageView worker;
    @FXML
    public GridPane boardGridPane;
    @FXML
    public ImageView endTurnButton;
    @FXML
    public ImageView firstLevelImageView;
    @FXML
    public ImageView secondLevelImageView;
    @FXML
    public ImageView thirdLevelImageView;
    @FXML
    public ImageView domeImageView;

    private boolean dNdActiveMove = false;
    private boolean dNdActiveBuild = false;


    String workerGender;

    private Gui gui;

    ImageView source;
    ImageView source_pointer;

    ImageView destination;
    ImageView destination_pointer;

    /**
     * In-Game Players
     */

    private ArrayList<Player> players;

    /**
     * Current player in the player's information box
     */

    private int currentPlayerId = 0;

    /**
     * When it is true the gui shows the player's god card
     */

    boolean showGod = false;

    /**
     * Current state of the match.
     * (worker-> place workers)
     * (move)
     * (build)
     */

    String state = "worker";


    @FXML
    public void testMethod(MouseEvent mouseEvent) {

    }

    public void changeImageViewRedButton(MouseEvent mouseEvent) {
        Image updateButton = GuiManager.loadImage("Buttons/btn_red_pressed.png");
        redButton.setImage(updateButton);
        redButton.setDisable(true);
    }

    /**
     * build button
     * @param mouseEvent
     */
    public void doActionRedButton(MouseEvent mouseEvent) {
        dNdActiveBuild = true;
        state ="build";
    }

    public void changeImageViewBlueButton(MouseEvent mouseEvent) {
        Image updateButton = GuiManager.loadImage("Buttons/btn_blue_pressed.png");
        blueButton.setImage(updateButton);
        blueButton.setDisable(true);
    }

    public void doActionBlueButton(MouseEvent mouseEvent) {
        dNdActiveMove = true;
        state = "move";
    }

    public void restoreImage(){

        source_pointer.setImage(source.getImage());
        destination_pointer.setImage(destination.getImage());

    }

    public void dragDoneMethod(DragEvent dragEvent) {

        if(dragEvent.getTransferMode() == TransferMode.MOVE){
            if(!((ImageView) dragEvent.getSource()).getId().equals("worker")){}
            else
                ((ImageView)dragEvent.getSource()).setImage(null);
        }

    }

    /**
     * On Drag Done Method
     * @param dragEvent
     */

    public void removeWorkerDragged(DragEvent dragEvent) {

        if(dragEvent.getTransferMode() == TransferMode.MOVE){
            if(!((ImageView) dragEvent.getSource()).getId().equals("worker")){}
            else
                ((ImageView)dragEvent.getSource()).setImage(null);
        }


    }

    /**
     * On Drag Dropped method
     * @param dragEvent
     */

    public void acceptElement(DragEvent dragEvent) {

        switch(state){
            case "worker":
                gui.sendSetWorkerOnBoardRequest(workerGender,boardGridPane.getRowIndex( ((ImageView) dragEvent.getSource()).getParent()),boardGridPane.getColumnIndex( ((ImageView) dragEvent.getSource()).getParent()));
                break;
            case "move":
                gui.setSelectedWorker(boardGridPane.getRowIndex( source_pointer.getParent()), boardGridPane.getColumnIndex( source_pointer.getParent()));
                gui.sendMoveRequest(gui.getWorkerGender(boardGridPane.getRowIndex(source_pointer.getParent()),boardGridPane.getColumnIndex( source_pointer.getParent())),boardGridPane.getRowIndex( ((ImageView) dragEvent.getSource()).getParent()), boardGridPane.getColumnIndex( ((ImageView) dragEvent.getSource()).getParent()));
                dNdActiveMove = false;
            case "build":

                int level;
                switch (source_pointer.getId()){
                    case "firstLevelImageView":
                        level = 1;
                        break;
                    case "secondLevelImageView":
                        level = 2;
                        break;
                    case "thirdLevelImageView":
                        level = 3;
                        break;
                    case "domeImageView":
                        level = 4;
                        break;
                }

                dNdActiveBuild = false;
                break;
        }


        Dragboard db = dragEvent.getDragboard();
        boolean success = false;

        ImageView test = ((ImageView)dragEvent.getSource());

        destination = new ImageView(((ImageView)dragEvent.getSource()).getImage());
        destination_pointer = test;
        if(db.hasImage()){
            test.setImage(db.getImage());
            success = true;
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();


        //TODO if the answer from server is negative restoreImage();

    }

    /**
     * On Drag Over method
     */

    public void highlightSquareOverMethod(DragEvent dragEvent) throws IOException {

        /* ((ImageView)dragEvent.getGestureSource()) != source_pointer should check that the place for drop is not the same of source of drag
        if(((ImageView)dragEvent.getGestureSource()) != source_pointer && dragEvent.getDragboard().hasImage())
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        dragEvent.consume();
         */

        if(state.equals("worker") || dNdActiveMove || (dNdActiveBuild && ((ImageView) dragEvent.getSource()).getId().equals("workerImageView"))) {
            if ((dragEvent.getGestureSource() != dragEvent.getSource()) && dragEvent.getDragboard().hasImage())
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            dragEvent.consume();
        }

    }

    public void startChangingPosition(MouseEvent mouseEvent) {

        boolean testing = false;
        int i[];
        if(state.equals("move")) {
            i = gui.getMyWorkerPosition(gui.getWorkerGender(boardGridPane.getRowIndex(((ImageView) mouseEvent.getSource()).getParent()), boardGridPane.getColumnIndex(((ImageView) mouseEvent.getSource()).getParent())));
            testing = (dNdActiveMove && (((ImageView) mouseEvent.getSource()).getId().equals("workerImageView")) && (i[0] == boardGridPane.getRowIndex(((ImageView) mouseEvent.getSource()).getParent()) && (i[1] == boardGridPane.getColumnIndex(((ImageView) mouseEvent.getSource()).getParent()))));
        }
        if((state.equals("worker") && ((ImageView) mouseEvent.getSource()).getId().equals("worker")) || (testing) || (dNdActiveBuild && !(((ImageView) mouseEvent.getSource()).getId().equals("workerImageView")))) {
            ImageView test = ((ImageView) mouseEvent.getSource());


            source = new ImageView(((ImageView) mouseEvent.getSource()).getImage());
            source_pointer = test;

            Dragboard db = test.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putImage(test.getImage());
            db.setContent(content);
            mouseEvent.consume();
        }

    }

    public void highlightSquare(DragEvent dragEvent) { }

    public void notHihglightSquare(DragEvent dragEvent) { }

    /**
     * When showInformationButton is clicked, informations about selected player is displayed
     * @param actionEvent
     */

    public void showOtherPlayerInformation(ActionEvent actionEvent) {
        if(!showGod){
            God tempGod = gui.getPlayerGod(players.get(currentPlayerId));
            godName.setText(tempGod.getName());
            godDescription.setText(tempGod.getDescription());
            godImage.setImage(GuiManager.loadGod(tempGod.getId()));
            godName.setVisible(true);
            godImage.setVisible(true);
            godDescription.setVisible(true);
            showInformationButton.setText("Hide Informations");
            showGod = true;
        }

        else{
            showInformationButton.setText("Show Informations");
            hideGod();
            showGod = false;
        }


    }

    /**
     * This method is called when the next player button is pressed, it updates the gui
     * @param actionEvent none
     */

    public void nextPlayer(ActionEvent actionEvent) {
        if(currentPlayerId >= players.size()-1 )
            currentPlayerId = 0;
        else
            currentPlayerId++;

        God tempGod = gui.getPlayerGod(players.get(currentPlayerId));
        godName.setText(tempGod.getName());
        godDescription.setText(tempGod.getDescription());
        godImage.setImage(GuiManager.loadGod(tempGod.getId()));

        numberOfPlayer.setText(currentPlayerId+1 + " of " + players.size());
        playerName.setText(players.get(currentPlayerId).getUsername());
    }

    /**
     * This method is called when the prev player button is pressed, it updates the gui
     * @param actionEvent none
     */

    public void showPrevPlayer(ActionEvent actionEvent) {
        if(currentPlayerId == 0)
            currentPlayerId = players.size()-1;
        else
            currentPlayerId--;


        God tempGod = gui.getPlayerGod(players.get(currentPlayerId));
        godName.setText(tempGod.getName());
        godDescription.setText(tempGod.getDescription());
        godImage.setImage(GuiManager.loadGod(tempGod.getId()));

        numberOfPlayer.setText(currentPlayerId+1 + " of " + players.size());
        playerName.setText(players.get(currentPlayerId).getUsername());
    }

    public void setPlayers(ArrayList<Player> players){
        this.players = players;
        numberOfPlayer.setText(currentPlayerId+1 + " of " + players.size());
        playerName.setText(players.get(currentPlayerId).getUsername());
    }

    public void setupWorker(String gender){
        worker.setImage(getWorkerImage(gui.getMyColor(),gender));
        workerGender = gender;
        showInformationButton.setText("Show Informations");
    }

    public void setInstructionLabel(String text){
        informationBox.setText(text);
    }

    public void hideGod(){
        godDescription.setVisible(false);
        godImage.setVisible(false);
        godName.setVisible(false);
    }

    public void updateBoard(Board board_view){


        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                updateSquare(board_view.getSquareByCoordinates(x,y));
            }
        }

    }

    public void updateSquare(Square square){

        int x = square.getX();
        int y = square.getY();
        Image imageTemp;
        AnchorPane anchorPane = (AnchorPane) getNodeFromGridPane(boardGridPane, y, x);
        //Node node = getNodeFromGridPane(board, x, y);
        //Node node = board.getChildren().get(y*5+x);
        //AnchorPane anchorPane = (AnchorPane) boardGridPane.getChildren().get((y*5+x));

        if (anchorPane != null){

            if(square.getWorker() != null){
                Image workerImage = getWorkerImage( square.getWorker().getColor(), square.getWorker().getGender());
                ((ImageView) anchorPane.getChildren().get(0)).setImage(workerImage);
            }
            else {
                ((ImageView) anchorPane.getChildren().get(0)).setImage(null);
            }

            if(square.getDome()){
                switch (square.getLevel()){

                    case 0:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/dome.png");
                        //TODO set buildingImage to background or to another ImageView in the same anchorPane
                        break;
                    case 1:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/first_level_dome.png");
                        break;
                    case 2:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/second_level_dome.png");
                        break;
                    case 3:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/complete_tower.png");
                        break;

                }

            } else {
                switch (square.getLevel()) {

                    case 0:
                        //TODO remove background or the other ImageView
                        break;
                    case 1:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/first_level.png");
                        break;
                    case 2:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/second_level.png");
                        break;
                    case 3:
                        imageTemp = GuiManager.loadImage("Buildings_+_pawns/third_level.png");
                        break;

                }
            }



        }

    }

    public Node getNodeFromGridPane(GridPane gridPane, int col, int row) {

        for (Node node : gridPane.getChildren()) {
            if (gridPane.getColumnIndex(node) == col &&  gridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    /*
    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }
    */


    public void setGui(Gui gui){
        this.gui = gui;
    }


    public void hideBlueButton(){
        blueButton.setVisible(false);
        blueButton.setDisable(true);
    }

    public void showBlueButton(){
        blueButton.setVisible(true);
        blueButton.setDisable(false);
    }
    public void showRedButton(){
        redButton.setVisible(true);
        redButton.setDisable(false);
    }

    public void hideRedButton(){
        redButton.setVisible(false);
        redButton.setDisable(true);
    }

    private Image getWorkerImage(Color color, String gender ){
        // default
        Image workerImage = GuiManager.loadImage("Buildings_+_pawns/"+gender+"_azure_worker.png");

        switch(color){
            case AZURE:
                workerImage = GuiManager.loadImage("Buildings_+_pawns/"+gender+"_azure_worker.png");
                break;
            case ORANGE:
                workerImage = GuiManager.loadImage("Buildings_+_pawns/"+gender+"_white_worker.png");
                break;
            case GREY:
                workerImage = GuiManager.loadImage("Buildings_+_pawns/"+gender+"_gray_worker.png");
                break;

        }
        return workerImage;


    }

    /**
     * This method is triggered when a square is clicked
     */

    @FXML
    public void buildAction(MouseEvent mouseEvent) {

    }

    //TODO dndACTIVEMOVE only when you press move
    //TODO dndATIVEMOVE false after move request (move va male? build before move?)
    //TODO dndACTIVEBUILD true when you press build
    //TODO  dndACTIVEBUILD false when you send request

    //TODO build va male -> bottone build up e blocco trascinato back
    //TODO move va male-> ok
    //

}
