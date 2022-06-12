/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TresEnRaya;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 *
 * @author alumno
 * 
 * clase que dado un socket se permiten
 * comunicaciones de distintos objectos 
 * a traves de el.
 */

//clase para enviar los diferentes tipos de comunicaciones entre el cliente y el servidor.
public class Comunicaciones {
    //socket en el que voy a escribir
    private Socket socket;
    

    //METODO CONSTRUCTOR
    public Comunicaciones(Socket socket) {
        this.socket = socket;
    }
    
    //enviar movimiento.
    public void sendMovement(Movimiento m) throws IOException{
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(m);
     


    }
    
    //recibir movimiento
    public Movimiento receiveMovement() throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
        Object move = in.readObject();      

        return ((Movimiento)move);
    }
    
    //enviar turno.
    public void sendTurno(Turno t) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(t);
    }
    
    //recibir turno.
    public Turno receiveTurno() throws IOException, ClassNotFoundException{

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
        Object turne = in.readObject();
        return (Turno)turne;
    }
    
    
    public void sendMsg(String msg) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(msg);
    }
    
    public String receiveMsg() throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
        Object msg = in.readObject();
        return (String)msg; 
    }
    
    public void socketClose() throws IOException{
        socket.close();
    }
    
    
}
