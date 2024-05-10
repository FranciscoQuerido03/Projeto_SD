package com.example.demo;

import com.example.demo.forms.Project;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class WelcomeTeste{

    @GetMapping("/")
    public String welcome(Model model) {
        return "home_page";
    }

    @GetMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/search")
    public String search(Model model) {
        return "search";
    }

    @GetMapping("/info")
    public String info(Model model) {
        return "info";
    }

    @GetMapping("/pages")
    public String pages(Model model) {
        return "pages";
    }

    @GetMapping("/hackernews")
    public String hackernews(Model model) {
        return "hackernews";
    }

}
