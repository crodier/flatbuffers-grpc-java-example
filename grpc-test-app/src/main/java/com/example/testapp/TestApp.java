package com.example.testapp;

import com.example.fbgrpc.stupidserver.client.StupidServerClient;

public class TestApp {
    public static void main(String[] args) {

        StupidServerClient stupidServerClient = new StupidServerClient("127.0.0.1", 9090);
        stupidServerClient.start();

        System.out.println("Do request");

        String response = stupidServerClient.work("Please work!");

        System.out.println("Got response: " + response);
    }
}
