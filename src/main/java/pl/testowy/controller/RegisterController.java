package pl.testowy.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pl.testowy.model.User;
import pl.testowy.service.EmailService;
import pl.testowy.service.UserService;

/*
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
*/

@Controller
public class RegisterController extends WebMvcConfigurerAdapter {
	
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserService userService;
	private EmailService emailService;
	private List<User> users;

 
    @Autowired
    public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder, UserService userService, EmailService emailService) {
      
      this.bCryptPasswordEncoder = bCryptPasswordEncoder;
      this.userService = userService;
      this.emailService = emailService;
    }
    /*
    @ModelAttribute("alreadyRegisteredMessage")
    public String alreadyRegisteredMessage() {
    	return "Oops!  To jest already a user registered with the email provided.";
    }
    
    
    @ModelAttribute("confirmationMessag")
    public String confirmationMessage() {
    	return "A confirmation e-mail has been sent to " + "...";
    }*/

	// Return registration form template
	@GetMapping("register")
	public String showRegistrationPage(@ModelAttribute User user) {	
		return "register";
	}
	
	// Process form input data
	@PostMapping("/register")
	public String processRegistrationForm(@Valid User user, BindingResult bindingResult, HttpServletRequest request, Model model) {			
		// Lookup user in database by e-mail
		User userExists = userService.findByEmail(user.getEmail());
		
		System.out.println(userExists);
		
		if (userExists != null) {
			model.addAttribute("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			bindingResult.reject("email");
			return "register";
		}
			
		if (bindingResult.hasErrors()) { 
			return "register";
		}
		
			// Disable user until they click on confirmation link in email
		    user.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    user.setConfirmationToken(UUID.randomUUID().toString());
		        
		    userService.saveUser(user);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(user.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8080/confirm?token=" + user.getConfirmationToken());
			registrationEmail.setFrom("programowanie11@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			model.addAttribute("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
			
		return "register";
	}
	
	@GetMapping("confirm")
	public String showConfirmationPage(User user, Model model, @RequestParam String token) {
			
		user = userService.findByConfirmationToken(token);
		
		if (user == null) { // No token found in DB
			return "home";
		}

		model.addAttribute("confirmationToken", user.getConfirmationToken());
		
			
		return "confirm";		
	}
	
	// Process confirmation link
	/*@PostMapping("/confirm")
	public ModelAndView processConfirmationForm(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map requestParams) {
				
		modelAndView.setViewName("confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
	
//			String token = (String) requestParams.get("token");
		// Find the user associated with the reset token
		User user = userService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password").toString()));

		// Set user to enabled
		user.setEnabled(true);
		
		// Save user
		userService.saveUser(user);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		return modelAndView;		
	}
*/
}
