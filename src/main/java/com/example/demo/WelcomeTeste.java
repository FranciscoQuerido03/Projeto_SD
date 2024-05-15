package com.example.demo;


import com.example.demo.forms.Project;

import org.springframework.web.bind.annotation.PostMapping;
import sd_projeto.Client;
import sd_projeto.Client_I;
import sd_projeto.Message;
import sd_projeto.Query;
import sd_projeto.Request;
import sd_projeto.URL_Content;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpServletRequest;


@Controller
public class WelcomeTeste{

    public static Registry registry;
    public static Request Gateway;
    public static Map<String, Client> clientesAtivos = new HashMap<>(); // Mapa para armazenar os clientes ativos

    @Autowired
    private HackerNewsService hackerNewsService;

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

    @GetMapping("/menu")
    public String menu(Model model) {
        Query m = new Query();
        model.addAttribute("query", m);

        return "menu";
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

        // Safty check
        if (Objects.equals(pesquisa.getClientId(), "") || pesquisa.getUrls().isEmpty()){
            return "redirect:/";
        }

        try {
            String clientId = pesquisa.getClientId();
            Client c = clientesAtivos.get(clientId);
            System.out.println("Conteudo "+pesquisa.getContent());


            // Itera sobre os URLs e envia cada um para indexação
            for (String url : pesquisa.getUrls()) {
                System.out.println("URL a indexar: " + url);
                Message querry = new Message(url);
                Gateway.send_request_queue(c, querry);
            }

            String content = "Indexação realizada com sucesso!";
            model.addAttribute("content", content);
        } catch (RemoteException e) {
            System.out.println("=======================================");
            e.printStackTrace();
        }

        return "indexed";
    }


    @GetMapping("/search")
    public String search(@ModelAttribute Query pesquisa, Model model) throws RemoteException {
        Client c = clientesAtivos.get(pesquisa.getClientId());
        // Create a Message object and add it to the model with the correct attribute name
        Gateway.request10(c, new Message(""), -1);
        Query m = new Query();
        model.addAttribute("query", m);

        return "search";
    }

    @GetMapping("/search_result")
    public String search_result(@ModelAttribute Query pesquisa, @RequestParam(defaultValue = "0") int pageNumber, Model model) {

        //Safty check
        System.out.println(pesquisa.getClientId());
        System.out.println(pesquisa.getContent());
        if (Objects.equals(pesquisa.getClientId(), "") || Objects.equals(pesquisa.getContent(), "")){
            return "redirect:/";
        }

        try{
            String clientId = pesquisa.getClientId();
            Client c = clientesAtivos.get(clientId);
            if (c == null){
                return "redirect:/";
            }
            //Message querry = new Message(message.getContent());
            System.out.println(pageNumber);
            ArrayList<URL_Content> content = Gateway.request10(c, new Message(pesquisa.getContent()), pageNumber);

            for(URL_Content u: content)
                System.out.println("===>" + u.citacao);

            if(content.get(0).title.equals("Falha Ocurrida")){
                content.get(0).url = null;
            }

            boolean Next = true;
            boolean Previous = true;

            if(content.size() < 10)
                Next = false;
            
            if(pageNumber == 0)
                Previous = false;

            model.addAttribute("pesquisa", pesquisa);
            model.addAttribute("content", content);
            model.addAttribute("pageNumber", pageNumber);
            model.addAttribute("next", Next);
            model.addAttribute("previous", Previous);


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

        //Safty check
        if (Objects.equals(pesquisa.getClientId(), "") || Objects.equals(pesquisa.getContent(), "")){
            return "redirect:/";
        }

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

    @GetMapping("/hackernews_search")
    public String hackernewsSearch(@ModelAttribute Query pesquisa, Model model) {
        List<String> keywords = Arrays.asList(pesquisa.getContent().split(" "));
        try {
            List<HackerNewsItemRecord> stories = hackerNewsService.fetchTopStoriesWithKeywords(keywords);
            List<String> urls = stories.stream()
                    .map(HackerNewsItemRecord::url)
                    .collect(Collectors.toList());

            Query m = new Query();
            model.addAttribute("query", m);

            model.addAttribute("content", stories);

            System.out.println("URLS: " + urls);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            model.addAttribute("error", "Ocorreu um erro ao buscar as histórias do Hacker News.");
        }
        return "hackernews_results";
    }

}
