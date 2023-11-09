package com.asurma.account.controller;

import com.asurma.account.config.AdyenConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/","index"})
    public String index(Model model) {
        model.addAttribute("environment", AdyenConfiguration.getEnvironment());
        return "index";
    }

}
