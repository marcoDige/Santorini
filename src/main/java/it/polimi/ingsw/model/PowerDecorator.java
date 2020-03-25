package it.polimi.ingsw.model;



public abstract class PowerDecorator implements Power {
    protected Power decoratedPower;

    public PowerDecorator(Power p){
        super();
        this.decoratedPower = p;
    }

    @Override
    public boolean checkMove(Worker w, Square s) {
        return false;
    }

    @Override
    public boolean checkBuild(Worker w, Square s) {
        return false;
    }

    @Override
    public boolean checkWin() {
        return false;
    }

    @Override
    public void updateMove(Worker w, Square s) {

    }

    @Override
    public void updateBuild(Worker w, Square s) {

    }

    @Override
    public Integer getMaxMoves() {
        return null;
    }

    @Override
    public Integer getMaxBuilds() {
        return null;
    }

    @Override
    public Board getBoard() {
        return null;
    }
}
