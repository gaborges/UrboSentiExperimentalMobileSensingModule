/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.device;

import java.io.Serializable;

/**
 *
 * @author Guilherme
 */
public abstract class UrboSentiService implements Serializable  {
    public abstract void start();
    public abstract void stop();
    public abstract void wakeUp();
}
