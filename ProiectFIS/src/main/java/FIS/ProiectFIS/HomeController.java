package FIS.ProiectFIS;

import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

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
    public String jucator(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        return "jucator";
    }

    @GetMapping("/antrenor")
    public String antrenor(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        return "antrenor";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerService playerService;

    @GetMapping("/add-player")
    public String showAddPlayerForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("player", new Player());
        return "add-player";
    }

    @PostMapping("/add-player")
    public String addPlayer(@ModelAttribute("player") Player player, Model model) {

        Player existingPlayer = playerRepository.findByFirstName(player.getFirstName());
        User user = userRepository.findByUsername(player.getFirstName());
        if (existingPlayer != null) {

            existingPlayer.setShirtNumber(player.getShirtNumber());
            existingPlayer.setPosition(player.getPosition());
            existingPlayer.setStartingTeam(player.getStartingTeam());
            existingPlayer.setUserId(user != null ? user.getId() : null);


            playerService.savePlayer(existingPlayer);
        } else {


            player.setUserId(user != null ? user.getId() : null);
            playerService.savePlayer(player);
        }

        return "redirect:/antrenor";
    }


    @GetMapping("/make-suggestion")
    public String showSuggestionForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            model.addAttribute("errorMessage", "Antrenorul nu te-a înregistrat încă.");
            return "jucator";
        }

        model.addAttribute("player", player);
        return "make-suggestion";
    }

    @PostMapping("/make-suggestion")
    public String submitSuggestion(@ModelAttribute("player") Player formPlayer,
                                   @RequestParam("coachUsername") String coachUsername,
                                   HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            model.addAttribute("errorMessage", "Antrenorul nu te-a înregistrat încă.");
            return "make-suggestion";
        }

        User coach = userRepository.findByUsername(coachUsername);
        if (coach == null) {
            model.addAttribute("errorMessage", "Antrenorul cu acest username nu există.");
            model.addAttribute("player", formPlayer);
            return "make-suggestion";
        }

        player.setSuggestion(formPlayer.getSuggestion());
        player.setCoachUserId(coach.getId());

        playerRepository.save(player);

        return "redirect:/jucator";
    }



    @Autowired
    private FormationService formationService;

    @GetMapping("/formation")
    public String showFormationForm(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        return "formation";
    }

    @PostMapping("/formation")
    public String saveFormation(@RequestParam("formation") String formationName, @RequestParam String description, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Formation formation = new Formation();
        formation.setName(formationName);
        formation.setDescription(description);
        formation.setUserId(user.getId());

        formationService.saveOrUpdateFormation(formation);

        return "redirect:/antrenor";
    }

    @GetMapping("/remove-player")
    public String showRemovePlayerForm(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        return "remove-player";
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
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerStatService playerStatService;

    @GetMapping("/statistics")
    public String showPlayerStatForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("playerStat", new PlayerStat());
        return "statistics";
    }

    @PostMapping("/statistics")
    public String savePlayerStat(@RequestParam String username,
                                 @RequestParam int minutesPlayed,
                                 @RequestParam int goals,
                                 @RequestParam int assists,
                                 @RequestParam int yellowCards,
                                 @RequestParam int redCards,
                                 @RequestParam int passesCompleted,
                                 @RequestParam int shotsOnTarget,
                                 Model model) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            model.addAttribute("errorMessage", "Jucătorul nu există.");
            model.addAttribute("playerStat", new PlayerStat());
            return "statistics";
        }

        Player player = playerRepository.findByFirstName(username);
        if (player == null) {
            model.addAttribute("errorMessage", "Jucătorul nu este înregistrat.");
            model.addAttribute("playerStat", new PlayerStat());
            return "statistics";
        }

        PlayerStat playerStat = new PlayerStat();
        playerStat.setMinutesPlayed(minutesPlayed);
        playerStat.setGoals(goals);
        playerStat.setAssists(assists);
        playerStat.setYellowCards(yellowCards);
        playerStat.setRedCards(redCards);
        playerStat.setPassesCompleted(passesCompleted);
        playerStat.setShotsOnTarget(shotsOnTarget);
        playerStat.setUserId(user.getId());
        playerStat.setPlayerId(player.getId());

        playerStatService .saveOrUpdateStat(playerStat);

        model.addAttribute("successMessage", "Statistici salvate cu succes!");
        model.addAttribute("playerStat", new PlayerStat());

        return "antrenor";
    }


    @Autowired
    private PlayerStatRepository playerStatRepository;

    @GetMapping("/see-statistics")
    public String seeStatistics(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("errorMessage", "Nu ești logat.");
            return "see-statistics";
        }

        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            model.addAttribute("errorMessage", "Jucătorul nu este înregistrat pentru acest utilizator.");
            return "see-statistics";
        }

        PlayerStat playerStat = playerStatRepository.findByPlayerId(player.getId());
        if (playerStat == null) {
            model.addAttribute("errorMessage", "Nu există statistici pentru acest jucător.");
            return "see-statistics";
        }

        model.addAttribute("playerStat", playerStat);
        return "see-statistics";
    }

    @GetMapping("/update-starting-team")
    public String showStartingTeamSelection(HttpSession session,Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        List<Player> players = playerRepository.findAll();
        model.addAttribute("players", players);
        return "update-starting-team";
    }


    @PostMapping("/update-starting-team")
    public String selectStartingTeam(@RequestParam(required = false, name = "startingTeamIds") List<Integer> startingTeamIds) {

        if (startingTeamIds == null) {
            startingTeamIds = Collections.emptyList();
        }

        List<Player> allPlayers = playerRepository.findAll();

        for (Player player : allPlayers) {
            player.setStartingTeam(startingTeamIds.contains(player.getId()));
        }
        playerRepository.saveAll(allPlayers);

        return "redirect:/antrenor"; // sau alt view după salvare
    }

    @GetMapping("/change-shirt-number")
    public String showChangeShirtNumberPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        return "change-shirt-number";
    }


    @PostMapping("/change-shirt-number")
    public String changeShirtNumber(@RequestParam int shirtNumber, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Player player = playerRepository.findByUserId(user.getId());

        if (player == null) {
            model.addAttribute("errorMessage", "Antrenorul nu v-a înregistrat încă.");
            return "change-shirt-number";
        }


        Player existingPlayer = playerRepository.findByShirtNumber(shirtNumber);
        if (existingPlayer != null && !existingPlayer.getId().equals(player.getId())) {
            model.addAttribute("errorMessage", "Numărul de tricou este deja folosit de un alt jucător.");
            return "change-shirt-number";
        }


        player.setShirtNumber(shirtNumber);
        playerRepository.save(player);

        model.addAttribute("successMessage", "Numărul de tricou a fost schimbat cu succes.");
        return "jucator";
    }

    @Autowired
    private FormationRepository formationRepository;

    @GetMapping("/view-formations")
    public String viewFormations(Model model) {
        List<Formation> formations = formationRepository.findAll();

        List<Map<String, String>> formationDetails = new ArrayList<>();

        for (Formation formation : formations) {
            User user = userRepository.findByIdNoOptional(formation.getUserId());

            String coachName = "Necunoscut";
            if (user != null) {
                coachName = user.getUsername();
            }

            Map<String, String> details = new HashMap<>();
            details.put("name", formation.getName());
            details.put("description", formation.getDescription());
            details.put("coach", coachName);

            formationDetails.add(details);
        }

        model.addAttribute("formationDetails", formationDetails);
        return "view-formations";
    }

    @GetMapping("/choose-action")
    public String chooseActionPage() {
        return "choose-action"; // numele fișierului HTML fără .html
    }

    @GetMapping("/view-formations-coach")
    public String viewFormationsCoach(Model model) {
        List<Formation> formations = formationRepository.findAll();

        List<Map<String, String>> formationDetails = new ArrayList<>();

        for (Formation formation : formations) {
            User user = userRepository.findByIdNoOptional(formation.getUserId());

            String coachName = "Necunoscut";
            if (user != null) {
                coachName = user.getUsername();
            }

            Map<String, String> details = new HashMap<>();
            details.put("name", formation.getName());
            details.put("description", formation.getDescription());
            details.put("coach", coachName);

            formationDetails.add(details);
        }

        model.addAttribute("formationDetails", formationDetails);
        return "view-formations-coach";
    }

    @GetMapping("/see-suggestions")
    public String seeSuggestions(HttpSession session, Model model) {
        User coach = (User) session.getAttribute("user");
        if (coach == null) return "redirect:/login";


        List<Player> players = playerRepository.findAllByCoachUserId(coach.getId());


        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Player p : players) {
            User playerUser = userRepository.findByIdNoOptional(p.getUserId());

            Map<String, Object> map = new HashMap<>();
            map.put("playerId", p.getId());
            map.put("playerName", playerUser != null ? playerUser.getUsername() : "Necunoscut");
            map.put("suggestion", p.getSuggestion());

            suggestions.add(map);
        }

        model.addAttribute("suggestions", suggestions);

        return "see-suggestions";
    }

    @PostMapping("/respond-suggestion")
    public String respondSuggestion(@RequestParam Integer playerId,
                                    @RequestParam String suggestion,
                                    HttpSession session,
                                    Model model) {
        User coach = (User) session.getAttribute("user");
        if (coach == null) return "redirect:/login";

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null || !player.getCoachUserId().equals(coach.getId())) {
            model.addAttribute("errorMessage", "Jucătorul nu există sau nu vă aparține.");
            return "see-suggestions";
        }

        String responsePrefix = "Antrenorul " + coach.getUsername() + " a răspuns cu: ";
        String updatedSuggestion = responsePrefix + suggestion;

        player.setSuggestion(updatedSuggestion);
        playerRepository.save(player);

        return "redirect:/see-suggestions";
    }



}
