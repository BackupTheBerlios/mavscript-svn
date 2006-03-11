/*
 * clParser.java
 *
 * Created on 31. Oktober 2004, 09:58
 */

package mavscript.bin;

import java.util.*;


/* Copyright (c) 2004 - 2006 A.Vontobel  <qwert2003@users.berlios.de>,
 *                                       <qwert2003@users.sourceforge.net>
 *
 *
 * -------------------------------------------------------------
 *
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen
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
 * Mavscript - Parser
 * <p>
 * Sucht den Quelltext Zeile für Zeile nach Mavscript-Anweisungen durch.
 * Der Text wird in eine Liste bestehend aus Instanzen von {@link clBaustein} zerlegt.
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clParser {
    
    private String[] quelle;
    LinkedList ziel = new LinkedList();
    
    // Steuerzeichen
    // Vorsicht: diese Steuerzeichen können nicht einfach so geändert werden, falls sie die gleichen Zeichenabfolge
    // enthalten (wie zB $i und $io).
    private String stzM = "$m";     // Start der Mathe-Anweisung
    private String stzI = "$i";     // Ende der Anweisung: Zeige Eingabe
    private String stzO = "$o";     // Ende der Anweisung: Zeige Ausgabe
    private String stzIO = "$io";   // Ende der Anweisung: Zeige Ein- und Ausgabe
    private String stzN = "$n";     // Ende der Anweisung: Zeige nichts
    
    private final String NZ = System.getProperty("line.separator"); //"\n"; // neue Zeile
    
    /** Creates a new instance of clParser */
    public clParser(String[] quelle) {
        this.quelle = quelle;
        parse();        
    }
    
    /** Creates a new instance of clParser.
     @args dollarersatz: ersetzt $ mit anderem Zeichen, z.B. %. --> %m statt $m*/
    public clParser(String[] quelle, String dollarErsatz) {
        this.quelle = quelle;
        stzM = dollarErsatz + "m";     // Start der Mathe-Anweisung
        stzI = dollarErsatz + "i";     // Ende der Anweisung: Zeige Eingabe
        stzO = dollarErsatz + "o";     // Ende der Anweisung: Zeige Ausgabe
        stzIO = dollarErsatz + "io";   // Ende der Anweisung: Zeige Ein- und Ausgabe
        stzN = dollarErsatz + "n";
        parse();        
    }
    
    private void parse() {
        clBaustein baustein;
        int index_stzM;
        int index_stzI;
        int index_stzO;
        int index_stzIO;
        int index_stzN;
        final int mgdI = 1;
        final int mgdO = 2;
        final int mgdIO = 3;
        final int mgdN = 4;
        final int mgdZeilenEnde = 5;
        int mgd;
        int index_mgd;
        
        zeilenschlaufe:
            for (int i = 0; i < quelle.length; i++) {
                String zeilenrest = quelle[i];
                boolean zeilenanfang = true;
                while (true) {
                    if (hatMatheAnweisung(zeilenrest, zeilenanfang) == false) {
                        baustein = new clBaustein(false);
                        baustein.setText(zeilenrest + NZ);
                        ziel.add(baustein);
                        continue zeilenschlaufe;
                    }
                    
                    // hier weiter geht es nur, falls eine Mathe-Anweisung gefunden wurde.
                    zeilenanfang = false;
                    index_stzM = zeilenrest.indexOf(stzM);
                    assert index_stzM >= 0;
                    
                    // Text vor der Anweisung schreiben
                    if (index_stzM > 0) {
                        baustein = new clBaustein(false);
                        baustein.setText(zeilenrest.substring(0, index_stzM));
                        ziel.add(baustein);
                    }
                    zeilenrest = zeilenrest.substring(index_stzM + stzM.length());
                    
                    // Ende der Mathe-Anweisung suchen
                    index_stzI  = zeilenrest.indexOf(stzI);
                    index_stzO  = zeilenrest.indexOf(stzO);
                    index_stzIO = zeilenrest.indexOf(stzIO);
                    index_stzN  = zeilenrest.indexOf(stzN);
                    mgd = mgdZeilenEnde;
                    index_mgd = zeilenrest.length();
                    if (index_stzI >= 0 && index_stzI < index_mgd) {
                        mgd = mgdI;
                        index_mgd = index_stzI;
                    }
                    if (index_stzO >= 0 && index_stzO < index_mgd) {
                        mgd = mgdO;
                        index_mgd = index_stzO;
                    }
                    if (index_stzN >= 0 && index_stzN < index_mgd) {
                        mgd = mgdN;
                        index_mgd = index_stzN;
                    }
                    if (index_stzIO >= 0 && index_stzIO <= index_mgd) { // Achtung: kleinergleich wichtig, sonst mdgI !!!
                        mgd = mgdIO;
                        index_mgd = index_stzIO;
                    }
                    
                    baustein = new clBaustein(true);
                    String anweisung = zeilenrest.substring(0, index_mgd);
                    // Ein Leerzeichen am Anfang und Ende der Anweisung wird entfernt. Dies dient nur der Darstellung
                    // von $m Anweisung $i.
                    if (anweisung.startsWith(" ")) anweisung = anweisung.substring(1);
                    if (anweisung.endsWith(" ")) anweisung = anweisung.substring(0, anweisung.length()-1);
                    baustein.setInput(anweisung);
                    
                    switch (mgd) {
                        case mgdZeilenEnde:
                            assert index_mgd == zeilenrest.length();
                            baustein.zeigeInput(true);
                            baustein.zeigeOutput(true);
                            zeilenrest = zeilenrest.substring(index_mgd);
                            break;
                        case mgdI:
                            assert index_mgd == index_stzI;
                            baustein.zeigeInput(true);
                            baustein.zeigeOutput(false);
                            zeilenrest = zeilenrest.substring(index_mgd + stzI.length());
                            break;
                        case mgdO:
                            assert index_mgd == index_stzO;
                            baustein.zeigeInput(false);
                            baustein.zeigeOutput(true);
                            zeilenrest = zeilenrest.substring(index_mgd + stzO.length());
                            break;
                        case mgdIO:
                            assert index_mgd == index_stzIO;
                            baustein.zeigeInput(true);
                            baustein.zeigeOutput(true);
                            zeilenrest = zeilenrest.substring(index_mgd + stzIO.length());
                            break;
                        case mgdN:
                            assert index_mgd == index_stzN;
                            baustein.zeigeInput(false);
                            baustein.zeigeOutput(false);
                            zeilenrest = zeilenrest.substring(index_mgd + stzN.length());
                            break;
                        default:
                            assert false;
                    }
                    
                    ziel.add(baustein);
                }
            }
    }
    
    private boolean hatMatheAnweisung(String zeile, boolean zeilenanfang) {
        if (zeile.indexOf(stzM) < 0) return false;
        
        // Abklären, ob Kommentarzeile
        if (zeilenanfang) {
            StringTokenizer tokens = new StringTokenizer(zeile, " \t");
            if (tokens.hasMoreTokens()) {
                String erstestoken = tokens.nextToken();
                if (erstestoken.startsWith("#") || erstestoken.startsWith("//") || erstestoken.startsWith("/*")) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public LinkedList getZiel() {
        return ziel;
    }
    
    
}
