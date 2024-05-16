package com.example.demo;


import com.example.demo.forms.Project;
import com.example.demo.sd_projeto.Client;
import com.example.demo.sd_projeto.Client_I;
import com.example.demo.sd_projeto.Message;
import com.example.demo.sd_projeto.Query;
import com.example.demo.sd_projeto.Request;
import com.example.demo.sd_projeto.URL_Content;
import com.example.demo.sd_projeto.WebServer_I;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


@Controller
public class WelcomeTeste extends UnicastRemoteObject implements WebServer_I{

    public static Registry registry;
    public static Registry registry2;
    public static Request Gateway;
    public static Map<String, Client> clientesAtivos = new HashMap<>(); // Mapa para armazenar os clientes ativos


    public WelcomeTeste() throws RemoteException{
        super();

        LocateRegistry.createRegistry(2500).rebind("WebServer", this);        

        try{
            registry = LocateRegistry.getRegistry("localhost", 1098);
            Gateway = (Request) registry.lookup("request");

            Gateway.ws_conn();

        } catch (RemoteException | NotBoundException e) {
            System.out.println("Inicializado sem sucesso!!!");
            e.printStackTrace();

        }

    }
    
    @Autowired
    private MessagingController messagingController;

    @Override
    public void update(Message m) throws RemoteException {
        
        // Call the WebSocket controller method to send the message
        messagingController.onMessage(m);
    }

    @GetMapping("/")
    public String welcome(Model model) {
        try {
            //Cria um novo cliente e adiciona-o ao mapa de clientes ativos
            Client c = new Client();
            String clienteId = UUID.randomUUID().toString();
            clientesAtivos.put(clienteId, c);

            model.addAttribute("clienteId", clienteId);

            System.out.println("Cliente criado com sucesso: " + clienteId);

            System.out.println("Inicializado com sucesso!!!");
        } catch (RemoteException e) {
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

        //Safty check
        if (Objects.equals(pesquisa.getClientId(), "") || Objects.equals(pesquisa.getContent(), "")){
            return "redirect:/";
        }

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

}
