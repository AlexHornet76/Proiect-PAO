package main.java.models;

import java.util.Date;

/**
 * Person model class representing the PERSON table in the database.
 */
public class Person {

    // Fields corresponding to columns in the PERSON table
    private int id_person;
    private String name;
    private Date birthday;
    private String nationality;

    /**
     * Default constructor
     */
    public Person() {
    }

    /**
     * Parameterized constructor
     *
     * @param id_person The person's ID
     * @param name The person's name
     * @param birthday The person's birthday
     * @param nationality The person's nationality
     */
    public Person(int id_person, String name, Date birthday, String nationality) {
        this.id_person = id_person;
        this.name = name;
        this.birthday = birthday;
        this.nationality = nationality;
    }

    /**
     * Constructor without ID (for new records)
     *
     * @param name The person's name
     * @param birthday The person's birthday
     * @param nationality The person's nationality
     */
    public Person(String name, Date birthday, String nationality) {
        this.name = name;
        this.birthday = birthday;
        this.nationality = nationality;
    }

    // Getters and Setters

    public int getId_person() {
        return id_person;
    }

    public void setId_person(int id_person) {
        this.id_person = id_person;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Override
    public String toString() {
        return "Person [id_person=" + id_person +
                ", name=" + name +
                ", birthday=" + birthday +
                ", nationality=" + nationality + "]";
    }
}