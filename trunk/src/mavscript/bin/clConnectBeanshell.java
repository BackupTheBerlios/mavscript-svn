/*
 * clConnectBeanshell.java
 *
 * Created on 18. June 2005, 19:02
 */

package mavscript.bin;

import bsh.*;
import java.util.*;


/* Copyright (c) 2005 A.Vontobel  <qwert2003@users.berlios.de>
 *                                <qwert2003@users.sourceforge.net>,
 *
 *
 * -------------------------------------------------------------
 *
 * Deses Programm ist freie Software. Sie können es unter den Bedingungen
 * der GNU General Public License, Version 2, wie von der Free Software
 * Foundation herausgegeben, weitergeben und/oder modifizieren.
 *
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, dass es
 * Ihnen von Nutzen sein wird, aber OHNE JEDE GEWÄHRLEISTUNG - sogar ohne
 * die implizite Gewährleistung der MARKTREIFE oder der EIGNUNG FüR EINEN
 * BESTIMMTEN ZWECK.  Details finden Sie in der GNU General Public License.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen  mit
 * diesem Programm erhalten haben. Falls nicht, schreiben Sie an die
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 * Die Lizenz befindet sich in der beiliegenden Datei LICENCE-GPL.txt.
 * Siehe auch http://www.gnu.org/copyleft/gpl.html für weitere Informationen.
 *
 * -------------------------------------------------------------
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 * This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * The license is in LICENCE-GPL.txt.
 * See http://www.gnu.org/copyleft/gpl.html for the details.
 */

/**
 * Stellt die lokale Verbindung zu <code> beanshell </code> her und
 * führt die Anfragen durch.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clConnectBeanshell extends clConnect {
    
    
    private Interpreter interpreter;
    private boolean verbose = false;
    
    
    /** Creates a new instance of clConnectBeanshell
     */
    public clConnectBeanshell()  {
        interpreter = new Interpreter();
    }
    
    /** Es gibt keinen Grund connect() aufzurufen.*/
    public boolean connect() {
        return true;
    }
    
    /** nur zu Testzwecken*/
    public static void main(String[] args) {
        
        clConnectBeanshell verbindung = new clConnectBeanshell();
        
        String[] befehle = new String[4];
        befehle[0] = "a = 5.0;";
        befehle[1] = "b = 3*a";
        befehle[2] = "a/b";
        befehle[3] = "c";
        
        verbindung.setVerbose(false);
        if (verbindung.connect()) {
            verbindung.exec(befehle);
        } else System.out.println("keine Verbindung zum Beanshell-Interpreter");
        verbindung.stop();
    }
    
    public String[][] exec(String[] befehle) {
        String[][] antworten = new String[befehle.length][1];
        Object aktObj;
        for (int i = 0; i < befehle.length; i++) {
            try {
                aktObj = interpreter.eval(befehle[i]);
                if (aktObj != null) antworten[i][0] = aktObj.toString();
                else antworten[i][0] = "null";
            } catch (EvalError e) {
                System.err.println(e.getMessage());
                antworten[i][0] = "ERROR";
            }
            if (verbose) {
                System.out.println("# " + i + ":  " + befehle[i]);
                for (int j = 0; j < antworten[i].length; j++) {
                    System.out.println("@ " + antworten[i][j]);
                }
            } else for (int j = 0; j < antworten[i].length; j++) {System.out.print("#");}
        }
        
        return antworten;
    }
    
    public String[] exec(String befehl) {
        String[] antwort = new String[1];
        Object aktObj;
        try {
            aktObj = interpreter.eval(befehl);
            if (aktObj != null) antwort[0] = aktObj.toString();
            else antwort[0] = "null";
        } catch (EvalError e) {
            System.err.println(e.getMessage());
            antwort[0] = "ERROR";
        }
        
        System.out.print("#");
        if (verbose) {
            System.out.println(" " + befehl);
            System.out.println("@ " + antwort[0]);
        }
        
        return antwort;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void stop() {
        interpreter = null;
    }
    
}
