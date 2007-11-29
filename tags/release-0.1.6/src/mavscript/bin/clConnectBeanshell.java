/*
 * clConnectBeanshell.java
 *
 * Created on 18. June 2005, 19:02
 */

package mavscript.bin;

import bsh.*;
import java.util.*;


/* Copyright (c) 2005 - 2007 A.Vontobel  <qwert2003@users.berlios.de>,
 *                                       <qwert2003@users.sourceforge.net>
 *
 *
 * -------------------------------------------------------------
 *
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen
 * der GNU General Public License, wie von der Free Software Foundation
 * veröffentlicht, weitergeben und/oder modifizieren, entweder gemäss Version 2
 * der Lizenz oder (nach Ihrer Option) jeder späteren Version.

 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, dass es
 * Ihnen von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne
 * die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN
 * BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
 *
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit
 * diesem Programm erhalten haben. Falls nicht, schreiben Sie an die
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
 *
 * Die Lizenz befindet sich in der beiliegenden Datei LICENCE-GPL.txt.
 * Falls nicht, siehe http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 *
 * -------------------------------------------------------------
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * The license is in LICENCE-GPL.txt.
 * If not, see http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
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
    private boolean quiet = false;
    
    
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
        } else System.err.println("keine Verbindung zum Beanshell-Interpreter");
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
                assert !quiet;
                System.out.println("# " + i + ":  " + befehle[i]);
                for (int j = 0; j < antworten[i].length; j++) {
                    System.out.println("@ " + antworten[i][j]);
                }
            }
            else if (!quiet) for (int j = 0; j < antworten[i].length; j++) {System.out.print("#");}
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
        
        if (!quiet) System.out.print("#");
        if (verbose) {
            assert !quiet;
            System.out.println(" " + befehl);
            System.out.println("@ " + antwort[0]);
        }
        
        return antwort;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setQuiet(boolean quiet) {
        if (quiet) verbose = false;
        this.quiet = quiet;
    }
    
    public void stop() {
        interpreter = null;
    }
    
}
