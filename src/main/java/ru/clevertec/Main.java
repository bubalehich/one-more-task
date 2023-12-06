package ru.clevertec;

import ru.clevertec.client.Client;
import ru.clevertec.server.Server;

public class Main {
    public static void main(String[] args){
        var client = new Client(new Server(),10);
        client.doSend();
    }
}
