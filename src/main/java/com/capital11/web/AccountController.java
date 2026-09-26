package com.capital11.web;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capital11.service.BankException;
import com.capital11.service.BankService;

/** Pages for a logged-in customer (legacy default/deposit/withdrawl/update.do). */
@Controller
public class AccountController {

    private final BankService bankService;

    public AccountController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/main")
    public String main(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, Model model) {
        model.addAttribute("customer", bankService.getCustomer(cid));
        model.addAttribute("account", bankService.getAccount(cid));
        model.addAttribute("transactions", bankService.listTransactions(cid));
        return "main";
    }

    @GetMapping("/deposit")
    public String depositPage() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, @RequestParam String amount,
                          Model model, RedirectAttributes redirect) {
        return transact(cid, amount, bankService::deposit, "deposit", "Deposited", model, redirect);
    }

    @GetMapping("/withdraw")
    public String withdrawPage() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, @RequestParam String amount,
                           Model model, RedirectAttributes redirect) {
        return transact(cid, amount, bankService::withdraw, "withdraw", "Withdrew", model, redirect);
    }

    @GetMapping("/profile")
    public String profile(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, Model model) {
        model.addAttribute("customer", bankService.getCustomer(cid));
        return "viewinfo";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, Model model) {
        model.addAttribute("customer", bankService.getCustomer(cid));
        return "update";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@SessionAttribute(SessionKeys.CUSTOMER_ID) int cid, @RequestParam String email,
                              @RequestParam String username, @RequestParam(required = false) String password,
                              Model model, RedirectAttributes redirect) {
        try {
            bankService.updateProfile(cid, email.trim(), username.trim(), password);
        } catch (BankException e) {
            model.addAttribute("customer", bankService.getCustomer(cid));
            model.addAttribute("error", e.getMessage());
            return "update";
        }
        redirect.addFlashAttribute("message", "Profile updated.");
        return "redirect:/main";
    }

    private String transact(int cid, String rawAmount, BiConsumer<Integer, BigDecimal> operation, String view,
                            String verb, Model model, RedirectAttributes redirect) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(rawAmount.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Enter a valid amount, e.g. 25.00");
            return view;
        }
        try {
            operation.accept(cid, amount);
        } catch (BankException e) {
            model.addAttribute("error", e.getMessage());
            return view;
        }
        redirect.addFlashAttribute("message", verb + " $" + amount.setScale(2) + ".");
        return "redirect:/main";
    }
}
