/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TresEnRaya;


import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author lukep
 */
public class Jugador implements Runnable {
    
    //direccion del servidor del juego
    private String hostAddrJ;
    //puerto del servidor del juego
    private int portJ;
    //direccion del servidor del chat
    private String hostAddrC;
    //puerto del servidor del chat
    private int portC;
    //numero de jugador(PLAYER1(1) O PLAYER(2)
    private int numJugador; 
    // esta conectado no
    private boolean conectado = false;
    //es mi turno o no
    private Turno miTurno;
    //interfaz del jugador(tablero)
    private InterfazJugador tablero; 
    //socket con el servidor del juego
    private Socket socketJ;
     //socket con el servidor del Chat
    private Socket socketC;
    //comunicaciones del juego
    private Comunicaciones comunicationsJ;
    //comunicaciones del Chat
    private Comunicaciones comunicationsC;
    //movimeinto recibido
    private Movimiento movimiento;
    private boolean victoria = false;
    public Jugador(String hostAddrJ, int portJ,String hostAddrC, int portC, InterfazJugador tablero) {
        try {
            this.hostAddrJ = hostAddrJ;
            this.portJ = portJ;
            this.hostAddrC = hostAddrC;
            this.portC = portC;
            this.tablero = tablero;
            tablero.setVisible(true);
            //creo el socket del servidor del juego
            socketJ = new Socket(hostAddrJ,portJ);
            //creo el socket del servidor del chat
            socketC = new Socket(hostAddrC, portC);
            //creo las comunicaciones con el servidor del juego
            comunicationsJ = new Comunicaciones(socketJ);
            //creo las comunicaciones con el servidor del chat
            comunicationsC = new Comunicaciones(socketC);
            //le asigno las comunicaciones al interfaz.
            tablero.setComunications(comunicationsJ);
            tablero.setComChat(comunicationsC);
            //creo el hilo del interfaz que sera el encargado de recibir los mensajes del chat
            Thread hiloTablero = new Thread(tablero);
            hiloTablero.start();
        } catch (IOException ex) {
            Logger.getLogger(Jugador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    
    
    
    
    public static void main(String args[]){
        InterfazJugador interfaz = new InterfazJugador();
        Jugador player = new Jugador("localhost", 12000,"localhost", 12001,  interfaz);
        Thread hiloJugador = new Thread(player);
        hiloJugador.start();
        
    }
    
    //funcion usada para establecer la conexion con el servidor.
    public void iniciarConexiones(){
        try {
            String msg;
            //voy a recibir un mensaje
            msg = comunicationsJ.receiveMsg();
            String[] partes = msg.split(" - ");
            System.out.println(partes[0]);
            String name = null;
            //si recibo conexion
            if("CONEXION".equals(partes[0])){
                //si el servidor me dice que soy el jugador 1
                if("P1".equals(partes[1])){
                    numJugador = 1;
                    tablero.setNumJugador(numJugador);
                    tablero.getjTextField1().setText("PLAYER1");
                    //introduczo mi nombre
                     name = JOptionPane.showInputDialog(tablero, "Escribe tu nombre: ");
                    tablero.getjTextField4().setText(name);
                //si el servidor me dice que soy el jugador 2
                }else if("P2".equals(partes[1])){
                    numJugador = 2;
                    tablero.setNumJugador(numJugador);
                    tablero.getjTextField1().setText("PLAYER2");
                    //introduzco mi nombre
                     name = JOptionPane.showInputDialog(tablero, "Escribe tu nombre: ");
                    tablero.getjTextField4().setText(name);
                }
                conectado = true;
            }
            
           //recibo el turno del servidor
           miTurno = comunicationsJ.receiveTurno();
           if(miTurno.getTurno() == 1){
               tablero.getjTextField2().setText("PLAYER1");
           }
           else{
               tablero.getjTextField2().setText("PLAYER1");
           }
           tablero.setTurno(miTurno);
           
           //le envio al servidor mi nombre
           comunicationsJ.sendMsg(name);
           
           String oponente = comunicationsJ.receiveMsg();
           partes = oponente.split(" - ");
           if("OPPONENT".equals(partes[0])){
                tablero.getjTextField5().setText(partes[1]);
           }
           
        } catch (IOException ex) {
            Logger.getLogger(InterfazJugador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(InterfazJugador.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
    
   public void partida(){
        while(victoria == false){
            try {
                movimiento = comunicationsJ.receiveMovement();
                //recibo mi turno, para ya saber que me toca a mi.
                miTurno = comunicationsJ.receiveTurno();
                //si el movimiento anterior ha sido victoria o el tablero esta lleno
                if(movimiento.isVictoria() == true || movimiento.isLleno() == true){
                    
                    //si seria mi turno ahora no he actualizado la ultima jugada
                    if(numJugador == miTurno.getTurno()){
                        if(numJugador == 1){
                            //muestro el movimiento del jugador anterior
                            insertarMovimiento(movimiento.getX(),movimiento.getY(),"O");
                            //actualizo el turno
                            tablero.setTurno(miTurno);
                            tablero.getjTextField2().setText("PLAYER 1");
                        }
                        else if(numJugador == 2){
                            insertarMovimiento(movimiento.getX(),movimiento.getY(),"X");
                            tablero.setTurno(miTurno);
                            tablero.getjTextField2().setText("PLAYER 2");
                        }
                    }
                    //si ha sido victoria lo muestro.
                    if(movimiento.isVictoria() == true){
                        victoria = true;
                        if(miTurno.getTurno() == 1){
                            JOptionPane.showMessageDialog(tablero, "El jugador "+2+" ha conseguido la victoria");
                        }else if(miTurno.getTurno() ==2){
                            JOptionPane.showMessageDialog(tablero, "El jugador "+1+" ha conseguido la victoria");
                        }
                        
                        //cierro las conexiones con el socket.
                        socketJ.close();
                          
                        //envio al servidor del chat el cierre de la conexion y finalize el hilo
                        //mediante ese mensaje de protocolo
                        tablero.enviarChat("PROTOCOL CIERRE CONEXION");
                        
                        //dejo de mostrar el tablero
                        tablero.setVisible(false);
                          //tablero.cerrarChat();
                           //System.exit(0);
                           
                    //si el tablero esta lleno indico que hay empate.
                    }else if(movimiento.isLleno() == true){
                        JOptionPane.showMessageDialog(tablero, "Ha habido empate");
                         //reseteo los botones
                        resetBotones();
                    }
                }
                
                //SI ha sido una jugada normal
                else{
                    if(numJugador == 1){
                        //muestro el movimiento en el tablero
                        insertarMovimiento(movimiento.getX(),movimiento.getY(),"O");
                        //actualizo el turno
                        tablero.setTurno(miTurno);
                        tablero.getjTextField2().setText("PLAYER 1");
                    }else if(numJugador == 2){
                        insertarMovimiento(movimiento.getX(),movimiento.getY(),"X");
                        tablero.setTurno(miTurno);
                        tablero.getjTextField2().setText("PLAYER 2");
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Jugador.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Jugador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
   }
   
       public void insertarMovimiento(int fila, int columna, String caracter){
           //dependiendo de la columna y la fila indicadas por el movimient
           //insertare la jugada en un boton diferente.
         if(fila == 0 && columna == 0){
             insertIcon(tablero.getjButton1(), caracter);
        } else if (fila == 0 && columna == 1) {
            insertIcon(tablero.getjButton2(), caracter);
        } else if (fila == 0 && columna == 2) {
            insertIcon(tablero.getjButton3(), caracter);
        } else if (fila == 1 && columna == 0) {
            insertIcon(tablero.getjButton4(), caracter);
        } else if (fila == 1 && columna == 1) {
            insertIcon(tablero.getjButton5(), caracter);
        } else if (fila == 1 && columna == 2) {
            insertIcon(tablero.getjButton6(), caracter);
        } else if (fila == 2 && columna == 0) {
            insertIcon(tablero.getjButton7(), caracter);
        } else if (fila == 2 && columna == 1) {
            insertIcon(tablero.getjButton8(), caracter);
        } else if (fila == 2 && columna == 2) {
            insertIcon(tablero.getjButton9(), caracter);
        }
    }
       
    public void insertIcon(JButton boton, String caracter){
        //depende del caracter insertare una imagen u otra
        if("X".equals(caracter)){
            boton.setIcon(new ImageIcon("src/imagenes/equis.png"));
        }else if("O".equals(caracter)){
            boton.setIcon(new ImageIcon("src/imagenes/circle.png"));
        }
        
    }
        public void resetBotones(){
            ImageIcon imageReset = tablero.getEmpty();
            tablero.getjButton1().setIcon(imageReset);

            tablero.getjButton2().setIcon(imageReset);

            tablero.getjButton3().setIcon(imageReset);

            tablero.getjButton4().setIcon(imageReset);

            tablero.getjButton5().setIcon(imageReset);

            tablero.getjButton6().setIcon(imageReset);

            tablero.getjButton7().setIcon(imageReset);

            tablero.getjButton8().setIcon(imageReset);

            tablero.getjButton9().setIcon(imageReset);
                    
    }
    
    //hilo del jugador, sera el encargado de recibir los movimientos del otro jugador
    //por el servidor y actualizar su interfaz
    @Override
    public void run() {
        iniciarConexiones();
        partida();
    }
    
}
