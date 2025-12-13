package io.github.some_example_name;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Inherited;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread server = new ServerThread();
        server.start();
    }

    static class MyThread extends Thread {
        boolean stop = false;
        String name;
        public MyThread(String name) {
            this.name = name;
        }
        @Override
        public void run() {
            while (!stop) {
                System.out.println("Name is " + name);
            }
        }

        public void finish() {
            this.stop = true;
        }
    }

    static class ServerThread extends Thread {
        boolean stop = false;
        @Override
        public void run() {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(9090);
                System.out.println(socket.toString());
                while (!stop) {
                    Socket clientSocket = socket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    String phrase = in.readLine();
                    System.out.println("Client mgs: " + phrase);
                    out.println("I am Server");
                    clientSocket.close();
                }
                // socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void setStop(boolean stop) {
            this.stop = stop;
        }
    }
    static class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 9090);
                System.out.println(socket);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("I am Client");
                String phrase = in.readLine();
                System.out.println("Server mgs: " + phrase);

                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
