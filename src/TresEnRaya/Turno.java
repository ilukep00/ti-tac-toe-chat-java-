/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TresEnRaya;

import java.io.Serializable;


/**
 *
 * @author lukep
 */
public class Turno implements Serializable { 
    private int turno;

    public Turno(int turno) {
        this.turno = turno;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }
    
    


    
}
