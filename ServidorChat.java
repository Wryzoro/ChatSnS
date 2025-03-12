import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorChat {
    private static final int PORTA = 12345;
    private static Map<String, Set<PrintWriter>> salas = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor aguardando conexões na porta " + PORTA + "...");
            System.out.println("Endereço do Servidor: " + Inet4Address.getLocalHost().getHostAddress());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + socket);

                new Thread(new ManipuladorCliente(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ManipuladorCliente implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nomeCliente;
        private String salaAtual;

        public ManipuladorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Solicita o nome do cliente
                out.println("Digite seu nome:");
                nomeCliente = in.readLine();

                // Lista as salas disponíveis
                out.println("SALAS_DISPONIVEIS:" + String.join(",", salas.keySet()));

                // Solicita a sala desejada
                out.println("Digite o nome da sala (ou crie uma nova):");
                salaAtual = in.readLine();

                // Cria a sala se não existir
                salas.computeIfAbsent(salaAtual, k -> new HashSet<>()).add(out);
                broadcast(salaAtual, nomeCliente + " entrou na sala.");

                String mensagem;
                while ((mensagem = in.readLine()) != null) {
                    if (mensagem.startsWith("/")) {
                        processarComando(mensagem);
                    } else {
                        broadcast(salaAtual, nomeCliente + ": " + mensagem);
                    }
                }
            } catch (IOException e) {
                System.out.println(nomeCliente + " saiu da sala " + salaAtual);
            } finally {
                if (salaAtual != null && salas.containsKey(salaAtual)) {
                    salas.get(salaAtual).remove(out);
                    broadcast(salaAtual, nomeCliente + " saiu da sala.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processarComando(String comando) {
            if (comando.equalsIgnoreCase("/sair")) {
                out.println("/sair");
            } else if (comando.equalsIgnoreCase("/listar_salas")) {
                out.println("SALAS_DISPONIVEIS:" + String.join(",", salas.keySet()));
            } else if (comando.startsWith("/entrar_sala ")) {
                String novaSala = comando.substring("/entrar_sala ".length());
                if (salas.containsKey(novaSala)) {
                    salas.get(salaAtual).remove(out);
                    salaAtual = novaSala;
                    salas.get(salaAtual).add(out);
                    out.println("Você entrou na sala: " + novaSala);
                } else {
                    out.println("Sala não encontrada: " + novaSala);
                }
            } else {
                out.println("Comando desconhecido: " + comando);
            }
        }

        private void broadcast(String sala, String mensagem) {
            if (salas.containsKey(sala)) {
                for (PrintWriter cliente : salas.get(sala)) {
                    cliente.println(mensagem);
                }
            }
        }
    }
}