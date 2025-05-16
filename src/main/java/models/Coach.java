package main.java.models;

public class Coach extends Person {
    private String type;
    private int experience;

    public Coach() {
    }

    public Coach(int id_person, String name, java.util.Date birthday, String nationality, String type, int experience) {
        super(id_person, name, birthday, nationality);
        this.type = type;
        this.experience = experience;
    }

    public Coach(String name, java.util.Date birthday, String nationality, String type, int experience) {
        super(name, birthday, nationality);
        this.type = type;
        this.experience = experience;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    public String toString() {
        return "Coach{" +
                "id_person=" + getId_person() +
                ", name='" + getName() + '\'' +
                ", birthday=" + getBirthday() +
                ", nationality='" + getNationality() + '\'' +
                ", type='" + type + '\'' +
                ", experience=" + experience +
                '}';
    }
}
