package FIS.ProiectFIS;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        if (userService.authenticate(username, password)) {
            session.setAttribute("user", userService.getUser(username));
            User user = userService.getUser(username);

            String role = user.getRole();
            return switch (role.toLowerCase()) {
                case "antrenor" -> "redirect:/antrenor";
                case "jucator" -> "redirect:/jucator";
                default -> {
                    model.addAttribute("error", "Rol necunoscut.");
                    yield "login";
                }
            };
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        // TODO: hash password înainte de salvare!
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/jucator")
    public String jucator() {
        return "jucator";
    }

    @GetMapping("/antrenor")
    public String antrenor() {
        return "antrenor";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/add-player")
    public String showAddPlayerForm(Model model) {
        model.addAttribute("player", new Player());
        return "add-player";
    }

    // Primește datele din formular și salvează jucătorul
    @PostMapping("/add-player")
    public String addPlayer(@ModelAttribute("player") Player player, Model model) {
        // Verifică dacă numărul de tricou există deja
        if (playerService.shirtNumberExists(player.getShirtNumber())) {
            model.addAttribute("error", "Shirt number already exists!");
            return "add-player";
        }

        // Salvează jucătorul în baza de date
        playerService.savePlayer(player);

        // Redirect la lista jucătorilor după adăugare
        return "redirect:/antrenor";
    }



    @Autowired
    private PlayerService playerService;


    @GetMapping("/make-suggestion")
    public String make_suggestion() {
        return "make-suggestion";
    }

    @Autowired
    private FormationService formationService;

    @GetMapping("/formation")
    public String showFormationForm() {
        return "formation"; // pagina HTML cu formularul select (cel de mai sus)
    }

    @PostMapping("/formation")
    public String saveFormation(@RequestParam("formation") String formationName, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Formation formation = new Formation();
        formation.setName(formationName);
        formation.setUser(user);

        formationService.saveFormation(formation);

        return "redirect:/antrenor";  // sau unde vrei după salvare
    }

    @GetMapping("/remove-player")
    public String showRemovePlayerForm() {
        return "remove-player";  // nu mai trimiți model, că nu ai nevoie
    }

    @PostMapping("/remove-player")
    public String removePlayer(@ModelAttribute Player playerDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/login";
        }

        String name = playerDTO.getFirstName();
        Integer shirtNumber = playerDTO.getShirtNumber();

        if(name == null || name.isEmpty() || shirtNumber == null) {
            return "redirect:/remove-player";
        }

        Player player = playerService.getByFirstNameAndShirtNumber(name, shirtNumber);

        if(player != null) {
            playerService.deletePlayer(player.getId());
        }

        return "redirect:/antrenor";
    }

    @Autowired
    private PlayerStatService playerStatService;

    @GetMapping("/statistics")
    public String showPlayerStatForm(Model model) {
        model.addAttribute("playerStat", new PlayerStat());
        return "statistics"; // numele paginii HTML din templates (statistics.html)
    }

    @PostMapping("/statistics")
    public String savePlayerStat(@ModelAttribute PlayerStat playerStat) {
        playerStatService.saveStat(playerStat);
        return "redirect:/antrenor"; // poți schimba cu alt URL dacă vrei
    }




}
