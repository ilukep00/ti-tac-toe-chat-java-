/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TresEnRaya;

import java.io.Serializable;

/**
 *
 * @author alumno
 * clase que describe un movimiento
 * indicando filas, columnas, si se ha producido victoria, si se ha llenado el tablero
 */
public class Movimiento implements Serializable {
    private int x;
    private int y;
    private boolean victoria = false;
    private boolean lleno = false;

    public Movimiento(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isVictoria() {
        return victoria;
    }

    public void setVictoria(boolean victoria) {
        this.victoria = victoria;
    }

    public boolean isLleno() {
        return lleno;
    }

    public void setLleno(boolean lleno) {
        this.lleno = lleno;
    }
    
    
    
    
}
