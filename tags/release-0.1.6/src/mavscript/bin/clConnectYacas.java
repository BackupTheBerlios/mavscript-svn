/*
 * clConnectYacas.java
 *
 * Created on 10. August 2005
 */

package mavscript.bin;

import net.sf.yacas.YacasInterpreter;
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
 * Stellt die lokale Verbindung zu <code> yacas </code> her und
 * führt die Anfragen durch.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clConnectYacas extends clConnect {
    
    private YacasInterpreter interpreter;
    private boolean verbose = false;
    private boolean quiet = false;
    
    
    /** Creates a new instance of clConnectYacas
     */
    public clConnectYacas()  {
        interpreter = new YacasInterpreter();
    }
    
    public boolean connect() {
        return true;
    }
    
    /** nur zu Testzwecken*/
    public static void main(String[] args) {
        
        clConnectYacas verbindung = new clConnectYacas();
        
        String[] befehle = new String[4];
        befehle[0] = "a := 5.0;";
        befehle[1] = "b := 3*a";
        befehle[2] = "a/b";
        befehle[3] = "c";
        
        verbindung.setVerbose(true);
        if (verbindung.connect()) {
            verbindung.exec(befehle);
        } else System.err.println("Fehler beim Aufstarten von Yacas.");
        verbindung.stop();
    }
    
    public String[][] exec(String[] befehle) {
        String[][] antworten = new String[befehle.length][1];
        String aktAntw;
        for (int i = 0; i < befehle.length; i++) {
            try {
                aktAntw = interpreter.Evaluate(befehle[i]);
                if (aktAntw != null) antworten[i][0] = aktAntw;
                else antworten[i][0] = "null";
            } catch (Exception e) {
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
        String aktAntw;
        try {
            aktAntw = interpreter.Evaluate(befehl);
            if (aktAntw != null) antwort[0] = aktAntw;
            else antwort[0] = "null";
        } catch (Exception e) {
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
