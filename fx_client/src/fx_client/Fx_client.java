/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fx_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Jusung Kang
 */
public class Fx_client extends Application {
    //field
    Socket socket;
    TextArea textArea;
    
    //consructor
    
    //method
    public void startClient(String ip, int port){
        Thread thread = new Thread(){
          public void run(){
              try{
                  socket = new Socket(ip, port);
                  receive();
              }catch(Exception ex1){
                  if(socket.isClosed()){
                      stopClient();
                      System.out.println("[fail to access server]");
                      Platform.exit();
                  }
                  
              }//catch
          }//run
        };//thread
        thread.start();
    }//startClinet
    
    public void stopClient(){
        try{
            if(socket != null && !socket.isClosed()){
                socket.close();
            }//if
        }catch(Exception ex2){
            ex2.printStackTrace();
        }//catch
            
    
    }//stopClient
    
    public void receive(){
        while(true){
            try{
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[512];
                int length = in.read(buffer);
                if(length == -1) throw new IOException();
                String message = new String(buffer, 0, length);
                
                Platform.runLater(() -> {
                    textArea.appendText(message);
                });
            }catch(Exception ex3){
                
            }
        }//while
    }//receive
    
    public void send(String message){
        Thread thread = new Thread(){
            public void run(){
                try{
                    OutputStream out = socket.getOutputStream();
                    byte[] buffer = message.getBytes("UTF-8");
                    out.write(buffer);
                    out.flush();
                    
                }catch(Exception ex3){
                    stopClient();
                }
            }//run
        };
        thread.start();
    }//send
    
    
    @Override
    public void start(Stage primaryStage) {
        //dialog design part
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));
        
        HBox hbox = new HBox();
        hbox.setSpacing(5);
        
        //userName
        TextField userName = new TextField();
        userName.setPrefWidth(150);
        userName.setPromptText("Enter your name: ");
        HBox.setHgrow(userName, Priority.ALWAYS);
        
        //IP Number
        TextField ipNumber = new TextField("127.0.0.1");
        
        //Port Number
        TextField portNumber = new TextField("9876");
        portNumber.setPrefWidth(80);
        
        //top part
        hbox.getChildren().addAll(userName, ipNumber, portNumber);
        root.setTop(hbox);
        
        //middle
        textArea = new TextArea();
        textArea.setEditable(false);
        root.setCenter(textArea);
        
        //bottom
        TextField input = new TextField();
        input.setPrefWidth(Double.MAX_VALUE);
        input.setDisable(true);
        input.setOnAction(event -> {
            send(userName.getText() + ": "+input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        });
        
        Button sendButton = new Button("send");
        sendButton.setDisable(true);
        
        sendButton.setOnAction(event->{
            send(userName.getText()+": "+input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        });
        Button connectionButton = new Button("access");
        connectionButton.setOnAction(event -> {
            if(connectionButton.getText().equals("access")){
                int port = 9876;
                try{
                    port = Integer.parseInt(portNumber.getText());
                }catch(Exception ex4){
                    ex4.printStackTrace();
                }//catch
                startClient(ipNumber.getText(), port);
                Platform.runLater(() -> {
                    textArea.appendText("[access chatroom]\n");
                });
                connectionButton.setText("exit");
                input.setDisable(false);
                sendButton.setDisable(false);
                input.requestFocus();
            }//if
            else{
                stopClient();
                Platform.runLater(() ->{
                    textArea.appendText("[exit chatroom]\n");
                });
                connectionButton.setText("access");
                input.setDisable(true);
                sendButton.setDisable(true);
            }//else
        
        });
        
        BorderPane pane = new BorderPane();
        pane.setLeft(connectionButton);
        pane.setCenter(input);
        pane.setRight(sendButton);
        
        root.setBottom(pane);
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("[chatting Client]");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> stopClient());
        primaryStage.show();
        
        connectionButton.requestFocus();
        
        
    }//start

    
    public static void main(String[] args) {
        launch(args);
    }//main
    
}//class
