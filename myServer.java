import java.io.*;
import java.net.*;
import java.util.*;

public class myServer {
    private static final Set<PrintWriter> clientWriters = new HashSet<>();
    private static final Map<PrintWriter, String> clientNames = new HashMap<>();

    public static void main(String[] args) {
        int port = 12345; // Porta do servidor
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor em execução na porta " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                    new ClientHandler(clientSocket).start(); // Iniciar um novo thread para o cliente
                } catch (IOException e) {
                    System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out); // Adiciona o escritor à lista
                }

                // Pedir nome de usuário
                out.println("Digite seu nome:");
                String name = in.readLine();
                synchronized (clientNames) {
                    clientNames.put(out, name);
                }
                System.out.println(name + " se conectou.");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(name + ": " + inputLine);
                    // Encaminha a mensagem para todos os clientes conectados
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(name + ": " + inputLine); // Enviar mensagem para todos os clientes
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket: " + e.getMessage());
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out); // Remove o escritor ao desconectar
                }
                synchronized (clientNames) {
                    clientNames.remove(out); // Remove o nome do cliente ao desconectar
                }
            }
        }
    }
}
