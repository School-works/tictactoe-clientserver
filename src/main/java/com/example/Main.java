package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static boolean gameRunning = true;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Inserire indirizzo IP:");
        String ip = scanner.nextLine();
        System.out.println("Inserire porta:");
        int port = Integer.parseInt(scanner.nextLine());

        try {
            socket = new Socket(ip, port);
            System.out.println("Connesso al server...");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message = in.readLine();
            if ("WAIT".equals(message)) {
                System.out.println("In aspettamento secondo giocatore...");
                message = in.readLine();
            }

            if ("READY".equals(message)) {
                System.out.println("Il gioco è pronto all'avvio ");

                String playerSymbol = (Math.random() < 0.5) ? "X" : "O";
                String opponentSymbol = playerSymbol.equals("X") ? "O" : "X";
                System.out.println("Sei: " + playerSymbol);

                while (gameRunning) {
                    playerTurn(playerSymbol);
                    waitForOpponentMove(opponentSymbol);
                }
            }

        } catch (IOException e) {
            System.err.println("Errore nella connessione al server: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void playerTurn(String playerSymbol) {
        try {
            System.out.println("\n E' il turno di (" + playerSymbol + "). Scegli una cella da 0 a 8:");
            String boardState = in.readLine();
            printBoard(boardState.split(","));

            Scanner scanner = new Scanner(System.in);
            int move = scanner.nextInt();
            out.println(move);

            String response = in.readLine();
            switch (response) {
                case "OK":
                    System.out.println("Mossa eseguita con successo, aspettando opponente...");
                    break;
                case "KO":
                    System.out.println("Cella non valida, riprova.");
                    playerTurn(playerSymbol);
                    break;
                case "W":
                    System.out.println("Hai vinto!");
                    gameRunning = false;
                    break;
                case "P":
                    System.out.println("Pareggio!");
                    gameRunning = false;
                    break;
            }

        } catch (IOException e) {
            System.err.println("Errore durante il tuo turno: " + e.getMessage());
        }
    }

    private static void waitForOpponentMove(String opponentSymbol) {
        try {
            String boardState = in.readLine();
            printBoard(boardState.split(","));

            String status = in.readLine();
            switch (status) {
                case "W":
                    System.out.println("Avversario (" + opponentSymbol + ") vince!");
                    gameRunning = false;
                    break;
                case "P":
                    System.out.println("Pareggio");
                    gameRunning = false;
                    break;
            }

        } catch (IOException e) {
            System.err.println("Errore aspettando l'avversario " + e.getMessage());
        }
    }

    private static void printBoard(String[] cells) {
        System.out.println("Tabella:");
        for (int i = 0; i < 9; i++) {
            switch (cells[i]) {
                case "0":
                    System.out.print("_ ");
                    break;
                case "1":
                    System.out.print("X ");
                    break;
                case "2":
                    System.out.print("O ");
                    break;
                default:
                    System.out.print("? ");
                    break;
            }
            if (i % 3 == 2) System.out.println();
        }
    }
}