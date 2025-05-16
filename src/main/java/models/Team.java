package main.java.models;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id_team;
    private String name;
    private List<Person> members;

    public Team() {
        this.members = new ArrayList<>();
    }

    public Team(int id_team, String name) {
        this.id_team = id_team;
        this.name = name;
        this.members = new ArrayList<>();
    }

    public int getId_team() {
        return id_team;
    }

    public void setId_team(int id_team) {
        this.id_team = id_team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Person> getMembers() {
        return members;
    }

    public void addMember(Person person) {
        this.members.add(person);
    }

    public void removeMember(Person person) {
        this.members.remove(person);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id_team=" + id_team +
                ", name='" + name + '\'' +
                ", members=" + members +
                '}';
    }
}
