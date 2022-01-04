/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fx_server;

import java.net.*;
import java.io.*;

/**
 *
 * @author Jusung Kang
 */
public class ClientUtil {
    //field
    Socket socket;    
    
    //constructor
    public ClientUtil(Socket socket){
        this.socket = socket;
        receive();
    }//clientUtil
    
    //method
    public void receive(){
        Runnable thread = new Runnable(){
            @Override
            public void run() {
                try{
                    while(true){
                        InputStream in = socket.getInputStream();
                        byte[] buffer = new byte[512];
                        int length = in.read(buffer);
                        while(length == -1) throw new IOException();
                        System.out.println("message successfully received"+socket.getRemoteSocketAddress()+": "+Thread.currentThread());
                        String message = new String(buffer, 0, length, "UTF-8");
                        
                        //share message with other client
                        for(ClientUtil client: Fx_server.clients){
                            client.send(message);
                        }//for
                    }//while
                }catch(Exception e1){
                    try{
                        System.out.println("[message receive error] " + socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());

                    }catch(Exception e2){
                    }//catch
                }//catch
            }//run
        };//runnable
        Fx_server.threadpool.submit(thread);
    }//receive
    
    public void send(String message){
        Runnable thread = new Runnable(){
            @Override
            public void run() {
                try{
                    OutputStream out = socket.getOutputStream();
                    byte[] buffer = message.getBytes("UTF-8");
                    out.write(buffer);
                    out.flush();
                    
                }catch(Exception e){
                    try{
                        System.out.println("[message send error] " + socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
                        Fx_server.clients.remove(ClientUtil.this);
                        socket.close();
                                
                    }catch(Exception e2){
                    }//catch
                }//catch
            }//run
        };//runnable
        Fx_server.threadpool.submit(thread);
    }//send
    
}//class
