package TresEnRaya;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPEchoServerMultiThread extends Thread{
    public static void main(String[] args) {
        TCPEchoServerMultiThread server = new TCPEchoServerMultiThread(12001);
        server.start();
    }
    
    private final int port;
    private final List<ClientThread> clients = new LinkedList<>();
    
    public TCPEchoServerMultiThread(int port) {
        this.port = port;
    }

    @Override
    public void run(){
        try( ServerSocket serverSocket = new ServerSocket(port); ){
        System.out.println("Started server on port " + port);
            // repeatedly wait for connections
            while(! interrupted() ){
                Socket clientSocket = serverSocket.accept();

                ClientThread clientThread = new ClientThread(clients, clientSocket);
                clientThread.start();
            }
        }catch(Exception ex){
            System.out.println("Se ha cerrado la conexion del servidor del chat :(");
        }
    
    }
    
    public class ClientThread extends Thread{
        final List<ClientThread> clients;
        final Socket socket;
        Comunicaciones com;
        
        public ClientThread(List<ClientThread> clients, Socket socket) {
            this.clients = clients;
            this.socket = socket;
            com = new Comunicaciones(this.socket);
 
        }
        
        //only one thread at the time can send messages through the socket
        synchronized public void sendMsg(String msg){
            try {
                com.sendMsg(msg);
            } catch (IOException ex) {
                Logger.getLogger(TCPEchoServerMultiThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            synchronized (clients) { //we must sync because other clients may be iterating over it
                    clients.add(this);
            }
            try {
                boolean victoria = false;
                while(victoria == false){
                    System.out.println("Connection from " + 
                            socket.getInetAddress() + ":" + socket.getPort());


                    //now that we have managed to stablish proper connection, we add ourselve into the list
                    String msg = com.receiveMsg();
                    if("PROTOCOL CIERRE CONEXION".equals(msg)){
                        victoria = true;
                        //si recibo cierre de conexion solo lo envio al cliente del hilo ya que
                        //como los dos reciben victoria desde el servidor del juego los dos clientes
                        //le enviaran al servidor del chat el cierre de conexion. Por lo que cada hilo debera
                        //confirmar a su cliente el cierre de la conexion
                        synchronized(clients){
                            this.com.sendMsg("PROTOCOL CIERRE CONEXION");
                            clients.remove(this);
                        } 
                    }else{
                    //cuando recibimos un mensaje normal, se lo enviamos a todos , incluso al mismo cliente
                    //para confirmarle que el mensaje se ha enviado correctamente.
                        synchronized (clients) { //other clients may be trying to add to the list
                            clients.forEach(c -> c.sendMsg( msg ));
                        }
                    }
                }
                //salimos del bucle y cerramos las conexiones y finalizamos el hilo. 

            }  catch (IOException ex) {
                Logger.getLogger(TCPEchoServerMultiThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TCPEchoServerMultiThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (Exception ex) {
                 ex.printStackTrace();
            }finally{
                try {
                    Thread.sleep(2000);
                
                    socket.close();//this will make sure that the socket closes
                    com.socketClose();
                   
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPEchoServerMultiThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TCPEchoServerMultiThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("Hilo acabado");
        }
    }
    
}
