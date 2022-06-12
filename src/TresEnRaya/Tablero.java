/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TresEnRaya;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tablero implements Runnable {
    /*tablero de donde se controlara todo
    --> si ha habido victoria
    --> si esta lleno
    --> ....
    */
    
    private int[][] tablero;
    //atributo donde ira actualizando el turno.
    private Turno tuTurno;
    //socket del servidor
    private ServerSocket serverSocket;
    //variable para ver si ha habido victoria
    private boolean victoria = false;
    //variable para ver si el tablero esta lleno
    private boolean lleno = false;
    //mapeo que guarda el nombre de los jugadores con el numero de victorias
    private Map<String, Integer> jugadoresPartidasGanadas;
    //nombre de los jugadores
    private String nombre1;
    private String nombre2;
    //puerto del servidor
    private final int port;
    
    //movimiento a analizar
    private Movimiento m;
    
     public static void main(String[] args) {
        Tablero tablero = new Tablero(12000);
        Thread hiloServ = new Thread(tablero);
        hiloServ.start();
     }

    public Tablero(int port) {
        this.port = port;
        tuTurno = new Turno(0);
        jugadoresPartidasGanadas = new TreeMap<>();
        iniciarTablero();
    }
    
    //funcion que inicializa el tablero
    public void iniciarTablero(){
        tablero = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = 0;
            }
        }
    }
     
    //hilo
    @Override
    public void run() {
        try {
            //creo el socket
            serverSocket = new ServerSocket(port);
            System.out.println("Started server on " + port);
            while(true){
                //acepto el cliente 1
                Socket clientSocket1 = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket1.getInetAddress() + ":" + clientSocket1.getPort());
                //creo las comunicaciones con ese cliente
                Comunicaciones j1 = new Comunicaciones(clientSocket1);
                //le envio un mensaje de conexion, indicandole que es el jugador 1
                String msg = "CONEXION - P1";
                j1.sendMsg(msg);
                //establezco el turno para jugador 1.
                tuTurno.setTurno(1);
                //acepto el cliente 2
                Socket clientSocket2 = serverSocket.accept();
                System.out.println("Accepted connection from "+ clientSocket1.getInetAddress() + ":" + clientSocket2.getPort());
                //creo las comunicaciones con ese cliente
                Comunicaciones j2 = new Comunicaciones(clientSocket2);
                //le envio un mensaje de conexion, indicandole que es el jugador 2
                msg = "CONEXION - P2";
                j2.sendMsg(msg);

                ////SE ENVIAN LOS TURNOS
                j1.sendTurno(tuTurno);
                j2.sendTurno(tuTurno);
                //recibo los nombres
                nombre1 = j1.receiveMsg();
                ///LOS INSERTO EN EL MAP
                //si el cliente no esta en el map, lo inserto,con el numero de victoias a 0
                if(!jugadoresPartidasGanadas.containsKey(nombre1))
                        jugadoresPartidasGanadas.put(nombre1, 0);
                nombre2 = j2.receiveMsg();
                if(!jugadoresPartidasGanadas.containsKey(nombre2))
                    jugadoresPartidasGanadas.put(nombre2, 0);
                
                //envio los nombres a los contrincantes
                String opponent1 = "OPPONENT - "+nombre2;
                String opponent2 = "OPPONENT - "+nombre1;
                j1.sendMsg(opponent1);
                j2.sendMsg(opponent2);
                victoria = false;
                
                //hasta que no se produce la victoria , jugar
                while(victoria == false){
                    //recibo un movimiento del jugador1
                   m = j1.receiveMovement();
                   //analizo el movimiemto
                   movimiento(m, 1, j2);
                   //si ha habido victoria
                   if(m.isVictoria() == true){
                       //se lo notificamos a los dos jugadores que ha habido victoria
                       j1.sendMovement(m);
                       j1.sendTurno(tuTurno);

                       j2.sendMovement(m);
                       j2.sendTurno(tuTurno);
                       
                       //cerramos las conexiones
                        clientSocket1.close();
                        j1.socketClose();
                        clientSocket2.close();
                        j2.socketClose();
                   }else{
                       //si no ha sido victoria pero tsi se ha llenado el tablero
                       if(m.isLleno() == true){
                           //se lo notificamos a los dos jugadores.
                            j1.sendMovement(m);
                            j1.sendTurno(tuTurno);
                            
                            j2.sendMovement(m);
                            j2.sendTurno(tuTurno);
                       }
                       //recibimos movimiento del jugador 1
                        m = j2.receiveMovement();
                        //se analiza el movimiento.
                        movimiento(m, 2, j1);
                        //s i es victoria
                        if(m.isVictoria() == true){
                            //se lo notificamos a los dos jugadores que ha habido victoria
                            j1.sendMovement(m);
                            j1.sendTurno(tuTurno);

                            j2.sendMovement(m);
                            j2.sendTurno(tuTurno);
                            
                            //cerramos las conexiones
                            clientSocket1.close();
                            j1.socketClose();
                            clientSocket2.close();
                            j2.socketClose();
                        }else if(m.isLleno() == true){
                             //si no ha sido victoria pero tsi se ha llenado el tablero
                            j1.sendMovement(m);
                            j1.sendTurno(tuTurno);
                            
                            //se lo notificamos a los dos jugadores.
                            j2.sendMovement(m);
                            j2.sendTurno(tuTurno);
                        }
                   }
                }
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
            Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //funcion que analiza el movimiento de un jugador
    public void movimiento(Movimiento movement, int numJugador, Comunicaciones com){
        //extraigo la fila 
        int fila = movement.getX();
        //extraigo la columna 
        int columna = movement.getY();
        //si la posicion tiene valor 0, esta libre Ok
        if(tablero[fila][columna] == 0){
            //inserto en esa posicion de la matriz el numero de jugador
            tablero[fila][columna] = numJugador;
            //compruebo el estado del tablero
            estadoTablero(fila, columna,numJugador, com, movement);
        }
        //aunque ya ha comprobado el jugador que la posicion esta ocupada, el servidor
        //tambien lo comprueba para asegurarse
        else{
            System.out.println("Posicion ocupada");
        }
        
    }
    
    //comprueba el estado del tablero tras una tirada
    public void estadoTablero(int fila, int columna, int numero, Comunicaciones com, Movimiento movement){
        //si ha habido victoria
        if(victoria(fila, columna) == true){
            System.out.println("Enhorabuena , jugador "+numero+ " ha conseguido la victoria");
            //le aumento el numero de victorias al jugador en el map
            if(numero == 1){
                aumentarMap(1);
            }else if(numero == 2){
                aumentarMap(2);
            }
            //escribo en un fichero el registro de victorias de cada jugador
            try {
                FileWriter fw = new FileWriter("puntuacion.txt");
               for (String string : jugadoresPartidasGanadas.keySet()) {
                        fw.append(string+" ha ganado "+jugadoresPartidasGanadas.get(string)+" veces"+ "\n");
                }

                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
            }
            //ACTUALIZO A TURNO
            if(numero == 1){
                tuTurno.setTurno(2);
            }
            else if(numero == 2){
                tuTurno.setTurno(1);
            }
            //REINICIO EL TABLERO(por si a caso)
            iniciarTablero();
            //victoria = true;
            victoria = true;
            //indicamos que ese movimiento a sido victoria
            movement.setVictoria(true);
        }
        else{
            //no ha habido victoria
            victoria = false;
            movement.setVictoria(false);
            //vemos si se el tablero esta lleno o no
            slotVacio(numero, com, movement);
        }
        
    }
    
    //funcion para comprobar si el tablero esta lleno o no.
    public void slotVacio(int numero, Comunicaciones com, Movimiento movement){
        //recorre la matriz para comprobar si quedan huecos libres o no
        boolean lleno = true;
         for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(tablero[i][j] == 0){
                    lleno = false;
                }
            }
        }
        //si esta llena.
        if(lleno == true){
            System.out.println("Todas Posiciones llenas,sin ningun ganador");
            //inicio el tablero
            iniciarTablero();
            //actualizo los turnos.
             if(numero == 1){
                tuTurno.setTurno(2);
            }
            else if(numero == 2){
                tuTurno.setTurno(1);
            }
            lleno = true;
            //indicamos que ese movimiento a echo que el tablero se llenase
            movement.setLleno(true);
        }else{
            //actualizamos el turno
            if(numero == 1){
                tuTurno.setTurno(2);
                try {
                    //enviamos el movimiento al otro jugador.
                    com.sendMovement(movement);
                    //enviamos el turno al otro jugador.
                    com.sendTurno(tuTurno);
                } catch (IOException ex) {
                    Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if (numero == 2){
                tuTurno.setTurno(1);
               try {
                    com.sendMovement(movement);
                    com.sendTurno(tuTurno);
                } catch (IOException ex) {
                    Logger.getLogger(Tablero.class.getName()).log(Level.SEVERE, null, ex);
                }
 
            }
        }
    }
    
    //funcion que comprueba si ha habido victoria
    public boolean victoria(int fila, int columna){
        boolean victory = false;
        if(horizontal(fila) == true || vertical(columna) == true || diagonal(fila,columna) == true){
            victory =true;
            
        }
        return victory;
    }
    
    //comprueba si hay 3 en raya por la diagonal
    public boolean diagonal(int fila, int columna){
        boolean tresEnRaya = false;
        if(fila == columna){
            if(tablero[0][0] == tablero[1][1]){
                if(tablero[1][1] == tablero[2][2]){
                    System.out.println("true");
                    tresEnRaya = true;
                }
            }
        }
        if(Math.abs(fila-columna) == 2|| (fila == 1 && columna==1)){
            if(tablero[0][2] == tablero[1][1]){
                if(tablero[1][1] == tablero[2][0]){
                    tresEnRaya = true;
                }
            }
        }
        return tresEnRaya;
    }
    
    //comprueba si hay tres en raya en horizontal
    public boolean horizontal(int fila){
        int valor = tablero[fila][0];
        for(int i = 1; i < 3; i++){
            if(valor != tablero[fila][i]){
                return false;
            }
        }
        return true;
    }
    
    //comprueba si hay tres en raya en vertical
    public boolean vertical(int columna){
        int valor = tablero[0][columna];
        for(int i = 1; i < 3; i++){
            if(valor != tablero[i][columna]){
                return false;
            }
        }
        return true;
      }
    
    //aumentamos el numero de victorias de un jugador.
    public void aumentarMap(int numJugador){
        Integer n = null;
        if(numJugador == 1){
            n = jugadoresPartidasGanadas.get(nombre1);
            n++;
            jugadoresPartidasGanadas.put(nombre1, n);
        }
        else if(numJugador == 2){
            n = jugadoresPartidasGanadas.get(nombre2);
            n++;
            jugadoresPartidasGanadas.put(nombre2, n);
        }
 
    }

}


