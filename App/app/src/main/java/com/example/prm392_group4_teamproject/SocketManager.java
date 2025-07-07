package com.example.prm392_group4_teamproject;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private static SocketManager instance;
    private Socket socket;
    private String token;
    private String currentMatchId;
    private OnNewMessageListener onNewMessageListener;
    private OnConnectedListener onConnectedListener;

    public interface OnNewMessageListener {
        void onNewMessage(Message message);
    }

    public interface OnConnectedListener {
        void onConnected();
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect(String token) {
        this.token = token;

        if (socket != null && socket.connected()) return;

        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.reconnectionDelay = 1000;
            options.timeout = 5000;

            Map<String, String> auth = new HashMap<>();
            auth.put("token", token.replace("Bearer ", ""));
            options.auth = auth;

            socket = IO.socket("https://betinder.gicunhco.com", options);

            // Listeners
            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("SocketManager", "Connected to socket");
                if (currentMatchId != null) {
                    joinMatch(currentMatchId); // Tự động join lại match khi reconnect
                }
                if (onConnectedListener != null) {
                    onConnectedListener.onConnected();
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, args ->
                    Log.d("SocketManager", "Disconnected from socket"));

            socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                    Log.e("SocketManager", "Connect error: " + args[0]));

            socket.on("reconnect_attempt", args -> {
                Log.d("SocketManager", "Attempting to reconnect...");
            });

            listenForMessages();

            socket.connect();

        } catch (URISyntaxException e) {
            Log.e("SocketManager", "Socket URI error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    public void joinMatch(String matchId) {
        currentMatchId = matchId;
        if (socket != null && socket.connected()) {
            socket.emit("join_match", matchId);
            Log.d("SocketManager", "Joined match: " + matchId);
        }
    }

    public void sendMessage(String matchId, String content) {
        if (socket != null && socket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("matchId", matchId);
                data.put("content", content);
                data.put("messageType", "text");
                socket.emit("send_message", data);
                Log.d("SocketManager", "Sent message: " + content);
            } catch (JSONException e) {
                Log.e("SocketManager", "Send JSON error: " + e.getMessage());
            }
        } else {
            Log.w("SocketManager", "Socket not connected, cannot send");
        }
    }

    public void setOnNewMessageListener(OnNewMessageListener listener) {
        this.onNewMessageListener = listener;
        listenForMessages();
    }

    public void setOnConnectedListener(OnConnectedListener listener) {
        this.onConnectedListener = listener;
    }

    private void listenForMessages() {
        if (socket != null) {
            socket.off("new_message"); // Clear cũ để tránh lặp
            socket.on("new_message", args -> {
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    JSONObject json = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Message message = gson.fromJson(json.toString(), Message.class);
                    if (onNewMessageListener != null && message != null) {
                        onNewMessageListener.onNewMessage(message);
                    }
                }
            });
        }
    }


    public void reconnectIfNeeded() {
        if (token == null) {
            Log.w("SocketManager", "❗ Token null, không thể reconnect");
            return;
        }

        if (socket == null || !socket.connected()) {
            Log.d("SocketManager", "🛠 Reconnecting socket...");
            connect(token);
        } else {
            Log.d("SocketManager", "✅ Socket đã kết nối");
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.off(); // Remove tất cả listeners
            socket.disconnect();
            socket.close();
            socket = null;
            currentMatchId = null;
            Log.d("SocketManager", "Socket disconnected manually");
        }
    }

}