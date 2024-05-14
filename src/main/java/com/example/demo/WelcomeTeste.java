package com.example.demo;

import com.example.demo.forms.Project;

import org.springframework.web.bind.annotation.RequestParam;
import sd_projeto.Client;
import sd_projeto.Client_I;
import sd_projeto.Message;
import sd_projeto.Query;
import sd_projeto.Request;
import sd_projeto.URL_Content;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;


@Controller
public class WelcomeTeste{

    public static Registry registry;
    public static Request Gateway;
    public static Map<String, Client> clientesAtivos = new HashMap<>(); // Mapa para armazenar os clientes ativos


    @GetMapping("/")
    public String welcome(Model model) {
        try {
            registry = LocateRegistry.getRegistry("localhost", 1098);
            Gateway = (Request) registry.lookup("request");

            //Cria um novo cliente e adiciona-o ao mapa de clientes ativos
            Client c = new Client();
            String clienteId = UUID.randomUUID().toString();
            clientesAtivos.put(clienteId, c);

            model.addAttribute("clienteId", clienteId);

            System.out.println("Cliente criado com sucesso: " + clienteId);

            System.out.println("Inicializado com sucesso!!!");
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Inicializado sem sucesso!!!");
            e.printStackTrace();
        }
        return "home_page";
    }

    @GetMapping("/index")
    public String index(Model model) {
        Query m = new Query();
        model.addAttribute("query", m);
//        Gateway.send_request_queue(c, conteudo);
        return "index";
    }

    @GetMapping("/indexing")
    public String indexing(@ModelAttribute Query pesquisa, Model model) throws RemoteException {
        try{
            String clientId = pesquisa.getClientId();
            Client c = clientesAtivos.get(clientId);

            Message querry = new Message(pesquisa.getContent());

            String content = "Indexação realizada com sucesso!";
            model.addAttribute("content", content);

            Gateway.send_request_queue(c, querry);

            }catch (RemoteException e){
                System.out.println("=======================================");
                e.printStackTrace();
            }


        return "indexed";
    }

    @GetMapping("/search")
    public String search(Model model) {
        // Create a Message object and add it to the model with the correct attribute name

        Query m = new Query();
        model.addAttribute("query", m);

        return "search";
    }

    @GetMapping("/search_result")
    public String search_result(@ModelAttribute Query pesquisa, Model model) {
        try{
        String clientId = pesquisa.getClientId();
        Client c = clientesAtivos.get(clientId);

        Message querry = new Message(pesquisa.getContent());
        ArrayList<URL_Content> content = Gateway.request10((Client_I) c, querry, 0);

        if(content.get(0).title.equals("Falha Ocurrida")){
            content = new ArrayList<>();
//                content.get(0).title = "Universidade de Coimbra";
//                content.get(0).url = "www.uc.pt";
//                content.get(0).citacao = "Universidade de Coimbra";
        }

        model.addAttribute("content", content);


        for(URL_Content u : content)
            System.out.println(u.title + " " + u.url);

        } catch (RemoteException e){
            System.out.println("=======================================");
            e.printStackTrace();
        }


        return "search_results";
    }


    @GetMapping("/info")
    public String info(Model model) {
        return "info";
    }

    @GetMapping("/pages")
    public String pages(Model model) {

        Query m = new Query();
        model.addAttribute("query", m);

        return "pages";
    }

    @GetMapping("/pages_result")
    public String pages_result(@ModelAttribute Query pesquisa, Model model) {
        try{
            String clientId = pesquisa.getClientId();
            Client c = clientesAtivos.get(clientId);

            Message querry = new Message(pesquisa.getContent());
            ArrayList<URL_Content> content = Gateway.request10((Client_I) c, querry, 0);

            if(content.get(0).title.equals("Falha Ocurrida")){
                content = new ArrayList<>();
//                content.get(0).title = "Universidade de Coimbra";
//                content.get(0).url = "www.uc.pt";
//                content.get(0).citacao = "Universidade de Coimbra";
            }

            model.addAttribute("content", content);


            for(URL_Content u : content)
                System.out.println(u.title + " " + u.url);

        } catch (RemoteException e){
            System.out.println("=======================================");
            e.printStackTrace();
        }


        return "pages_results";
    }

    @GetMapping("/hackernews")
    public String hackernews(Model model) {
        return "hackernews";
    }

}
