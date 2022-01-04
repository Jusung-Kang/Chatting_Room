/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fx_server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Jusung Kang
 */
public class Fx_server extends Application {
    //field
    public static ExecutorService threadpool;
    public static Vector<ClientUtil> clients = new Vector<ClientUtil>();
    ServerSocket serverSocket;
    
    //constructor
    public Fx_server(){        
    }//constructor
    
    //method
    public void startServer(String ip, int port){
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
        }catch(Exception e){
            e.printStackTrace();
            if(!serverSocket.isClosed()){
                stopServer();
            }//if
        }//catch
        
        //wait until client access
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Socket socket = serverSocket.accept();
                        clients.add(new ClientUtil(socket));
                        System.out.println("[client accessed] "+socket.getRemoteSocketAddress()+" : "+Thread.currentThread().getName());
                    }catch(Exception e){
                        if(!serverSocket.isClosed()){
                            stopServer();
                        }
                    }
                }//while
            }//run
        };//runnable
        //threadpool initialize
        threadpool = Executors.newCachedThreadPool();
        threadpool.submit(thread);
        
    }//startServer
    
    public void stopServer(){
        try{
            //close all socket currently accessed
            Iterator<ClientUtil> iterator = clients.iterator();
            while(iterator.hasNext()){
                ClientUtil client = iterator.next();
                client.socket.close();
                iterator.remove();
            }//while
            
            //close server socket
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }//if
            
            //terminate threadpool
            if(threadpool != null && !threadpool.isShutdown()){
                threadpool.shutdown();
            }//if
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//stopServer
    
    @Override
    public void start(Stage primaryStage) {
        //graphic
         BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));
 
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("TimesNewRoman", 12));
        root.setCenter(textArea);

        //server start button
        Button toggleButton = new Button("start");
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
        root.setBottom(toggleButton);

        String IP = "127.0.0.1";
        int port = 9876;

        toggleButton.setOnAction(event -> {
        if(toggleButton.getText().equals("start")){
        startServer(IP, port);
        Platform.runLater(() -> {
        String message = String.format("[server start]\n", IP, port);
        textArea.appendText(message);
        toggleButton.setText("stop");
        });
        }//fi
        else{
        stopServer();
        Platform.runLater(() -> {
        String message = String.format("[server stop]\n", IP, port);
        textArea.appendText(message);
        toggleButton.setText("start");
        });
        }//esle
        });

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("[chat server]");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.setScene(scene);
        primaryStage.show();

    }//start

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }//main
    
}//class
