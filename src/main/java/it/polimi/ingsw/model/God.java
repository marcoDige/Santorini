package it.polimi.ingsw.model;

public class God {

    //attributes

    private String name;
    private String description;
    private Power power;

    //constructors

    public God(String name, String description, Power power) {
        this.name = name;
        this.description = description;
        this.power = power;
    }

    //methods

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Power getPower() {
        return power;
    }
}
