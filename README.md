# chat_multicast
Implementação de serviço de chat P2P usando Sockets Multicast e UDP

## Bibliotecas utilizadas
  - java.net
  - java.io
  - java.util
  - java.awt
  - javax.swing

## Compilação e execução
```
javac *.java
java ChatGUI
```
Ou execute o arquivo .jar
```
java -jar chat.jar
```

## Como usar
Para utilizar o chat, insira o ID do grupo ao qual você quer se conectar, 
a porta do grupo e o seu apelido. Com a caixa de seleção "Criptografia" você pode escolher se as mensagens que você enviar serão criptografas ou não.

Clique em "Entrar no Grupo".

Agora você pode enviar mensagens multicast ou para um usuário ativo específico selecionando "Todos" ou o apelido do usuário.

Para enviar, escreva a mensagem na caixa de texto e clique em "Enviar".
