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

    @ManyToOne
    @JoinColumn(name = "user_id") // cheia străină către tabela users
    private User user;

    // Constructor implicit
    public Player() {
    }

    // Constructor cu parametri (opțional)
    public Player(String firstName, String lastName, Integer shirtNumber, String position,
                  LocalDate dateOfBirth, String nationality, User user) {
        this.firstName = firstName;
        this.shirtNumber = shirtNumber;
        this.position = position;
        this.user = user;
    }

    // Getteri și setteri

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


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
