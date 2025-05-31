package FIS.ProiectFIS;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;


    @Column(name = "shirt_number", unique = true, nullable = false)
    private Integer shirtNumber;

    @Column(nullable = false)
    private String position;

    @Column(name = "user_id", nullable = true)
    private Integer userId;

    private Boolean startingTeam = false;

    @Column(length = 1000)
    private String suggestion;

    @Column
    private Integer coachUserId;

    public Player() {
    }


    public Player(String firstName, String lastName, Integer shirtNumber, String position,
                  LocalDate dateOfBirth, String nationality, Integer userId) {
        this.firstName = firstName;
        this.shirtNumber = shirtNumber;
        this.position = position;
        this.userId = userId;
    }


    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public Integer getCoachUserId() { return coachUserId; }
    public void setCoachUserId(Integer coachUserId) { this.coachUserId = coachUserId; }


    public Boolean getStartingTeam() {
        return startingTeam;
    }

    public void setStartingTeam(Boolean startingTeam) {
        this.startingTeam = startingTeam;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getShirtNumber() {
        return shirtNumber;
    }

    public void setShirtNumber(Integer shirtNumber) {
        this.shirtNumber = shirtNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
