package com.example.demo;

import com.example.demo.forms.Project;

import sd_projeto.Client;
import sd_projeto.Client_I;
import sd_projeto.Message;
import sd_projeto.Request;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WelcomeTeste{

    public static Registry registry;
    public static Request Gateway;
    public static Client c;

    @GetMapping("/")
    public String welcome(Model model) {
        try {
            registry = LocateRegistry.getRegistry("localhost", 1098);
            Gateway = (Request) registry.lookup("request");
            c = new Client();
            System.out.println("Inicializado com sucesso!!!");
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Inicializado sem sucesso!!!");
            e.printStackTrace();
        }
        return "home_page";
    }

    @GetMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/search")
    public String search(Model model) {
        try{
            Gateway.request10((Client_I) c, new Message("Ola"), 0);
        } catch (RemoteException e){
            System.out.println("=======================================");
            e.printStackTrace();
        }
        return "search";
    }

    @GetMapping("/search_result")
    public String search_result(Model model) {
        
        return "search_results";
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
