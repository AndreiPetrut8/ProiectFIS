package FIS.ProiectFIS;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User()); // **Foarte important!**
        return "register"; // Numele fișierului register.html din templates
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

    @GetMapping("/index")
    public String index() {
        return "index"; // fără extensia .html
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
            return "redirect:/index";
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }



}
