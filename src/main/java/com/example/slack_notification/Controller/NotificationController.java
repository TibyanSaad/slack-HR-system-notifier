package com.example.slack_notification.Controller;

import com.example.slack_notification.Services.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/notifications/drain")
    public String drain() {
        return service.drainAndSend() + " notification(s) sent to Slack.";
    }
}