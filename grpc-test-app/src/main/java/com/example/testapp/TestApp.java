package com.example.testapp;

import com.example.fbgrpc.server.client.ExampleClient;

public class TestApp {
    public static void main(String[] args) {

        ExampleClient exampleClient = new ExampleClient("127.0.0.1", 9090);
        exampleClient.start();

        System.out.println("Do request");

        String response = exampleClient.work("Please work!");

        System.out.println("Got response: " + response);
    }
}
