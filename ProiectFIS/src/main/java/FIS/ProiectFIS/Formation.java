package FIS.ProiectFIS;

import jakarta.persistence.*;

@Entity
@Table(name = "formations")
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // ex: "4-4-2", "4-3-3", etc.

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // antrenorul care a ales formarea

    public Formation() {}

    public Formation(String name, User user) {
        this.name = name;
        this.user = user;
    }

    // getteri si setteri
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

