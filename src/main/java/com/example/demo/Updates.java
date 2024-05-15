package com.example.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller; // Changed to Controller
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.example.demo.sd_projeto.Message;

@RestController // Changed to Controller
public class Updates {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {
        String jsonString = "{\"message\": \"" + message.toString() + "\"}";
        System.out.println(jsonString);
       
        return new Message(HtmlUtils.htmlEscape(jsonString));
    }
}
