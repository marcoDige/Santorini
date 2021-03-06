package it.polimi.ingsw.view.client.cli;

import it.polimi.ingsw.model.enums.Color;
import it.polimi.ingsw.view.client.InputValidator;
import it.polimi.ingsw.view.client.View;
import it.polimi.ingsw.view.client.cli.graphicComponents.Box;
import it.polimi.ingsw.view.client.cli.graphicComponents.ColorCode;
import it.polimi.ingsw.view.client.cli.graphicComponents.Escapes;
import it.polimi.ingsw.view.client.cli.graphicComponents.Unicode;
import it.polimi.ingsw.view.client.viewComponents.Board;
import it.polimi.ingsw.view.client.viewComponents.God;
import it.polimi.ingsw.view.client.viewComponents.Player;
import it.polimi.ingsw.view.client.viewComponents.Square;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class contains all methods to visualize on the command line interface the game flow.
 * @author aledimaio AND marcoDige
 */

public class Cli extends View {

    ExecutorService inputExecutor;
    Future inputThread;
    private String state;
    private boolean disconnected;
    private God godToVisualize;

    public Cli() {
        super();
        disconnected = false;
        inputExecutor = Executors.newSingleThreadExecutor();
        cliSetup();
    }

    /**
     * This method setup the terminal for the cli
     */

    public void cliSetup() {
        state = "SETUP";
        System.out.print(Escapes.CLEAR_ENTIRE_SCREEN.escape());
        printStartTemplate();
        gameSetup();
    }

    /**
     * This method represents the game setup
     */

    public void gameSetup() {
        printInStartTextBox("Press enter button to start");
        input();

        //Connection setup
        setMyIp();
        setMyPort();

        //start connection
        start();
    }

    //View Override methods

    /**
     * This method allows to insert the server ip.
     */

    @Override
    public void setMyIp() {
        printInStartTextBox("Insert the server IP address!");
        String ip = input();
        while (!InputValidator.validateIP(ip)) {
            printInStartTextBox("Invalid IP address! Please, try again!");
            ip = input();
        }

        myIp = ip;
    }

    /**
     * This method allows to insert the server port.
     */

    @Override
    public void setMyPort() {
        printInStartTextBox("Insert the server port!");
        String port = input();
        while (!InputValidator.validatePORT(port)) {
            printInStartTextBox("Invalid port! Please, try again.");
            port = input();
        }

        myPort = Integer.parseInt(port);
    }

    /**
     * This method allows to insert the player's username
     * @param rejectedBefore is a boolean that indicates if the username has been rejected from the server and the player
     *                       has to insert it newly
     */

    @Override
    public void setUsername(boolean rejectedBefore) {
        disconnected = false;

        String output = "";
        if(rejectedBefore)
            output = "Username already used! ";

        output += "Insert your username (must be at least 3 characters long and no more than 10, valid characters: A-Z, a-z, 1-9, _)";
        printInStartTextBox(output);
        inputThread = inputExecutor.submit(() -> {
            String username = input();
            while (!InputValidator.validateUSERNAME(username) && !Thread.interrupted()) {
                printInStartTextBox("Invalid username! It must be at least 3 characters long and no more than 10, valid characters: A-Z, a-z, 1-9, _, try again!");
                username = input();
            }
            sendLoginRequest(username);
        });
    }

    /**
     * This method allows to start the match.
     */

    @Override
    public void startMatch() {
        if (players.size() == 1) {
            appendInStartTextBox("You are currently 2 players in the game, enter \"s\" to start the game now or \"w\" to wait for a third player!");
            inputThread = inputExecutor.submit(() -> {
                String input;
                do {
                    input = input();
                } while (!input.equals("s") && !input.equals("w") && !Thread.interrupted());

                if (input.equals("s")) sendStartGameRequest();
                if (input.equals("w")) printInStartTextBox("Wait for a third player...");
            });
        } else {
            appendInStartTextBox("You are currently 3 players in the game, press enter to start the game now! The match will automatically start in 2 minutes");
            inputThread = inputExecutor.submit(() ->{
                inputWithTimeoutStartMatch();
                if(!Thread.interrupted()) sendStartGameRequest();
            });
        }

    }

    /**
     * This method allows to select 2 or 3 gods (is reserved for the challenger).
     */

    @Override
    public void selectGods() {
        inputThread = inputExecutor.submit(() -> {
            ArrayList<Integer> godsId = new ArrayList<>();
            int i = 0;

            printInGameTextBox("You are the challenger! Now you have to chose " + (players.size() + 1) + " Gods for this match! Wait...");
            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                return;
            }
            printInGameTextBox("The list of Santorini Gods will be shown, write \"t\" to select the God shown, \"n\" to go to next God's card, \"p\" to go to previously God's card" +
                    ". Press enter to continue...");
            inputWithTimeout();

            if(!Thread.interrupted()) {
                printInGameTextBox(gods.get(0).getId() + " " + gods.get(0).getName());
                appendInGameTextBox(gods.get(0).getDescription());

                while (godsId.size() < (players.size() + 1)) {

                    switch (inputWithTimeout()) {

                        case "n":
                            if (i < gods.size() - 1) i++;
                            else i = 0;
                            printInGameTextBox(gods.get(i).getId() + " " + gods.get(i).getName());
                            appendInGameTextBox(gods.get(i).getDescription());
                            break;

                        case "p":
                            if (i > 0) i--;
                            else i = gods.size() - 1;
                            printInGameTextBox(gods.get(i).getId() + " " + gods.get(i).getName());
                            appendInGameTextBox(gods.get(i).getDescription());
                            break;

                        case "t":
                            if (!godsId.contains(gods.get(i).getId())) {
                                godsId.add(gods.get(i).getId());

                                if ((players.size() + 1 - godsId.size()) > 0) {
                                    printInGameTextBox("You have to choose " + (players.size() + 1 - godsId.size()) + " more gods. Press enter to continue...");
                                    inputWithTimeout();
                                    printInGameTextBox(gods.get(i).getId() + " " + gods.get(i).getName());
                                    appendInGameTextBox(gods.get(i).getDescription());
                                } else {
                                    printInGameTextBox("Loading...");
                                }
                            } else {
                                printInGameTextBox("This god has already been chosen! Press enter to continue...");
                                inputWithTimeout();
                                printInGameTextBox(gods.get(i).getId() + " " + gods.get(i).getName());
                                appendInGameTextBox(gods.get(i).getDescription());
                            }
                            break;

                        case "timeoutExpired":
                            clientHandler.disconnectionForTimeout();

                    }

                    if (Thread.interrupted()) return;
                }
            }

            if (!Thread.interrupted()) sendCreateGodsRequest(godsId);
        });
    }

    /**
     * This method allows to select a god from a list.
     * @param ids is the list of gods among which the player can choose his god
     */

    @Override
    public void selectGod(List<Integer> ids) {
        if(ids.size() == 1) {
            appendInGameTextBox("The last god left will be your god for this game.");
            sendChooseGodRequest(ids.get(0));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            inputThread = inputExecutor.submit(() -> {
                appendInGameTextBox("It's your turn to choose! To discover the gods you can choose press enter ...");
                inputWithTimeout();

                if(!Thread.interrupted()) {
                    printInGameTextBox("Write \"t\" to select the God shown, \"n\" to go to next God's card, \"p\" to go to previously God's card" +
                            ". Press enter to continue...");
                    inputWithTimeout();
                }

                int godId = 0, i = 0;

                if(!Thread.interrupted()) {
                    printInGameTextBox(getGodById(ids.get(0)).getId() + " " + getGodById(ids.get(0)).getName());
                    appendInGameTextBox(getGodById(ids.get(0)).getDescription());

                    while (godId == 0 && !Thread.interrupted()) {

                        switch (inputWithTimeout()) {

                            case "n":
                                if (i < ids.size() - 1) i++;
                                else i = 0;
                                printInGameTextBox(getGodById(ids.get(i)).getId() + " " + getGodById(ids.get(i)).getName());
                                appendInGameTextBox(getGodById(ids.get(i)).getDescription());
                                break;

                            case "p":
                                if (i > 0) i--;
                                else i = ids.size() - 1;
                                printInGameTextBox(getGodById(ids.get(i)).getId() + " " + getGodById(ids.get(i)).getName());
                                appendInGameTextBox(getGodById(ids.get(i)).getDescription());
                                break;

                            case "t":
                                godId = getGodById(ids.get(i)).getId();
                                printInGameTextBox("Loading...");
                                break;
                        }
                        if (Thread.interrupted()) return;
                    }
                }

                if (!Thread.interrupted()) sendChooseGodRequest(godId);
            });
        }
    }

    /**
     * This method allows to select the starting player.
     */

    @Override
    public void selectStartingPlayer() {
        inputThread = inputExecutor.submit(() -> {
            printInGameTextBox("As a challenger you can choose who will start the game (yourself too), write starter name...");
            String starter = inputWithTimeout();
            while(getPlayerByUsername(starter) == null && !starter.equals(myPlayer.getUsername()) && !Thread.interrupted()) {
                printInGameTextBox("Insert an existing username...");
                starter = inputWithTimeout();
            }

            if(!Thread.interrupted()){
                printInGameTextBox("Loading...");
                sendChooseStartingPlayerRequest(starter);
            }
        });
    }

    /**
     * This method allows to set player's worker on board.
     * @param gender is the gender of the worker to set
     * @param rejectedBefore is boolean that indicates if the coordinates have been rejected from the server and the
     *                       player has to insert them newly
     */

    @Override
    public void setWorkerOnBoard(String gender, boolean rejectedBefore) {
        inputThread = inputExecutor.submit(() -> {
            if(rejectedBefore) printInGameTextBox("The position is occupied! Insert enter the coordinates of a free place (type #,#)...");
            else printInGameTextBox("Enter the coordinates where you want to place your " + gender + " worker (type #,#)...");
            String input = inputWithTimeout();
            while(!InputValidator.validateCOORDINATES(input) && !Thread.interrupted()){
                printInGameTextBox("Invalid coordinates! Please try again (type #,#)...");
                input = inputWithTimeout();
            }

            if(!Thread.interrupted()){
                int[] coordinates = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
                sendSetWorkerOnBoardRequest(gender,coordinates[0],coordinates[1]);
            }
        });
    }

    /**
     * This method allows to start a turn.
     * @param firstOperation is the first operation player can do in his turn
     */

    @Override
    public void turn(String firstOperation) {
        printInGameTextBox("It’s time to play your turn! Wait...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        nextOperation(firstOperation);
    }

    /**
     * This method allows to move a worker from a position to another position.
     */

    @Override
    public void move() {
        abortInputProcessing();
        moveAfterChose();
    }

    /**
     * This method allows to build, with a worker, a building on a square.
     */

    @Override
    public void build() {
        abortInputProcessing();
        buildAfterChose();
    }

    /**
     * This method allows to give the player a choice between move and build.
     */

    @Override
    public void moveOrBuild() {
        abortInputProcessing();
        inputThread = inputExecutor.submit(() -> {
            String input;
            do {
                printInGameTextBox("You can both move and build, what do you want to do? (m,b) or type \"n\" if you want to change god to visualize in God Power box...");
                do {
                    input = inputWithTimeout();
                } while (!Thread.interrupted() && !input.equals("m") && !input.equals("b") && !input.equals("n"));

                if (!Thread.interrupted() && input.equals("n")) changeGodToVisualize();

            }while (!Thread.interrupted() && input.equals("n"));

            if (!Thread.interrupted()) {
                switch (input){
                    case "m" :
                        moveAfterChose();
                        break;
                    case "b" :
                        buildAfterChose();
                        break;
                }
            }
        });
    }

    /**
     * This method allows to give the player a choice between build and end.
     */

    @Override
    public void buildOrEnd() {
        abortInputProcessing();
        inputThread = inputExecutor.submit(() -> {
            String input;
            do {
                printInGameTextBox("You can both build and end your turn, what do you want to do? (b,e) or type \"n\" if you want to change god to visualize in God Power box...");
                do {
                    input = inputWithTimeout();
                } while (!Thread.interrupted() && !input.equals("b") && !input.equals("e") && !input.equals("n"));

                if(!Thread.interrupted() && input.equals("n")) changeGodToVisualize();

            }while(!Thread.interrupted() && input.equals("n"));

            if (!Thread.interrupted()) {
                switch (input){
                    case "b" :
                        buildAfterChose();
                        break;
                    case "e" :
                        sendEndOfTurnRequest();
                        break;
                }
            }
        });
    }

    /**
     * This method allows to show to the player that the login has been done.
     */

    @Override
    public void showLoginDone() {
        StringBuilder message;
        message = new StringBuilder("Hi " + myPlayer.getUsername() + ", you're in!");
        if (players.size() == 0) message.append(" You're the creator of this match, so you will decide "
                + "when to start the game. You can either start it when another player logs in or wait for a third player. "
                + "The moment the third player logs in you can start the game, which will still start automatically after 2 minutes "
                + "from the login of the third player.");
        else
            message.append(" You're currently ").append(players.size() + 1).append(" players in this game : You");
        for (Player player : players)
            message.append(", ").append(player.getUsername());
        printInStartTextBox(message.toString());
    }

    /**
     * This method allows to show to the player that a new user has been logged.
     * @param username is the new player's username
     * @param color is the new player's color
     */

    @Override
    public void showNewUserLogged(String username, Color color) {
        abortInputProcessing();
        printInStartTextBox(username + " is a new player!");
    }

    /**
     * This method allows to show to the player a wait message.
     * @param waitFor is the motivation of waiting
     * @param author is the player who is acting
     */

    @Override
    public void showWaitMessage(String waitFor, String author) {
        switch (waitFor) {
            case "startMatch":
                appendInStartTextBox("Waiting for " + author + "(creator)'s start game command...");
                break;
            case "createGods":
                printInGameTextBox(author + " is the challenger, he is choosing " + (players.size() + 1) + " divinities for this game...");
                break;
            case "choseGod":
                appendInGameTextBox(author + " is choosing a god to use for this game...");
                break;
            case "choseStartingPlayer":
                appendInGameTextBox(author + " is choosing a the starter player...");
                break;
            case "setupMaleWorkerOnBoard":
                printInGameTextBox(author + " is placing his male worker on the board...");
                break;
            case "setupFemaleWorkerOnBoard":
                printInGameTextBox(author + " is placing his female worker on the board...");
                break;
            case "hisTurn":
                abortInputProcessing();
                inputThread = inputExecutor.submit(() -> {
                    while(!Thread.interrupted()){
                        printInGameTextBox(author + " is playing his turn...");
                        appendInGameTextBox("Enter \"n\" if you want to change god to visualize in God Power box...");
                        String input = input();
                        if(input.equals("n")){
                            changeGodToVisualize();
                        }
                    }
                });
        }
    }

    /**
     * This method allows to show that the match has started
     */

    @Override
    public void showMatchStarted() {
        state = "MATCH";
        System.out.print(Escapes.CLEAR_ENTIRE_SCREEN.escape());
        printGameTemplate();
        printBoard();
        printInGameTextBox("the match has been started...");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show that the gods choice has been done
     * @param ids is the id list of the gods
     */

    @Override
    public void showGodsChoiceDone(ArrayList<Integer> ids) {
        StringBuilder output = new StringBuilder();
        output.append("You have chosen the following gods : ").append(getGodById(ids.get(0)).getName());
        for(int i = 1; i < ids.size(); i++){
            output.append(", ").append(getGodById(ids.get(i)).getName());
        }

        output.append(".");

        printInGameTextBox(output.toString());
    }

    /**
     * This method allows to show the gods challenger selected
     * @param username is the challenger's username
     * @param ids is the id list of the gods selected by the challenger
     */

    @Override
    public void showGodsChallengerSelected(String username, ArrayList<Integer> ids) {
        StringBuilder output = new StringBuilder();
        output.append(username).append(" has chosen the following gods : ").append(getGodById(ids.get(0)).getName());
        for(int i = 1; i < ids.size(); i++){
            output.append(", ").append(getGodById(ids.get(i)).getName());
        }

        output.append(".");

        printInGameTextBox(output.toString());
    }

    /**
     * This method allows to show the god selected by the player
     */

    @Override
    public void showMyGodSelected() {
        printInGameTextBox("Your God is " + myPlayer.getGod().getName() + ".");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        godToVisualize = myPlayer.getGod();
        printGodInGodBox();
    }

    /**
     * This method allows to show the God selected by another player
     * @param username is the player who selected the god shown
     */

    @Override
    public void showGodSelected(String username) {
        printInGameTextBox(username + " has chosen " + getPlayerByUsername(username).getGod().getName() + ".");
    }

    /**
     * This method allows to show the Starting player username
     * @param username is the starting player's username
     */

    @Override
    public void showStartingPlayer(String username) {
        if(username.equals(myPlayer.getUsername()))
            printInGameTextBox("You will be the starter player.");
        else
            printInGameTextBox(username + " will be the starter player.");

        appendInGameTextBox("Loading...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show the game board.
     */

    @Override
    public void showBoard() {
        printBoard();
    }

    /**
     * This method allows to show that the player identified by "username" has finished his turn.
     * @param username is the player who finished his turn
     */

    @Override
    public void showTurnEnded(String username) {
        printInGameTextBox(username + " turn is over!");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show that the turn of the player has finished.
     */

    @Override
    public void showMyTurnEnded(){
        printInGameTextBox("Your turn is over!");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show errors encountered during the player turn.
     * @param errors is the errors to show list
     */

    @Override
    public void showTurnErrors(List<String> errors) {
        StringBuilder output = new StringBuilder();

        output.append("Invalid action! errors encountered :");

        for (String error : errors) {
            switch (error) {
                case "BMU":
                    output.append(", ").append("Athena blocked upward movements");
                    break;
                case "CDU":
                    output.append(", ").append("you can't build a dome under yourself");
                    break;
                case "CMU":
                    output.append(", ").append("you can't move up because you build before you moved");
                    break;
                case "EBNP":
                    output.append(", ").append("the additional build can't be on a perimeter space");
                    break;
                case "EBNSS":
                    output.append(", ").append("the additional build can't be on the same space");
                    break;
                case "EBOSS":
                    output.append(", ").append("the additional build must be built on top of your first block");
                    break;
                case "EMNB":
                    output.append(", ").append("your worker can't moves back to the space it started on");
                    break;
                case "ILB":
                    output.append(", ").append("you can't build this block in the space you selected");
                    break;
                case "ILM":
                    output.append(", ").append("the space where you want to move is too high");
                    break;
                case "ID":
                    output.append(", ").append("there is a dome");
                    break;
                case "NA":
                    output.append(", ").append("the space you selected is not adjacent");
                    break;
                case "NF":
                    output.append(", ").append("the space you selected is occupied");
                    break;
                case "SDNF":
                    output.append(", ").append("you can't push the worker on this space because the space in the same direction is occupied");
                    break;
                case "EBND":
                    output.append(", ").append("the additional build block can't be a dome");
                    break;
                case "SMW":
                    output.append(", ").append("you cannot swap with your other worker");
            }
        }
        output.delete(36,37);

        output.append(". Try again...");
        printInGameTextBox(output.toString());

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show that the server being searched could not be found
     */

    @Override
    public void serverNotFound() {
        printInStartTextBox("Server not found!");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //New connection
        setMyIp();
        setMyPort();
        start();
    }

    /**
     * This method allows to show that another client disconnected.
     */

    @Override
    public void showAnotherClientDisconnection() {
        disconnected = true;
        abortInputProcessing();
        switch (state){
            case "SETUP" :
                printInStartTextBox("A client has disconnected from the game, the match has been deleted! Do you want to try to search a new game? (s/n)");
                break;
            case "MATCH":
                printInGameTextBox("A client has disconnected from the game, the match has been deleted! Do you want to try to search a new game? (s/n)");
                break;
            case "FINISHED":
                printInFinalTextBox("The server disconnected you! Do you want to try to reconnect? (s/n)");
                break;
        }
        String input;
        do {
            input = input();
        } while (!input.equals("s") && !input.equals("n"));

        if (input.equals("s")) {
            newGame();
            state = "SETUP";
            eraseThings("all");
            printStartTemplate();
            start();
        } else System.exit(0);
    }

    /**
     * This method allows to show to player that he has been disconnected from the server because the lobby where he was
     * is no longer available.
     */

    @Override
    public void showDisconnectionForLobbyNoLongerAvailable() {
        disconnected = true;
        abortInputProcessing();
        printInStartTextBox("Too long, the match you were entered into has already started! Do you want to try to search a new game? (s/n)");
        String input;
        do {
            input = input();
        } while (!input.equals("s") && !input.equals("n"));

        if (input.equals("s")) {
            newGame();
            state = "SETUP";
            eraseThings("all");
            printStartTemplate();
            start();
        } else System.exit(0);
    }

    /**
     * This method allows to show that the server disconnected.
     */

    @Override
    public void showServerDisconnection() {
        disconnected = true;
        abortInputProcessing();
        switch (state){
            case "SETUP" :
                printInStartTextBox("The server has disconnected! Do you want to try to reconnect? (s/n)");
                break;
            case "MATCH":
                printInGameTextBox("The server has disconnected! Do you want to try to reconnect? (s/n)");
                break;
            case "FINISHED":
                appendInFinalTextBox("The match ended and the server disconnected you! Do you want to search a new game? (s/n)");
                break;
        }
        String input;
        do {
            input = input();
        } while (!input.equals("s") && !input.equals("n"));

        if (input.equals("s")) {
            newGame();
            cliSetup();
        } else System.exit(0);
    }

    /**
     * This method allows to show to player that he has been disconnected from the server because the timeout to insert
     * an input expired.
     */

    @Override
    public void showDisconnectionForInputExpiredTimeout() {
        disconnected = true;
        abortInputProcessing();
        if (state.equals("SETUP")) printInStartTextBox("The timeout to do your action has expired, " +
                "you were kicked out of the game! Do you want to try to search a new game? (s/n)");
        else
            printInGameTextBox("The timeout to do your action has expired, " +
                    "you were kicked out of the game! Do you want to try to search a new game? (s/n)");
        String input;
        do {
            input = input();
        } while (!input.equals("s") && !input.equals("n"));

        if (input.equals("s")) {
            newGame();
            state = "SETUP";
            eraseThings("all");
            printStartTemplate();
            start();
        } else System.exit(0);
    }

    /**
     * This method allows to show to player that he has been disconnected from the server because the timeout to insert
     * an input expired.
     */

    @Override
    public void showPlayerLose(String username) {
        printInGameTextBox(username + " lost! now you are only 2 : You and " + players.get(0));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method allows to show to the player that he lost.
     * @param reason is the reason why the player lost
     * @param winner is the winner's username
     */

    @Override
    public void showYouLose(String reason, String winner) {
        state = "FINISHED";
        eraseThings("all");
        printFinalTemplate();
        printLoser();

        StringBuilder output = new StringBuilder();
        output.append("You lose because ");
        switch (reason){
            case "youLoseForDirectWin":
                output.append(winner).append(" won instantly!");
                break;
            case "youLoseForBlocked":
                if(myPlayer.getWorkers().size() == 0) output.append(" you no longer have workers.");
                else output.append("you were trapped!");
                if(!winner.equals(myPlayer.getUsername()))
                    output.append(" The winner is ").append(winner).append("!");
        }

        printInFinalTextBox(output.toString());
    }

    /**
     * This method allows to show to the player that he won.
     * @param reason is the reason why the player won
     */

    @Override
    public void showYouWin(String reason) {
        state = "FINISHED";
        eraseThings("all");
        printFinalTemplate();
        printWinner();

        StringBuilder output = new StringBuilder();
        output.append("You win ");
        switch (reason){
            case "youWinDirectly":
                output.append("instantly!");
                break;
            case "youWinForAnotherLose":
                if(players.get(0).getWorkers().size() == 0) output.append(" your opponent no longer have workers.");
                else output.append("your opponent were trapped!");
        }

        printInFinalTextBox(output.toString());
    }

    //Frame methods

    /**
     * The printTemplate method print the background visual elements, the frame of game
     */

    public void printStartTemplate() {

        //draw the top line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_RIGHT.escape());
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Login");
                i += 5;
            }
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_LEFT.escape());

        //draw the left line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_RAWINGS_HEAVY_VERTICAL_AND_RIGHT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_RIGHT.escape());

        //draw the bottom line

        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_LEFT.escape());

        //draw the right line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.HORIZONTAL_DIM.escape() - 1);
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL_AND_LEFT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }

        //draw the text line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Text");
                i += 4;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }

        //draw the input line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Input");
                i += 5;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }


        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.CREDITS_START_FROM_UP.escape(), Box.CREDITS_START_LEFT.escape());
        System.out.println(ColorCode.ANSI_CYAN.escape() + "          Software engineering project, AM10 group, credits to:");
        System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.CREDITS_START_LEFT.escape());
        System.out.print(ColorCode.ANSI_CYAN.escape() + "    Piersilvio De Bartolomeis, Marco Di Gennaro, Alessandro Di Maio" + ColorCode.ANSI_RESET.escape());

        printSantorini();
    }

    /**
     * This method prints the frame displayed during the end-game, after win or lose
     */

    public void printFinalTemplate(){

        //draw the top line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_RIGHT.escape());
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("End");
                i += 3;
            }
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_LEFT.escape());

        //draw the left line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_RAWINGS_HEAVY_VERTICAL_AND_RIGHT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_RIGHT.escape());

        //draw the bottom line

        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_LEFT.escape());

        //draw the right line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.HORIZONTAL_DIM.escape() - 1);
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL_AND_LEFT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }

        //draw the text line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Text");
                i += 4;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }

        //draw the input line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Input");
                i += 5;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
    }

    /**
     * This method prints the frame displayed during the game
     */

    public void printGameTemplate() {

        //draw the top line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_RIGHT.escape());
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Game Board");
                i += 10;
            }
            if(i == Box.GODS_BOX_START.escape() + 10){
                System.out.print("God Power");
                i += 9;
            }
            if(i == Box.GODS_BOX_START.escape() - 1){
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_HORIZONTAL.escape());
                i += 1;
            }
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_DOWN_AND_LEFT.escape());

        //draw the left line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_RAWINGS_HEAVY_VERTICAL_AND_RIGHT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_RIGHT.escape());

        //draw the bottom line

        for (int i = 1; i < Box.HORIZONTAL_DIM.escape() - 1; i++) {
            System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_LEFT.escape());

        //draw the right line

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        for (int i = 1; i < Box.VERTICAL_DIM.escape(); i++) {
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.HORIZONTAL_DIM.escape() - 1);
            if (i == Box.TEXT_BOX_START.escape() - 1 || i == Box.INPUT_BOX_START.escape() - 1)
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL_AND_LEFT.escape());
            else
                System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
        }

        //draw the text line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Text");
                i += 4;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }

        //draw the input line

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape(), 2);
        for (int i = 1; i < Box.HORIZONTAL_DIM.escape(); i++) {
            if (i == Box.TEXT_START.escape()) {
                System.out.print("Input");
                i += 5;
            } else
                System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        }

        //draw the gods box

        System.out.println(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.GODS_BOX_START_LINE.escape() + 1, Box.GODS_BOX_START.escape());
        for (int i = Box.GODS_BOX_START_LINE.escape() + 1; i < Box.PLAYER_BOX_START_LINE.escape(); i++) {
            System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.GODS_BOX_START.escape() - 1);
        }

        //draw player box

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.PLAYER_BOX_START_LINE.escape(), Box.PLAYERS_BOX_START.escape());
        System.out.print(Unicode.BOX_RAWINGS_HEAVY_VERTICAL_AND_RIGHT.escape());
        for (int i = Box.PLAYERS_BOX_START.escape(); i < (Box.HORIZONTAL_DIM.escape() - 1); i++)
            if (i == Box.HORIZONTAL_DIM.escape() - 25) {
                System.out.print("Players");
                i += 6;
            } else
                if(i == Box.HORIZONTAL_DIM.escape() - 10){
                    System.out.print("Gods");
                    i += 3;
                }else
                    System.out.print(Unicode.BOX_DRAWINGS_HEAVY_HORIZONTAL.escape());
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL_AND_LEFT.escape());

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.PLAYER_BOX_START_LINE.escape() + 1, Box.PLAYERS_BOX_START.escape());

        for (int i = (Box.PLAYER_BOX_START_LINE.escape() + 1); i < Box.TEXT_BOX_START.escape(); i++) {
            System.out.println(Unicode.BOX_DRAWINGS_HEAVY_VERTICAL.escape());
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.PLAYERS_BOX_START.escape() - 1);
        }
        System.out.print(Unicode.BOX_DRAWINGS_HEAVY_UP_AND_HORIZONTAL.escape());

        //print player name in players' box

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.PLAYER_BOX_START_LINE.escape() + 2, Box.PLAYERS_BOX_START.escape() + 4);
        System.out.println(Color.getColorCodeByColor(myPlayer.getWorkers().get(0).getColor()).escape() + ColorCode.ANSI_BLACK.escape() + " " + myPlayer.getUsername() + " " + ColorCode.ANSI_RESET.escape() + " : " + myPlayer.getGod().getName() + "\n");
        for (Player player : players) {
            System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.PLAYERS_BOX_START.escape() + 3);
            System.out.println(Color.getColorCodeByColor(player.getWorkers().get(0).getColor()).escape() + ColorCode.ANSI_BLACK.escape() + " " + player.getUsername() + " " + ColorCode.ANSI_RESET.escape() + " : " + player.getGod().getName() + "\n");
        }


    }

    /**
     * This method prints "Santorini" in the game frame
     */

    public void printSantorini() {

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.ASCII_ART_START_UP.escape(), Box.ASCII_ART_START_LEFT.escape() + 1);
        System.out.print(ColorCode.ANSI_CYAN.escape() +
                "  _____  ____  ____   ______   ___   ____   ____  ____   ____ \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " / ___/ /    ||    \\ |      | /   \\ |    \\ |    ||    \\ |    |\n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "(   \\_ |  o  ||  _  ||      ||     ||  D  ) |  | |  _  | |  | \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " \\__  ||     ||  |  ||_|  |_||  O  ||    /  |  | |  |  | |  | \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " /  \\ ||  _  ||  |  |  |  |  |     ||    \\  |  | |  |  | |  | \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " \\    ||  |  ||  |  |  |  |  |     ||  .  \\ |  | |  |  | |  | \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "  \\___||__|__||__|__|  |__|   \\___/ |__|\\_||____||__|__||____|\n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "                                                              \n" + ColorCode.ANSI_RESET.escape());

    }

    /**
     * This method prints "Loser" in the game frame
     */

    public void printLoser() {

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.ASCII_ART_START_UP.escape(), Box.ASCII_ART_START_LEFT.escape() + 8);
        System.out.print(ColorCode.ANSI_RED.escape() +
                " (        )   (         (     \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " )\\ )  ( /(   )\\ )      )\\ )  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "(()/(  )\\()) (()/( (   (()/(  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " /(_))((_)\\   /(_)))\\   /(_)) \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "(_))    ((_) (_)) ((_) (_))   \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "| |    / _ \\ / __|| __|| _ \\  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "| |__ | (_) |\\__ \\| _| |   /  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "|____| \\___/ |___/|___||_|_\\  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "                              \n" + ColorCode.ANSI_RESET.escape());

    }

    /**
     * This method prints "You win" in the game frame
     */

    public void printWinner() {

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.ASCII_ART_START_UP.escape(), Box.ASCII_ART_START_LEFT.escape() + 1);
        System.out.print(ColorCode.ANSI_CYAN.escape() +
                " __  __   ______   __  __       __     __   ______   __   __    \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "/\\ \\_\\ \\ /\\  __ \\ /\\ \\/\\ \\     /\\ \\  _ \\ \\ /\\  __ \\ /\\ \"-.\\ \\   \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "\\ \\____ \\\\ \\ \\/\\ \\\\ \\ \\_\\ \\    \\ \\ \\/ \".\\ \\\\ \\ \\/\\ \\\\ \\ \\-.  \\  \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                " \\/\\_____\\\\ \\_____\\\\ \\_____\\    \\ \\__/\".~\\_\\\\ \\_____\\\\ \\_\\\\\"\\_\\ \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "  \\/_____/ \\/_____/ \\/_____/     \\/_/   \\/_/ \\/_____/ \\/_/ \\/_/ \n" + "\u001b[" + Box.ASCII_ART_START_LEFT.escape() + "C" +
                "                                                                \n" + ColorCode.ANSI_RESET.escape());

    }

    /**
     * This method erase part of the general frame
     *
     * @param thing represents the part of frame that will be erased
     */

    public void eraseThings(String thing) {

        switch (thing) {

            case "all":
                System.out.print(Escapes.CLEAR_ENTIRE_SCREEN.escape());
                break;

            case "game":
                System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() - 1, Box.HORIZONTAL_DIM.escape() - 1);
                System.out.print(Escapes.CLEAR_SCREEN_FROM_HERE_TO_BEGINNING.escape());
                break;

            case "text":
                System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape(), 0);
                System.out.print(Escapes.CLEAR_SCREEN_FROM_HERE_TO_END.escape());
                break;

            case "playerBox":
                System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.PLAYERS_BOX_START.escape(), 0);
                for (int i = 0; i < (Box.TEXT_BOX_START.escape() - 1); i++) {
                    System.out.println(Escapes.CLEAR_LINE_FROM_CURSOR_TO_END.escape());
                    System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.PLAYERS_BOX_START.escape());
                }
                break;
            case "godBox":
                System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.GODS_BOX_START_LINE.escape(), Box.GODS_BOX_START.escape());
                for (int i = 0; i < (Box.PLAYER_BOX_START_LINE.escape() - 1); i++) {
                    System.out.println(Escapes.CLEAR_LINE_FROM_CURSOR_TO_END.escape());
                    System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.GODS_BOX_START.escape());
                }
        }


    }

    /**
     * This method prints the board frame
     */

    private void printBoardFrame(){

        //print Board frame

        System.out.print(ColorCode.WHITE.escape() + ColorCode.ANSI_BLACK.escape());

        //print left border and horizontal lines
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.BOARD_START_UP.escape(), Box.BOARD_START_LEFT.escape());
        System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + Unicode.BOX_DRAWINGS_LIGHT_DOWN_AND_RIGHT.escape());
        for (int i = 1; i < Box.SQUARE_VERTICAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); i++) {
            if (i % (Box.SQUARE_VERTICAL_DIM.escape() + 1) == 0) {
                System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + Unicode.BOX_RAWINGS_LIGHT_VERTICAL_AND_RIGHT.escape());
                for (int j = 1; j < Box.SQUARE_HORIZONTAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); j++) {
                    if (j % (Box.SQUARE_HORIZONTAL_DIM.escape() + 1) == 0)
                        System.out.print(Unicode.BOX_DRAWINGS_LIGHT_VERTICAL_AND_HORIZONTAL.escape());
                    else
                        System.out.print(Unicode.BOX_DRAWINGS_LIGHT_HORIZONTAL.escape());
                }
                System.out.print(Unicode.BOX_DRAWINGS_LIGHT_VERTICAL_AND_LEFT.escape());
            } else {
                System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + Unicode.BOX_DRAWINGS_LIGHT_VERTICAL.escape());
            }
        }

        //print bottom border
        System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
        System.out.print(Unicode.BOX_DRAWINGS_LIGHT_UP_AND_RIGHT.escape());
        for (int i = 1; i < Box.SQUARE_HORIZONTAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); i++) {
            if (i % (Box.SQUARE_HORIZONTAL_DIM.escape() + 1) == 0)
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
            else
                System.out.print(Unicode.BOX_DRAWINGS_LIGHT_HORIZONTAL.escape());
        }

        //print top border and vertical internal lines
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.BOARD_START_UP.escape(), Box.BOARD_START_LEFT.escape() + 1);
        for (int i = 1; i < Box.SQUARE_HORIZONTAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); i++) {
            if (i % (Box.SQUARE_HORIZONTAL_DIM.escape() + 1) == 0) {
                System.out.println(Unicode.BOX_DRAWINGS_LIGHT_DOWN_AND_HORIZONTAL.escape() + Escapes.SAVE_CURSOR_POSITION.escape());
                for (int j = 1; j < Box.SQUARE_VERTICAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); j++) {
                    System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.BOARD_START_LEFT.escape() + i - 1);
                    if (j % (Box.SQUARE_VERTICAL_DIM.escape() + 1) == 0)
                        System.out.println(Unicode.BOX_DRAWINGS_LIGHT_VERTICAL_AND_HORIZONTAL.escape());
                    else
                        System.out.println(Unicode.BOX_DRAWINGS_LIGHT_VERTICAL.escape());
                }
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.BOARD_START_LEFT.escape() + i - 1);
                System.out.print(Unicode.BOX_DRAWINGS_LIGHT_UP_AND_HORIZONTAL.escape() + Escapes.RESTORE_CURSOR_POSITION.escape());
            } else
                System.out.print(Unicode.BOX_DRAWINGS_LIGHT_HORIZONTAL.escape());
        }

        //print right border
        System.out.print(Escapes.SAVE_CURSOR_POSITION.escape() + Unicode.BOX_DRAWINGS_LIGHT_DOWN_AND_LEFT.escape());
        for (int i = 1; i < Box.SQUARE_VERTICAL_DIM.escape() * Box.SQUARE_DIMENSION.escape() + Box.SQUARE_DIMENSION.escape(); i++) {
            if (i % (Box.SQUARE_VERTICAL_DIM.escape() + 1) == 0)
                System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.SAVE_CURSOR_POSITION.escape(), 1);
            else
                System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.SAVE_CURSOR_POSITION.escape() + Unicode.BOX_DRAWINGS_LIGHT_VERTICAL.escape(), 1);
        }
        System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Unicode.BOX_DRAWINGS_LIGHT_UP_AND_LEFT.escape(), 1);

        System.out.print(ColorCode.ANSI_RESET.escape());

    }

    /**
     * This method prints the board reference next to the board frame and call the method "drawSquareV2" in order to
     * print each square
     */

    public void printBoard() {

        printBoardFrame();

        //print 1,2,3,4,5 vertical board reference
        for (int i = Board.DIMENSION, j = 0; i > 0; i--, j += Box.SQUARE_VERTICAL_DIM.escape() + 1) {
            System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.BOARD_START_UP.escape() + j + 2, Box.BOARD_START_LEFT.escape() - 1);
            System.out.printf("%d", i - 1);
        }

        //This cycle prints 0,0 position in the left-bottom corner

        for (int x = 0, i = 1, j = 0; x < Board.DIMENSION; x++, i += Box.SQUARE_HORIZONTAL_DIM.escape() + 1, j++) {
            System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.BOARD_START_UP.escape() + 1, Box.BOARD_START_LEFT.escape() + i);
            for (int y = Board.DIMENSION - 1; y > -1; y--) {
                //drawSquare(x, y);
                drawSquareV2(gameBoard.getSquareByCoordinates(x, y));
                System.out.println("\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.BOARD_START_LEFT.escape() + i - 1);
            }
            //print 1,2,3,4,5 horizontal board reference
            System.out.print(ColorCode.ANSI_RESET.escape());
            System.out.print("   " + j);
        }

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape() + 1, 2);
        System.out.print(">");

    }

    /**
     * This method sets the background color for terminal
     * @param level indicates the level of building, depending on it the background color will change
     * @return the escape code referring the background wanted
     */

    private String setBackgroundColorOfLevel(int level){

        if (level == 4)
            return ColorCode.LEVEL_DOME_BLUE_BACKGROUND.escape();
        else {
            switch (level) {
                case 0:
                    return ColorCode.LEVEL_0_GREEN_BACKGROUND.escape();
                case 1:
                    return ColorCode.LEVEL_1_SAND_BACKGROUND.escape();
                case 2:
                    return ColorCode.LEVEL_2_GRAY_BACKGROUND.escape();
                case 3:
                    return ColorCode.LEVEL_3_WHITE_BACKGROUND.escape();
            }
        }

        return null;
    }

    /**
     * This method prints a single square
     * @param square indicates the square that will be printed
     */

    private void drawSquareV2(Square square){

        int level = square.getLevel();
        int x = 10, y = 5;
        System.out.print(ColorCode.ANSI_BLACK.escape());

        if (0 <= level){
            for (int i = 0; i < y; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape());
                for (int j = 0; j < x; j++) {
                    if(j==x-1 && i==y-1)
                        System.out.print(level);
                    else
                        System.out.print(setBackgroundColorOfLevel(0) + " ");
                }
                if (i != y - 1) {
                    System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                }
            }
        }

        if(1 <= level){
            System.out.printf(Escapes.CURSOR_LEFT_INPUT_REQUIRED.escape() + Escapes.CURSOR_UP_INPUT_REQUIRED.escape(), 10, 4);
            for (int i = 0; i < y; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape());
                for (int j = 0; j < x; j++) {
                    if(j==x-1 && i==y-1)
                        System.out.print(level);
                    else
                        System.out.print(setBackgroundColorOfLevel(1) + " ");
                }
                if (i != y - 1) {
                    System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                }
            }
        }

        if(2 <= level){
            System.out.printf(Escapes.CURSOR_LEFT_INPUT_REQUIRED.escape() + Escapes.CURSOR_UP_INPUT_REQUIRED.escape(), 10, 4);
            for (int i = 0; i < y; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape());
                for (int j = 0; j < x; j++) {
                    if(j==x-1 && i==y-1)
                        System.out.print(level);
                    else
                        System.out.print(setBackgroundColorOfLevel(2) + " ");
                }
                if (i != y - 1) {
                    System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                }
            }
        }

        if(3 <= level){
            System.out.printf(Escapes.CURSOR_LEFT_INPUT_REQUIRED.escape() + Escapes.CURSOR_UP_INPUT_REQUIRED.escape(), 10, 4);
            x = 6;
            y = 3;
            System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1, 2);
            for (int i = 0; i < y; i++) {
                System.out.print(Escapes.SAVE_CURSOR_POSITION.escape());
                for (int j = 0; j < x; j++) {
                    System.out.print(setBackgroundColorOfLevel(3) + " ");
                }
                if (i != y - 1) {
                    System.out.printf(Escapes.RESTORE_CURSOR_POSITION.escape() + Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape(), 1);
                }
            }
            System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1, 2);
        }

        if(square.getDome() || square.getWorker() != null){
            System.out.printf(Escapes.CURSOR_LEFT_INPUT_REQUIRED.escape() + Escapes.CURSOR_UP_INPUT_REQUIRED.escape(), 10, 4);
            x = 2;
            y = 1;
            System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 2, 4);
            for (int i = 0; i < y; i++) {
                if(square.getWorker() != null){
                    if(square.getWorker().getGender().equals("female"))
                        System.out.print(Color.getColorCodeByColor(square.getWorker().getColor()).escape() + Unicode.WORKER_FEMALE_ICON.escape() + " " );
                    else
                        System.out.print(Color.getColorCodeByColor(square.getWorker().getColor()).escape() + Unicode.WORKER_MALE_ICON.escape() + " " );
                }
                else {
                    for (int j = 0; j < x; j++) {
                        System.out.print(setBackgroundColorOfLevel(4) + " ");
                    }
                }
            }
            System.out.printf(Escapes.CURSOR_DOWN_INPUT_REQUIRED.escape() + Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 2, 4);
            }

            System.out.print(ColorCode.ANSI_RESET.escape());

    }

    /**
     * This method is used to get input from keyboard without any control
     *
     * @return the string from input
     */

    public String input() {

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape() + 1, 2);
        System.out.print(">");
        return InputCli.readLine();

    }

    /**
     * This method is used to get a string as input from the terminal with a maximum input time allowed during the main
     * phase of the game
     * @return the string input from terminal as String
     */

    public String inputWithTimeout() {
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape() + 1, 2);
        System.out.print(">");
        String input = "";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> result = executor.submit(InputCli::readLine);

        try {
            input = result.get(2, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            if(!disconnected){
                new Thread(this::disconnectionForInputExpiredTimeout).start();
            }
            Thread.currentThread().interrupt();
            result.cancel(true);
        }

        return input;
    }

    /**
     * This method is used to get a string as input from the terminal with a maximum input time allowed during the initial phase
     * of the game
     */

    public void inputWithTimeoutStartMatch(){
        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.INPUT_BOX_START.escape() + 1, 2);
        System.out.print(">");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> result = executor.submit(InputCli::readLine);

        try {
            result.get(2, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            sendStartGameRequest();
        }

    }

    /**
     * This method prints text in the part of the general frame dedicated to text
     * @param text indicates the text that will be printed
     */

    public void printInStartTextBox(String text) {

        char[] information = text.toCharArray();

        eraseThings("text");
        printStartTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }

    }

    /**
     * This method prints god's information in the part of the general frame dedicated to gods
     */

    public void printGodInGodBox(){
        eraseThings("godBox");

        printInGodTextBox("name",godToVisualize.getName());
        printInGodTextBox("description",godToVisualize.getDescription());
    }

    /**
     * This method prints text in the text part of the frame in the endgame
     */

    public void printInFinalTextBox(String text) {

        char[] information = text.toCharArray();

        eraseThings("text");
        printFinalTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }

    }

    /**
     * This method is used to change god visualized in the god box (not for print it in the box)
     */

    public void changeGodToVisualize(){
        if(godToVisualize.getName().equals(myPlayer.getGod().getName())){
            godToVisualize = players.get(0).getGod();
        }else {
            if (players.size() == 1)
                godToVisualize = myPlayer.getGod();
            else {
                if (godToVisualize.getName().equals(players.get(0).getGod().getName()))
                    godToVisualize = players.get(1).getGod();
                else
                    godToVisualize = myPlayer.getGod();
            }
        }

        printGodInGodBox();
    }

    /**
     * This method prints in God text box
     * @param name represents what is intended to print, of god or its descriptions
     * @param text represents the content to be displayed
     */

    public void printInGodTextBox(String name, String text){

        int size = text.length();
        char[] information = text.toCharArray();

        printGameTemplate();

        System.out.print(Escapes.CURSOR_HOME_0x0.escape());

        if(name.equals("name")){
            size = ((Box.HORIZONTAL_DIM.escape() - Box.GODS_BOX_START.escape()) - size)/2;
            System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.GODS_BOX_START_LINE.escape() + 3, Box.GODS_BOX_START.escape() + size + 1);
            System.out.print(text);
        }
        else{
            System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.GODS_BOX_START_LINE.escape() + 7, Box.GODS_BOX_START.escape() + 2);
            for (int i = Box.GODS_BOX_START.escape() + 2, j = 0; j < information.length; i++, j++) {
                System.out.print(information[j]);
                if (i == Box.HORIZONTAL_DIM.escape() - 3) {
                    System.out.print("-\n");
                    System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), Box.GODS_BOX_START.escape() + 1);
                    i = Box.GODS_BOX_START.escape() + 1;
                }
            }
        }

    }

    /**
     * This method is used to avoid that printed text exceed the length of the text box
     * @param text is the text that will be printed
     */

    public void appendInStartTextBox(String text) {
        char[] information = text.toCharArray();

        printStartTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        System.out.print("\n");
        System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }
    }

    /**
     * This method prints text in text frame
     *
     * @param text represents the text that will be printed
     */

    public void printInGameTextBox(String text) {

        char[] information = text.toCharArray();

        eraseThings("text");
        printGameTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }
    }

    /**
     * This method is used to avoid that printed text exceed the length of the god text box
     * @param text is the text that will be printed
     */

    public void appendInGameTextBox(String text) {

        char[] information = text.toCharArray();

        printGameTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        System.out.print("\n");
        System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }
    }

    /**
     * This method is used to avoid that printed text exceed the length of the final text box
     * @param text is the text that will be printed
     */

    public void appendInFinalTextBox(String text) {

        char[] information = text.toCharArray();

        printFinalTemplate();

        System.out.printf(Escapes.MOVE_CURSOR_INPUT_REQUIRED.escape(), Box.TEXT_BOX_START.escape() + 1, 2);

        //this cycle allow to avoid that text exceed the frame length

        System.out.print("\n");
        System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);

        for (int i = 2, j = 0; j < information.length; i++, j++) {
            System.out.print(information[j]);
            if (i == Box.HORIZONTAL_DIM.escape() - 2) {
                System.out.print("-\n");
                System.out.printf(Escapes.CURSOR_RIGHT_INPUT_REQUIRED.escape(), 1);
                i = 1;
            }
        }
    }

    //support methods

    /**
     * This method is used to handle the move action after you select a worker
     */

    public void moveAfterChose(){
        printInGameTextBox("You have to move!");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!Thread.interrupted())
            inputThread = inputExecutor.submit(() -> {
            int[] startCoordinates = selectWorker();

            if(startCoordinates != null) {

                String input;
                int[] coordinates;

                do {
                    printInGameTextBox("Where do you want to move? (#,#) or type \"n\" if you want to change god to visualize in God Power box...");
                    input = inputWithTimeout();
                    while (!InputValidator.validateCOORDINATES(input) && !input.equals("n") && !Thread.interrupted()) {
                        printInGameTextBox("Invalid coordinates or input! Please try again (type #,#) or \"n\"...");
                        input = inputWithTimeout();
                    }

                    if (input.equals("n") && !Thread.interrupted()) changeGodToVisualize();
                } while (!Thread.interrupted() && input.equals("n"));


                if (InputValidator.validateCOORDINATES(input)) {
                    coordinates = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
                    sendMoveRequest(gameBoard.getSquareByCoordinates(startCoordinates[0], startCoordinates[1]).getWorker().getGender(), coordinates[0], coordinates[1]);
                }
            }
            });
    }

    /**
     * This method is used to handle the build action after you select a worker
     */

    public void buildAfterChose(){
        printInGameTextBox("You have to build!");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!Thread.interrupted())
            inputThread = inputExecutor.submit(() -> {
            int[] startCoordinates = selectWorker();

            if(startCoordinates != null) {

                String inputCoordinates;
                int[] coordinates;

                do {
                    printInGameTextBox("Where do you want to build? (#,#) or type \"n\" if you want to change god to visualize in God Power box...");
                    inputCoordinates = inputWithTimeout();
                    while (!InputValidator.validateCOORDINATES(inputCoordinates) && !inputCoordinates.equals("n") && !Thread.interrupted()) {
                        printInGameTextBox("Invalid coordinates or input! Please try again (type #,#) or \"n\"...");
                        inputCoordinates = inputWithTimeout();
                    }

                    if (inputCoordinates.equals("n") && !Thread.interrupted()) changeGodToVisualize();
                } while (!Thread.interrupted() && inputCoordinates.equals("n"));

                int level;
                String inputLevel = "";
                if(InputValidator.validateCOORDINATES(inputCoordinates)) {
                    printInGameTextBox("What type of building do you want to build? (1 -> first block, 2 -> second block, 3 -> third block, 4 -> dome)");
                    inputLevel = inputWithTimeout();
                    while (!InputValidator.validateLEVEL(inputLevel) && !Thread.interrupted()) {
                        printInGameTextBox("Invalid building! Please try again (#)...");
                        inputLevel = inputWithTimeout();
                    }
                }

                if (!Thread.currentThread().isInterrupted()) {
                    coordinates = Arrays.stream(inputCoordinates.split(",")).mapToInt(Integer::parseInt).toArray();
                    level = Integer.parseInt(inputLevel);
                    sendBuildRequest(gameBoard.getSquareByCoordinates(startCoordinates[0], startCoordinates[1]).getWorker().getGender(), coordinates[0], coordinates[1], level);
                }
            }
            });
    }

    /**
     * This method is used to select the worker who will do an action
     * @return the position x,y of the worker selected
     */

    private int[] selectWorker() {

        int[] coordinates = new int[2];

        if(workerForThisTurnCoordinates[0] == -1 && workerForThisTurnCoordinates[1] == -1) {
            String input;

            if(myPlayer.getWorkers().size() == 1){
                Square s = myPlayer.getWorkers().get(0).getCurrentPosition();
                coordinates[0] = s.getX();
                coordinates[1] = s.getY();
                appendInGameTextBox("You have only one worker in game, you will play your turn with him!");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                do {
                    printInGameTextBox("Select one of your workers: (m,f) or type \"n\" if you want to change god to visualize in God Power box...");
                    input = inputWithTimeout();
                    while (!input.equals("m") && !input.equals("f") && !input.equals("n") && !Thread.currentThread().isInterrupted()) {
                        printInGameTextBox("Invalid input! Please try again (m,f) or \"n\"...");
                        input = inputWithTimeout();
                    }
                    if(input.equals("n") && !Thread.currentThread().isInterrupted()) changeGodToVisualize();
                }while (!Thread.currentThread().isInterrupted() && input.equals("n"));

                if (input.equals("m") && !Thread.currentThread().isInterrupted()) {
                    Square s = myPlayer.getWorkerByGender("male").getCurrentPosition();
                    coordinates[0] = s.getX();
                    coordinates[1] = s.getY();
                }
                if (input.equals("f") && !Thread.currentThread().isInterrupted()) {
                    Square s = myPlayer.getWorkerByGender("female").getCurrentPosition();
                    coordinates[0] = s.getX();
                    coordinates[1] = s.getY();
                }
            }
        }else{
            coordinates = workerForThisTurnCoordinates;
        }

        if(Thread.currentThread().isInterrupted()) return null;

        return coordinates;
    }

    /**
     * This method is used to interrupt input process
     */

    private void abortInputProcessing() {
        if (inputThread != null && !inputThread.isCancelled()) {
            inputThread.cancel(true);
            inputThread = null;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}