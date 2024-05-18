package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sd_projeto.Message;

@Controller // Changed to Controller
public class Updates {

    @Autowired
    SimpMessagingTemplate smt;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {

        String consola = message.toString();
        String[] sections = consola.split("\n");

        String on_svs = sections[2];

        StringBuilder common_s = new StringBuilder();
        StringBuilder resp_t = new StringBuilder();

        int index = 5;
        while (index < sections.length && !sections[index].equals("Average response time: ")) {
            if (!sections[index].trim().isEmpty()) {
                common_s.append(sections[index]).append("<br>");
            }
            index++;
        }

        // Extract 'resp_t'
        if (index + 2 < sections.length) {
            for (int i = index + 2; i < sections.length; i++) {
                if (!sections[i].trim().isEmpty()) {
                    resp_t.append(sections[i]).append("<br>");
                }else
                    break;
            }
        }

        // System.out.println("on " + on_svs);
        // System.out.println("comm " + common_s.toString());
        // System.out.println("resp " + resp_t.toString());
        
        ObjectNode json = objectMapper.createObjectNode();
        json.put("on_svs", on_svs);
        json.put("common_s", common_s.toString().trim());
        json.put("resp_t", resp_t.toString().trim());
        String jsonString = json.toString();

        smt.convertAndSend("/topic/messages", jsonString);
       
        return new Message(HtmlUtils.htmlEscape(jsonString));
    }
}
