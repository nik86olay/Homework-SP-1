package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();

        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream out) throws IOException {
                out.write((
                        "HTTP/1.1 202 Created\r\n" +
                                "Content-Type: " + 0 + "\r\n" +
                                "Content-Length: " + 0 + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        server.start(9999);
    }
}


