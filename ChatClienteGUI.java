import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ChatClienteGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton enviarButton;
    private JButton listarSalasButton;
    private JButton sairSalaButton;
    private PrintWriter out;
    private String nomeCliente;
    private String salaAtual;

    public ChatClienteGUI() {
        setTitle("Chat Cliente");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Painel de entrada de mensagem
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.addActionListener(this::enviarMensagem);
        inputPanel.add(inputField, BorderLayout.CENTER);

        enviarButton = new JButton("Enviar");
        enviarButton.setFont(new Font("Arial", Font.BOLD, 14));
        enviarButton.setBackground(new Color(0, 120, 215));
        enviarButton.setForeground(Color.WHITE);
        enviarButton.addActionListener(this::enviarMensagem);
        inputPanel.add(enviarButton, BorderLayout.EAST);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        listarSalasButton = new JButton("Listar Salas");
        listarSalasButton.setFont(new Font("Arial", Font.BOLD, 14));
        listarSalasButton.setBackground(new Color(50, 150, 50));
        listarSalasButton.setForeground(Color.WHITE);
        listarSalasButton.addActionListener(this::listarSalas);
        buttonPanel.add(listarSalasButton);

        sairSalaButton = new JButton("Sair da Sala");
        sairSalaButton.setFont(new Font("Arial", Font.BOLD, 14));
        sairSalaButton.setBackground(new Color(200, 50, 50));
        sairSalaButton.setForeground(Color.WHITE);
        sairSalaButton.addActionListener(this::sairSala);
        buttonPanel.add(sairSalaButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Solicita o nome do cliente
        nomeCliente = JOptionPane.showInputDialog(this, "Digite seu nome:");
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            nomeCliente = "Anônimo";
        }

        // Solicita o endereço IP do servidor
        String enderecoServidor = JOptionPane.showInputDialog(this, "Digite o endereço IP do servidor:");
        if (enderecoServidor == null || enderecoServidor.trim().isEmpty()) {
            enderecoServidor = "localhost"; // Usa localhost como padrão
        }

        // Conecta ao servidor
        conectarServidor(enderecoServidor);
    }

    private void enviarMensagem(ActionEvent e) {
        String mensagem = inputField.getText();
        if (mensagem.startsWith("/")) {
            // Processa comandos especiais
            out.println(mensagem);
            if (mensagem.equalsIgnoreCase("/sair")) {
                System.exit(0);
            }
        } else if (!mensagem.trim().isEmpty()) {
            out.println(mensagem);
            chatArea.append("Você: " + mensagem + "\n");
        }
        inputField.setText("");
    }

    private void listarSalas(ActionEvent e) {
        out.println("/listar_salas");
    }

    private void sairSala(ActionEvent e) {
        String novaSala = JOptionPane.showInputDialog(this, "Digite o nome da nova sala (ou crie uma nova):");
        if (novaSala != null && !novaSala.trim().isEmpty()) {
            out.println("/entrar_sala " + novaSala);
        }
    }

    private void conectarServidor(String enderecoServidor) {
        try {
            Socket socket = new Socket(enderecoServidor, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Envia o nome do cliente
            out.println(nomeCliente);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = in.readLine()) != null) {
                        if (mensagem.startsWith("SALAS_DISPONIVEIS:")) {
                            String[] salas = mensagem.substring("SALAS_DISPONIVEIS:".length()).split(",");
                            JOptionPane.showMessageDialog(this, "Salas disponíveis:\n" + String.join("\n", salas));
                        } else {
                            chatArea.append(mensagem + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao servidor.", "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClienteGUI().setVisible(true);
        });
    }
}