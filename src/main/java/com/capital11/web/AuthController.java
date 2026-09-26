package com.capital11.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capital11.domain.AccountType;
import com.capital11.domain.Customer;
import com.capital11.service.BankException;
import com.capital11.service.BankService;

/** Login, logout, registration and password reset (legacy login/register/verify/reset/exist/logout.do). */
@Controller
public class AuthController {

    private final BankService bankService;

    public AuthController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        return bankService.authenticate(username, password)
                .map(customer -> {
                    session.setAttribute(SessionKeys.CUSTOMER_ID, customer.getCid());
                    return "redirect:/main";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid username or password.");
                    return "login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            bankService.register(form.getName(), form.getEmail(), form.getUsername(), form.getPassword(),
                    form.birthday(), form.getGender(), form.getAccountType());
        } catch (BankException e) {
            result.rejectValue("username", "taken", e.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("message", "Account created. Please log in.");
        return "redirect:/login";
    }

    /** Called as the user types on the registration page. */
    @GetMapping(value = "/register/username-check", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String usernameCheck(@RequestParam String username) {
        return bankService.isUsernameAvailable(username)
                ? "<span class=\"available\">Available</span>"
                : "<span class=\"taken\">Taken</span>";
    }

    @ModelAttribute("accountTypes")
    public AccountType[] accountTypes() {
        return AccountType.values();
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "verifyReset";
    }

    @PostMapping("/forgot-password")
    public String verifyUsername(@RequestParam String username, HttpSession session, Model model) {
        Customer customer = bankService.findCustomer(username).orElse(null);
        if (customer == null) {
            model.addAttribute("error", "No user found with that username.");
            return "verifyReset";
        }
        session.setAttribute(SessionKeys.RESET_CUSTOMER_ID, customer.getCid());
        model.addAttribute("username", customer.getUsername());
        return "reset";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password, HttpSession session, Model model,
                                RedirectAttributes redirect) {
        Integer cid = (Integer) session.getAttribute(SessionKeys.RESET_CUSTOMER_ID);
        if (cid == null) {
            return "redirect:/forgot-password";
        }
        if (password.isBlank()) {
            model.addAttribute("error", "Password cannot be blank.");
            return "reset";
        }
        bankService.resetPassword(cid, password);
        session.removeAttribute(SessionKeys.RESET_CUSTOMER_ID);
        redirect.addFlashAttribute("message", "Password updated. Please log in.");
        return "redirect:/login";
    }
}
