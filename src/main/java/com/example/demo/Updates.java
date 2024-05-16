package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import sd_projeto.Message;

@Controller // Changed to Controller
public class Updates {

    @Autowired
    SimpMessagingTemplate smt;

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {
        String jsonString = "{\"message\": \"" + message.toString() + "\"}";
        System.out.println(jsonString);
        
        smt.convertAndSend("/topic/messages", jsonString);
       
        return new Message(HtmlUtils.htmlEscape(jsonString));
    }
}
