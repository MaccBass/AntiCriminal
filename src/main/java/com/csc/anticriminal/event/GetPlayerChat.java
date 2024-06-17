package com.csc.anticriminal.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import okhttp3.*;
import com.google.gson.Gson;

import static org.bukkit.Bukkit.getLogger;


public class GetPlayerChat implements Listener {

    // 플레이어 UUID, 위험도 count
    HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();

    @EventHandler
    public void getChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        String message = e.getMessage();
        LocalDate time = LocalDate.now();

        UUID uuid = p.getUniqueId();
        if(!map.containsKey(uuid)) {
            map.put(uuid, 0);
        }

        String logMessage = "[" + time.toString() + "] " + p.getName() + ": " + message;
        logMessage += "// " + Objects.requireNonNull(p.getAddress()).toString();
        // 채팅내역 검증
        if (isCrimeChat(message)){
            // 카운트 1 증가
            map.put(uuid, map.get(uuid)+1);

            if (map.get(uuid) >= 5){
                String reason = "Aggressive/Criminal Chat. Contact the admin";
                Duration d = Duration.ofDays(7);
                p.banIp(reason, d, null, true);
            }

            logMessage += " !criminal";
        }

        // 아닐시
        else{
            // 카운트 -1
            map.put(uuid, map.get(uuid)-1);
        }

        // debug
        logMessage += map.get(uuid);
        // 로그에 사용자명-카운트 형식으로 남김
        logToFile(p.getName(), logMessage);
    }

    boolean isCrimeChat(String sentence){
        // http 통신

        final String URL = "http://125.128.251.165:5001/predict_chat";
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        boolean result;

        // 보낼 데이터
        String json = gson.toJson(new Sentence(sentence));

        // JSON 요청 본문 생성
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        // 요청 생성
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        // 요청 실행 및 응답 처리
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            Value value = gson.fromJson(responseBody, Value.class);
            getLogger().info("Received value: " + value.getValue());

            if (value.getValue() != 0) {result = true;}
            else {result = false;}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void logToFile(String playerName, String message){
        String serverName = Bukkit.getServer().getName();
        String directoryPath = "logs-anticriminal";
        String fileName = directoryPath + "/" + serverName + " - " + playerName + ".log";

        File directory = new File(directoryPath);
        if (!directory.exists()){
            directory.mkdirs();
        }

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Sentence {
        private final String sentence;

        Sentence(String sentence) {
            this.sentence = sentence;
        }

        public String getSentence() {
            return sentence;
        }
    }

    static class Value {
        private final int value;

        Value(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
