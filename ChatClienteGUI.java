import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClienteGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton enviarButton;
    private JButton listarSalasButton;
    private JButton sairSalaButton;
    private PrintWriter out;
    private String nomeCliente;
    private String salaAtual = "Principal";
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private ImageIcon userIcon;
    private ImageIcon adminIcon;

    public ChatClienteGUI() {
        // Configuração básica da janela
        setTitle("Chat Cliente");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Debug inicial
        debugIconLoading();

        // Inicializa componentes
        initComponents();
        
        // Configura ícones
        setupIcons();
        
        // Conecta ao servidor
        conectarServidor();
    }

    private void debugIconLoading() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources" + File.separator;
        System.out.println("=== VERIFICAÇÃO DE RECURSOS ===");
        System.out.println("Diretório atual: " + System.getProperty("user.dir"));
        System.out.println("Caminho dos recursos: " + resourcesPath);

        String[] icons = {"user.png", "admin.png", "chat.png"};
        for (String icon : icons) {
            File f = new File(resourcesPath + icon);
            System.out.println(icon + " - " + (f.exists() ? "ENCONTRADO" : "NÃO ENCONTRADO") + 
            " em " + f.getAbsolutePath());
        }
    }

    private void initComponents() {
        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // Lista de usuários
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellWidth(150);
        userList.setFixedCellHeight(40);
        JScrollPane userListScroll = new JScrollPane(userList);
        
        // Painel de entrada
        inputField = new JTextField();
        enviarButton = new JButton("Enviar");
        enviarButton.addActionListener(this::enviarMensagem);
        inputField.addActionListener(this::enviarMensagem);
        
        // Botões de controle
        listarSalasButton = new JButton("Listar Salas");
        sairSalaButton = new JButton("Trocar Sala");
        
        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(userListScroll, BorderLayout.WEST);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(enviarButton, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(listarSalasButton);
        buttonPanel.add(sairSalaButton);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        add(mainPanel);
        
        // Configura eventos
        listarSalasButton.addActionListener(e -> {
            if (out != null) out.println("/listar_salas");
            else JOptionPane.showMessageDialog(this, "Conecte-se primeiro ao servidor");
        });
        
        sairSalaButton.addActionListener(e -> {
            if (out == null) {
                JOptionPane.showMessageDialog(this, "Conecte-se primeiro ao servidor");
                return;
            }
            String novaSala = JOptionPane.showInputDialog("Digite o nome da nova sala:");
            if (novaSala != null && !novaSala.isEmpty()) {
                out.println("/entrar_sala " + novaSala);
            }
        });
    }

    private void setupIcons() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources" + File.separator;
            
            // Carrega ícones com verificação explícita
            userIcon = loadIcon(basePath + "user.png", 32, 32);
            adminIcon = loadIcon(basePath + "admin.png", 32, 32);
            
            // Ícone da aplicação
            ImageIcon appIcon = loadIcon(basePath + "chat.png", 64, 64);
            if (appIcon != null) {
                setIconImage(appIcon.getImage());
            }
            
            // Configura renderizador personalizado
            userList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    
                    if (value.toString().startsWith("Admin-")) {
                        label.setIcon(adminIcon);
                        label.setForeground(new Color(200, 0, 0)); // Vermelho para admin
                    } else {
                        label.setIcon(userIcon);
                    }
                    return label;
                }
            });

        } catch (Exception e) {
            System.err.println("Erro ao carregar ícones: " + e.getMessage());
            createFallbackIcons();
        }
    }

    private ImageIcon loadIcon(String path, int width, int height) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + path);
        }
        ImageIcon icon = new ImageIcon(path);
        return resizeIcon(icon, width, height);
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }

    private void createFallbackIcons() {
        System.out.println("Criando ícones padrão...");
        userIcon = createDefaultIcon(new Color(0, 100, 200), "USR");
        adminIcon = createDefaultIcon(new Color(200, 0, 0), "ADM");
    }

    private ImageIcon createDefaultIcon(Color color, String text) {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        // Fundo circular
        g.setColor(color);
        g.fillOval(2, 2, 28, 28);
        
        // Texto centralizado
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int x = (32 - fm.stringWidth(text)) / 2;
        int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
        
        g.dispose();
        return new ImageIcon(image);
    }

    private void conectarServidor() {
        nomeCliente = JOptionPane.showInputDialog("Digite seu nome:");
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            nomeCliente = "Anônimo" + (int)(Math.random() * 1000);
        }
        setTitle("Chat - " + nomeCliente + " (Sala: " + salaAtual + ")");

        try {
            Socket socket = new Socket("localhost", 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Envia credenciais
            out.println(nomeCliente);
            out.println(salaAtual);

            // Thread para receber mensagens
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                    
                    String mensagem;
                    while ((mensagem = in.readLine()) != null) {
                        if (mensagem.startsWith("USUARIOS:")) {
                            updateUserList(mensagem.substring(9));
                        } else {
                            chatArea.append(mensagem + "\n");
                        }
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("[ERRO] Conexão perdida com o servidor\n");
                    });
                }
            }).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao conectar ao servidor: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserList(String usersString) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : usersString.split(",")) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    private void enviarMensagem(ActionEvent e) {
        if (out == null) {
            JOptionPane.showMessageDialog(this, "Conecte-se primeiro ao servidor");
            return;
        }

        String texto = inputField.getText().trim();
        if (!texto.isEmpty()) {
            out.println(texto);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Erro ao definir look and feel: " + e.getMessage());
            }
            new ChatClienteGUI().setVisible(true);
        });
    }
}