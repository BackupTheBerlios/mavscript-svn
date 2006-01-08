/*
 * clConnectYacas.java
 *
 * Created on 10. August 2005
 */

package mavscript.bin;

import net.sf.yacas.YacasInterpreter;
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
 * Stellt die lokale Verbindung zu <code> yacas </code> her und
 * führt die Anfragen durch.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clConnectYacas extends clConnect {
    
//    private CYacas interpreter;
    private YacasInterpreter interpreter;
    private boolean verbose = false;
    
    
    /** Creates a new instance of clConnectYacas
     */
    public clConnectYacas()  {
//        interpreter = new CYacas(new StdFileOutput(System.out)); // funktioniert auch
//        interpreter = new CYacas(new StringOutput(new StringBuffer()));
        interpreter = new YacasInterpreter();
    }
    
    public boolean connect() {
        
//        try {
////            interpreter.env.iCurrentInput = new CachedStdFileInput(interpreter.env.iInputStatus);
//        } catch (Exception e) {
//            System.out.println(e);
//            return false;
//        }
//        try {
//            // Datei scripts.zip suchen, anhand von yacasinit.ys
//            java.net.URL erkenndateiURL = java.lang.ClassLoader.getSystemResource("yacasinit.ys");
//            if (erkenndateiURL == null) throw new java.io.FileNotFoundException("yacasinit.ys not found. Expected in scripts.zip");
//            String erkenndatei = erkenndateiURL.getPath(); // file:/home/av/src/lib/scripts.zip!/yacasinit.ys
//            String zipFileName = erkenndatei.substring(0, erkenndatei.lastIndexOf('!')); // file:/home/av/src/lib/scripts.zip
//            
//            java.util.zip.ZipFile z = new java.util.zip.ZipFile(new java.io.File(new java.net.URI(zipFileName)));
//            LispStandard.zipFile = z;
//            if (verbose) System.out.println("Succeeded in finding "+zipFileName);
//        } catch(Exception e) {
//            System.out.println("Failed to find scripts.zip " + e.toString());
//            return false;
//        }
//        
//        String yacasinitys = interpreter.Evaluate("Load(\"yacasinit.ys\");");
//        if (verbose) System.out.println(yacasinitys);
//        
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
        } else System.out.println("Fehler beim Aufstarten von Yacas. (skript.zip ??).");
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
        String aktAntw;
        try {
            aktAntw = interpreter.Evaluate(befehl);
            if (aktAntw != null) antwort[0] = aktAntw;
            else antwort[0] = "null";
        } catch (Exception e) {
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
