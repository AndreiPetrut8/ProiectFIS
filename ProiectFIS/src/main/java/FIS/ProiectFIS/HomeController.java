package FIS.ProiectFIS;

import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private FormationService formationService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerStatService playerStatService;

    @Autowired
    private PlayerStatRepository playerStatRepository;

    @Autowired
    private FormationRepository formationRepository;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            String role = user.getRole() == null ? "" : user.getRole().toLowerCase();
            return switch (role) {
                case "antrenor" -> "redirect:/antrenor";
                case "jucator" -> "redirect:/jucator";
                default -> "redirect:/login";
            };
        }
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(String username, String password, HttpSession session, Model model) {
        if (userService.authenticate(username, password)) {
            String otp = String.valueOf(new Random().nextInt(900000) + 100000);
            long expiry = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes

            User user = userService.getUser(username);
            OtpData otpData = new OtpData(otp, expiry, 
                user != null ? user.getEmail() : null, 
                user != null ? user.getPhone() : null);

            otpStorage.put(username, otpData);
            session.setAttribute("pendingUser", username);
            session.setAttribute("otpExpiry", expiry);

            // Send OTP via email if available
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                emailService.sendOtp(user.getEmail(), otp);
            }

            // Send OTP via SMS if available
            if (user != null && user.getPhone() != null && !user.getPhone().isBlank()) {
                smsService.sendOtp(user.getPhone(), otp);
            }

            System.out.println("OTP for " + username + ": " + otp + " (expires at " + new Date(expiry) + ")");

            return "redirect:/verify-otp";
        }

        model.addAttribute("error", "Invalid credentials");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        // Validate at least one contact method
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        boolean hasPhone = user.getPhone() != null && !user.getPhone().isBlank();

        if (!hasEmail && !hasPhone) {
            model.addAttribute("error", "You must provide at least an email or a phone number.");
            model.addAttribute("user", user);
            return "register";
        }

        // Validate email format
        if (hasEmail && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("error", "Invalid email format.");
            model.addAttribute("user", user);
            return "register";
        }

        // Validate phone format (basic: digits, spaces, +, -, min 7 chars)
        if (hasPhone && !user.getPhone().matches("^[+]?[0-9\\s-]{7,20}$")) {
            model.addAttribute("error", "Invalid phone number format.");
            model.addAttribute("user", user);
            return "register";
        }

        // Check username uniqueness
        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists.");
            model.addAttribute("user", user);
            return "register";
        }

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
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private final Map<String, OtpData> otpStorage = new HashMap<>();

    private static class OtpData {
        String otp;
        long expiryTime;
        String email;
        String phone;

        OtpData(String otp, long expiryTime, String email, String phone) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.email = email;
            this.phone = phone;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    @GetMapping("/verify-otp")
    public String showOtpPage(HttpSession session, Model model) {
        Long expiry = (Long) session.getAttribute("otpExpiry");
        if (expiry == null) {
            return "redirect:/login";
        }
        model.addAttribute("otpExpiry", expiry);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, HttpSession session, Model model) {
        String username = (String) session.getAttribute("pendingUser");

        if (username == null) return "redirect:/login";

        OtpData otpData = otpStorage.get(username);

        // Check if OTP exists or expired
        if (otpData == null || otpData.isExpired()) {
            otpStorage.remove(username);
            session.removeAttribute("pendingUser");
            session.removeAttribute("otpExpiry");
            model.addAttribute("error", "OTP expired. Please login again.");
            return "redirect:/login";
        }

        if (otpData.otp.equals(otp)) {
            User user = userService.getUser(username);
            if (user == null) {
                model.addAttribute("error", "User not found");
                return "redirect:/login";
            }

            session.setAttribute("user", user);
            otpStorage.remove(username);
            session.removeAttribute("pendingUser");
            session.removeAttribute("otpExpiry");

            String role = user.getRole() == null ? "" : user.getRole().toLowerCase();

            return switch (role) {
                case "antrenor" -> "redirect:/antrenor";
                case "jucator" -> "redirect:/jucator";
                default -> "redirect:/login";
            };
        }

        model.addAttribute("otpExpiry", otpData.expiryTime);
        model.addAttribute("error", "Cod OTP invalid");
        return "redirect:/verify-otp";
    }

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
        User user = userRepository.findFirstByUsername(player.getFirstName());
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
            model.addAttribute("player", formPlayer);
            return "make-suggestion";
        }

        User coach = userRepository.findFirstByUsername(coachUsername);
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
        if(user == null) {
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

        User user = userRepository.findFirstByUsername(username);
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

        playerStatService.saveOrUpdateStat(playerStat);

        model.addAttribute("successMessage", "Statistici salvate cu succes!");
        model.addAttribute("playerStat", new PlayerStat());

        return "antrenor";
    }

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
    public String showStartingTeamSelection(HttpSession session, Model model) {
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

        return "redirect:/antrenor";
    }

    @GetMapping("/change-shirt-number")
    public String showShirtNumberForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            model.addAttribute("errorMessage", "Antrenorul nu v-a înregistrat încă.");
            return "change-shirt-number";
        }

        model.addAttribute("shirtNumber", player.getShirtNumber());
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
            model.addAttribute("shirtNumber", player.getShirtNumber());
            return "change-shirt-number";
        }

        player.setShirtNumber(shirtNumber);
        playerRepository.save(player);

        model.addAttribute("successMessage", "Numărul de tricou a fost schimbat cu succes.");
        model.addAttribute("shirtNumber", shirtNumber);

        return "change-shirt-number";
    }

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
        return "choose-action";
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
            details.put("id", String.valueOf(formation.getId())); 
            details.put("name", formation.getName());
            details.put("description", formation.getDescription());
            details.put("coach", coachName);
            formationDetails.add(details);
        }

        model.addAttribute("formationDetails", formationDetails);
        return "view-formations-coach";
    }

    @PostMapping("/formation/delete")
    public String deleteFormation(@RequestParam(value = "id", required = false) String idRaw) {
        System.out.println("ID primit brut: '" + idRaw + "'");

        if (idRaw == null || idRaw.trim().isEmpty() || idRaw.equals("null")) {
            System.err.println("Eroare: ID-ul este gol sau nevalid!");
            return "redirect:/view-formations-coach";
        }

        try {
            Long id = Long.parseLong(idRaw);
            formationRepository.deleteById(id);
            System.out.println("Ștergere reușită pentru ID: " + id);
        } catch (NumberFormatException e) {
            System.err.println("Nu s-a putut converti ID-ul: " + idRaw);
        }

        return "redirect:/view-formations-coach";
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