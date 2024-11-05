import java.io.*;
import java.net.*;
import java.util.Scanner;

public class mySns {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Endereço do servidor
        int port = 12345; // Porta do servidor

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor em " + serverAddress + ":" + port);

            // Thread para receber mensagens do servidor
            new Thread(() -> {
                String serverResponse;
                try {
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Mensagem: " + serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao receber mensagem do servidor: " + e.getMessage());
                }
            }).start();

            // Envio de mensagens
            String userInput;
            System.out.println("Digite uma mensagem para enviar ao chat (ou 'sair' para terminar):");
            while (!(userInput = scanner.nextLine()).equalsIgnoreCase("sair")) {
                out.println(userInput); // Enviar mensagem ao servidor
            }
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
