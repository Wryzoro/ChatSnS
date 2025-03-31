Chat em Java com GUI (Swing) e Sockets

Este projeto implementa um sistema de chat cliente-servidor usando Java Swing para a interface gráfica e sockets para comunicação em rede.

Funcionalidades
Cliente
Interface gráfica amigável com Swing

Conexão a um servidor de chat

Envio e recebimento de mensagens em tempo real

Listagem de salas disponíveis

Troca entre salas de chat

Identificação por nome de usuário

Servidor
Gerencia múltiplas salas de chat

Aceita conexões de vários clientes

Distribui mensagens para todos os clientes na mesma sala

Mantém lista de salas disponíveis

Gerencia entrada/saída de usuários

Como Executar
Pré-requisitos
Java JDK 8 ou superior instalado

Passos
Inicie o servidor:

bash
Copy
javac ServidorChat.java
java ServidorChat
Inicie um ou mais clientes:

bash
Copy
javac ChatClienteGUI.java
java ChatClienteGUI
No cliente:

Digite seu nome quando solicitado

Informe o endereço IP do servidor (ou use "localhost" para conexão local)

Digite o nome da sala desejada ou crie uma nova

Comandos Disponíveis
/listar_salas - Lista todas as salas disponíveis

/entrar_sala [nome] - Entra em uma sala específica

/sair - Sai do chat (encerra o cliente)

Estrutura do Código
ChatClienteGUI.java: Implementa a interface gráfica do cliente e comunicação com o servidor

ServidorChat.java: Implementa o servidor que gerencia as salas de chat e clientes

Tecnologias Utilizadas
Java Swing para a interface gráfica

Java Sockets para comunicação em rede

Threads para lidar com múltiplos clientes simultaneamente

Melhorias Possíveis
Adicionar criptografia para comunicações seguras

Implementar histórico de mensagens

Adicionar recursos como envio de arquivos

Melhorar tratamento de erros e reconexão

Adicionar autenticação de usuários

Autor
56699 - Guilherme Lima