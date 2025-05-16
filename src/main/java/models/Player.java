package main.java.models;

public class Player extends Person {
    private String position;
    private int shirtNumber;

    public Player() {
    }

    public Player(int id_person, String name, java.util.Date birthday, String nationality, String position, int shirtNumber) {
        super(id_person, name, birthday, nationality);
        this.position = position;
        this.shirtNumber = shirtNumber;
    }

    public Player(String name, java.util.Date birthday, String nationality, String position, int shirtNumber) {
        super(name, birthday, nationality);
        this.position = position;
        this.shirtNumber = shirtNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getShirtNumber() {
        return shirtNumber;
    }

    public void setShirtNumber(int shirtNumber) {
        this.shirtNumber = shirtNumber;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id_person=" + getId_person() +
                ", name='" + getName() + '\'' +
                ", birthday=" + getBirthday() +
                ", nationality='" + getNationality() + '\'' +
                ", position='" + position + '\'' +
                ", shirtNumber=" + shirtNumber +
                '}';
    }
}
