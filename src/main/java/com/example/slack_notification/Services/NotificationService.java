package com.example.slack_notification.Services;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Map;

@Service
public class NotificationService {

    private final JdbcTemplate jdbc;
    private final RestClient restClient = RestClient.create();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public NotificationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // polls every 5 seconds and drains any pending notifications automatically
    @Scheduled(fixedRate = 500)
    public void pollAndDrain() {
        int sent = drainAndSend();
        if (sent > 0) {
            System.out.println(sent + " notification(s) auto-sent to Slack.");
        }
    }

    public int drainAndSend() {
        int[] sentCount = {0};

        jdbc.execute((java.sql.Connection con) -> {
            CallableStatement cs = con.prepareCall("{call GET_PENDING_NOTIFICATIONS(?)}");
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    long id = rs.getLong("NOTIFICATION_ID");
                    String type = rs.getString("TYPE");
                    String message = rs.getString("MESSAGE");

                    sendToSlack(type, message);
                    jdbc.update("{call MARK_NOTIFICATION_SENT(?)}", id);
                    sentCount[0]++;
                }
            }
            cs.close();
            return null;
        });

        return sentCount[0];
    }

    private void sendToSlack(String type, String message) {
        restClient.post()
                .uri(webhookUrl)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("text", "*" + type + "*\n" + message))
                .retrieve()
                .toBodilessEntity();
    }
}