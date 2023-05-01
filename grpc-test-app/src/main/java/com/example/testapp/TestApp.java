package com.example.testapp;

import com.example.fbgrpc.server.client.ExampleClient;

public class TestApp {
    public static void main(String[] args) {

        int experiment = 20_000;

        ExampleClient exampleClient = new ExampleClient("127.0.0.1", 9090, experiment);
        exampleClient.start();

        System.out.println("Experiment size: "+experiment);

        // String response = exampleClient.work("Please work!");
        try {
            // async, fails over 10k request.. probably needs to wait for responses.
//            System.out.println("--------------------- ASYNC (Great throughput, slow latency) --------------------");
//            exampleClient.recordRouteAsync(true);

            System.out.println("--------------------- Blocking (much faster) --------------------");
            // warmup
            exampleClient.EXPERIMENT_SIZE = 1000;
            exampleClient.recordRouteBlocking(false);

            exampleClient.EXPERIMENT_SIZE = experiment;
            exampleClient.recordRouteBlocking(true);

            // not useful (in this application)
//            System.out.println("--------------------- Stream Client  --------------------");
//            exampleClient.recordRouteStreamClient();

            // not useful (in this application)
            //            System.out.println("--------------------- Stream Server  --------------------");
            //            exampleClient.recordRouteServerStream();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // System.out.println("Got response: " + response);
    }
}
