package com.example.testapp;

import com.example.fbgrpc.server.client.ExampleClient;

public class TestApp {
    public static void main(String[] args) {

        int experiment = 10_000;

        ExampleClient exampleClient = new ExampleClient("127.0.0.1", 9090, experiment);
        exampleClient.start();

        System.out.println("Do request");

        // String response = exampleClient.work("Please work!");
        try {
            System.out.println("--------------------- ASYNC (SLOW) --------------------");
            exampleClient.recordRouteAsync();
            System.out.println("--------------------- Blocking (much faster) --------------------");
            exampleClient.recordRouteBlocking();

            // not useful
//            System.out.println("--------------------- Stream Client  --------------------");
//            exampleClient.recordRouteStreamClient();

            // alson ot useful in this context
            //            System.out.println("--------------------- Stream Server  --------------------");
            //            exampleClient.recordRouteServerStream();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // System.out.println("Got response: " + response);
    }
}
