
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorChat {
    private static final int PORTA = 8080;
    private static final String SENHA_ADMIN = "admin123";
    private static final Map<String, Set<PrintWriter>> salas = new ConcurrentHashMap<>();
    private static final Map<PrintWriter, String> clientes = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Iniciando servidor de chat na porta " + PORTA + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private String nomeCliente;
        private String salaAtual;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                
                // Autenticação
                nomeCliente = in.readLine();
                salaAtual = in.readLine();
                if (salaAtual == null || salaAtual.trim().isEmpty()) {
                    salaAtual = "Principal";
                }

                // Registra cliente
                clientes.put(out, nomeCliente);
                entrarSala(salaAtual);
                
                // Processa mensagens
                String mensagem;
                while ((mensagem = in.readLine()) != null) {
                    if (mensagem.startsWith("/")) {
                        processarComando(mensagem);
                    } else {
                        broadcast(salaAtual, nomeCliente + ": " + mensagem);
                    }
                }
            } catch (IOException e) {
                System.out.println(nomeCliente + " desconectado");
            } finally {
                sairSala(salaAtual);
                clientes.remove(out);
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private void entrarSala(String sala) {
            salaAtual = sala;
            salas.computeIfAbsent(sala, k -> ConcurrentHashMap.newKeySet()).add(out);
            broadcast(sala, "[SISTEMA] " + nomeCliente + " entrou na sala");
            atualizarListaUsuarios(sala);
        }

        private void sairSala(String sala) {
            if (salas.containsKey(sala)) {
                salas.get(sala).remove(out);
                broadcast(sala, "[SISTEMA] " + nomeCliente + " saiu da sala");
                atualizarListaUsuarios(sala);
            }
        }

        private void atualizarListaUsuarios(String sala) {
            StringBuilder lista = new StringBuilder("USUARIOS:");
            salas.getOrDefault(sala, Collections.emptySet())
            .forEach(writer -> lista.append(clientes.get(writer)).append(","));
            broadcast(sala, lista.toString());
        }

        private void processarComando(String comando) {
            if (comando.equalsIgnoreCase("/listar_salas")) {
                out.println("[SISTEMA] Salas disponíveis: " + String.join(", ", salas.keySet()));
            } else if (comando.startsWith("/entrar_sala ")) {
                String novaSala = comando.substring(12).trim();
                sairSala(salaAtual);
                entrarSala(novaSala);
            } else if (comando.startsWith("/admin ")) {
                String senha = comando.substring(7).trim();
                if (senha.equals(SENHA_ADMIN)) {
                    nomeCliente = "Admin-" + nomeCliente;
                    clientes.put(out, nomeCliente);
                    broadcast(salaAtual, "[SISTEMA] " + nomeCliente + " é agora administrador");
                    atualizarListaUsuarios(salaAtual);
                } else {
                    out.println("[SISTEMA] Senha de administrador incorreta");
                }
            }
        }

        private void broadcast(String sala, String mensagem) {
            salas.getOrDefault(sala, Collections.emptySet())
            .forEach(writer -> writer.println(mensagem));
        }
    }
}