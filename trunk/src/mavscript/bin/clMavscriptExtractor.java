/*
 * berechne.java
 *
 * Created on 30. Oktober 2004, 17:49
 */

package mavscript.bin;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import mavscript.bin.inConst;


/* Copyright (c) 2004 - 2007 A.Vontobel  <qwert2003@users.berlios.de>,
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
 * Mavscript - Hauptklasse für Textdateien
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clMavscriptExtractor implements inConst {
    
    private String quelldatei;
    private String quellarchiv;
    private String dateiImArchiv; // = "content.xml";
    private String zieldatei;
    private String vorlaufdatei;
    private boolean mitvorlauf = false;
    private boolean htmlkonvertieren = false;
    private boolean utf2asciikonvertieren = false;
    private boolean dollarersetzen = false;
    private String dollarersatz = "$"; // wird nur ausgewertet wenn dollarersetzen == true
    
    String[] quelle;
    String[] vorlauf;
    LinkedList zielListe = new LinkedList();
    String ziel;
    private boolean FEHLER = false;
    private boolean verbose = false;
    private htmlConverter converter;
    private clUTF2asciiConverter UTFconverter;
    private boolean TRAILINGSEMICOLON = false;
    private boolean istZIP; // false für Textdateien, true für gezippte Textdateien
    
    // Verbindungstyp
    private int verbindungstyp = 1;
//    private final int port = 1;
//    private final int beanshell = 2;
//    private final int yacas = 3;
    
    String charsetName = "UTF-8";
    
    private final String NZsys = System.getProperty("line.separator");
    private String NZ = NZsys; //"\n"; // neue Zeile
    private Locale locale = Locale.getDefault();
    private final clTranslation tr = new clTranslation("mavscript/locales/clMavscript");
    
    
    /** Creates a new instance of clMavscriptExtractor
     * Für Textdateien
     */
    public clMavscriptExtractor(int verbindungsTyp, String indatei, String outdatei) {
        quelldatei = indatei;
        zieldatei = outdatei;
        this.verbindungstyp = verbindungsTyp;
        istZIP = false;
        switch (verbindungstyp) {
            case yacas:
            case beanshell:
            case port: // zurzeit hängt clConnectPort immer einen Strichpunkt an.
                TRAILINGSEMICOLON = true;
                break;
            default:
                TRAILINGSEMICOLON = false;
        }
    }
    
    /** Creates a new instance of clMavscriptExtractor
     * Für gezippte Textdateien. Z.B. .odt
     */
    public clMavscriptExtractor(int verbindungsTyp, String inarchiv, String dateiimarchiv, String outdatei) {
        quellarchiv = inarchiv;
        dateiImArchiv = dateiimarchiv;
        zieldatei = outdatei;
        this.verbindungstyp = verbindungsTyp;
        istZIP = true;
        switch (verbindungstyp) {
            case yacas:
            case beanshell:
                TRAILINGSEMICOLON = true;
                break;
            case port:
            default:
                TRAILINGSEMICOLON = false;
        }
    }
    
    /** Hängt einen Strichpunkt an das Ende der Anweisung, falls noch keiner vorhanden ist.*/
    public void setTrailingSemicolon(boolean TRAILINGSEMICOLON) {
        this.TRAILINGSEMICOLON = TRAILINGSEMICOLON;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
        tr.setLocale(locale);
    }
    
    public void setVorlauf(String vorlaufdatei) {
        this.vorlaufdatei = vorlaufdatei;
        mitvorlauf = true;
    }
    
    public void setHTMLkonvertieren(boolean htmlkonvertieren) {
        this.htmlkonvertieren = htmlkonvertieren;
    }
    
    public void setUTF2asciikonvertieren(boolean utf2asciikonvertieren) {
        this.utf2asciikonvertieren = utf2asciikonvertieren;
    }
    
    public void setCharset(String neuerCharsetName) {
        if (neuerCharsetName.equalsIgnoreCase("system")) {
            neuerCharsetName = System.getProperty("file.encoding");
        }
        
        boolean charsetOK = false;
        try {
            charsetOK = java.nio.charset.Charset.isSupported(neuerCharsetName);
            
        } catch (java.nio.charset.IllegalCharsetNameException e) {
            String Fehlermeldung = tr.tr("InvalidCharset") +" "+ neuerCharsetName  + NZsys + tr.tr("ErrorMessage") + ": " + e
                    + NZsys + charsetName + " " + tr.tr("UsedInstead");
            System.err.println(Fehlermeldung);
        }
        if (charsetOK) charsetName = neuerCharsetName;
        else {
            String Fehlermeldung = tr.tr("InvalidCharset") +" "+ neuerCharsetName + NZsys + charsetName + " " + tr.tr("UsedInstead");
            System.err.println(Fehlermeldung);
        }
    }
    
    /** Ein Zeichen (oder eine Zeichenfolge) angeben, welche '$' in $m/$i/etc ersetzt.*/
    public void setDollarErsatz(String dollarersatz) {
        if (dollarersatz.length() < 1) {
            System.err.println("Warnung: '$' can not be replaced by " + dollarersatz);
            return;
        }
        dollarersetzen = true;
        this.dollarersatz = dollarersatz;
    }
    
    /** Startet den Programmablauf: Einlesen, Parsen, Rechnen lassen, Rückeinsetzen, Schreiben*/
    public boolean run() {
        if (!FEHLER) {
            if (istZIP) quellarchivEinlesen(quellarchiv, dateiImArchiv);
            else quelldateiEinlesen(quelldatei);
        }
        if (mitvorlauf && !FEHLER) vorlaufdateiEinlesen(vorlaufdatei);
        if (!FEHLER) quelldateiParsen();
        System.out.println("");
        if (!FEHLER) befehlliste_schreiben();
        if (!FEHLER) zieldateiSchreiben(zieldatei);
        return !FEHLER;
    }
    
    private void quelldateiEinlesen(String dateiname) {
        LinkedList quelleListe = new LinkedList();
        String Fehlermeldung;
        int zeilennr = 0;
        
        try {
            File datei = new File(dateiname);
            InputStreamReader eingabestrom = new InputStreamReader(new FileInputStream(datei), charsetName);
            BufferedReader eingabe = new BufferedReader(eingabestrom);
            
            String zeile = "";
            boolean EOF = false;
            
            do {
                zeilennr++;
                zeile = eingabe.readLine();
                if (zeile != null) quelleListe.add(zeile);
                else EOF = true;
                
            }
            while (EOF == false);
            eingabe.close();
            eingabestrom.close();
            
        } catch(FileNotFoundException e) {
            Fehlermeldung = tr.tr("File")+" " + dateiname + " "+tr.tr("NotExisting")+"!" + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        } catch(IOException e) {
            Fehlermeldung = tr.tr("ErrorInFile") +" " + dateiname + ", "+tr.tr("Line")+" " + zeilennr + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        }
        
        // Array quelle schreiben
        quelle = new String[quelleListe.size()];
        int i = 0;
        for (Iterator it = quelleListe.iterator(); it.hasNext();) {
            quelle[i] = (String) it.next();
            i++;
        }
        
        /*
        // debug
        System.out.println("");
        System.out.println("Quelldatei:");
        System.out.println("-----------");
        System.out.println("");
        for (int j = 0; j < quelle.length; j++) {
            System.out.println(quelle[j]);
        }
        System.out.println("");
        System.out.println("");
         */
        
        if (!FEHLER) System.out.println(tr.tr("Template") + " " + dateiname + " " + tr.tr("Read"));
        
        // Zeilenend-Zeichen feststellen "\n", "\r\n" oder "\r"
        try {
            File datei = new File(dateiname);
            InputStreamReader eingabestrom = new InputStreamReader(new FileInputStream(datei), charsetName);
            char[] cbuf = new char[2];
            eingabestrom.skip(quelle[0].length());
            eingabestrom.read(cbuf, 0, cbuf.length);
            eingabestrom.close();
            if (cbuf[0] == '\n') NZ = "\n"; // Unix
            else if (cbuf[0] == '\r') {
                if (cbuf[1] == '\n') NZ = "\r\n"; // Windows
                else NZ = "\r"; // Mac früher
            }
            else NZ = NZsys; // wenn nichts entdeckt wird. Z.B. Einzeiler.
        }
        catch(Exception e) {
            Fehlermeldung = tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            System.err.println("Could not detect NewLine char: using system default.");
        }
    }
    
    private void quellarchivEinlesen(String dateiname, String dateiimarchivname) {
        LinkedList quelleListe = new LinkedList();
        String Fehlermeldung;
        int zeilennr = 0;
        
        try {
            ZipFile datei = new ZipFile(dateiname);
            ZipEntry entry = datei.getEntry(dateiimarchivname);
            BufferedReader eingabe = new BufferedReader(new InputStreamReader(datei.getInputStream(entry), charsetName));
            
            String zeile = "";
            boolean EOF = false;
            
            do {
                zeilennr++;
                zeile = eingabe.readLine();
                if (zeile != null) quelleListe.add(zeile);
                else EOF = true;
                
            }
            while (EOF == false);
            eingabe.close();
        } catch(FileNotFoundException e) {
            Fehlermeldung = tr.tr("File")+" " + dateiname + " "+tr.tr("NotExisting")+"!" + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        } catch(IOException e) {
            Fehlermeldung = tr.tr("ErrorInFile") +" " + dateiname +  ", "+tr.tr("Line")+" " + zeilennr + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        }
        
        // Array quelle schreiben
        quelle = new String[quelleListe.size()];
        int i = 0;
        for (Iterator it = quelleListe.iterator(); it.hasNext();) {
            quelle[i] = (String) it.next();
            i++;
        }
        
        if (!FEHLER) System.out.println(tr.tr("Template") + " " + dateiname + " " + tr.tr("Read"));
        
        // Zeilenend-Zeichen feststellen "\n", "\r\n" oder "\r"
        try {
            ZipFile datei = new ZipFile(dateiname);
            ZipEntry entry = datei.getEntry(dateiimarchivname);
            InputStreamReader eingabestrom = new InputStreamReader(datei.getInputStream(entry), charsetName);
            char[] cbuf = new char[2];
            eingabestrom.skip(quelle[0].length());
            eingabestrom.read(cbuf, 0, cbuf.length);
            eingabestrom.close();
            if (cbuf[0] == '\n') NZ = "\n"; // Unix
            else if (cbuf[0] == '\r') {
                if (cbuf[1] == '\n') NZ = "\r\n"; // Windows
                else NZ = "\r"; // Mac früher
            }
            else NZ = NZsys; // wenn nichts entdeckt wird. Z.B. Einzeiler.
        }
        catch(Exception e) {
            Fehlermeldung = tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            System.err.println("Could not detect NewLine char: using system default.");
        }
    }
    
    
    private void vorlaufdateiEinlesen(String dateiname) {
        assert mitvorlauf;
        String Fehlermeldung;
        File datei = new File(dateiname);
        
        switch(verbindungstyp) {
            case yacas: // Das Einlesen (Parsen) der Vorlaufdatei yacas überlassen.
                // Testen, ob Datei vorhanden.
                if (datei.canRead()) {
                    
                    vorlauf = new String[1];
                    String absdateiname = datei.getAbsolutePath();
                    absdateiname = absdateiname.replace('\\','/');  // Yacas erwartet '/', unabhängig vom Betriebssystem
                    if (!absdateiname.startsWith("/")) absdateiname = "/" + absdateiname;
                    vorlauf[0] = "Load(\"" + absdateiname + "\");" ;
                } else  {
                    Fehlermeldung = tr.tr("File") + " " + dateiname + " "+tr.tr("NotExisting")+"!";
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    verbose = true;
                }
                break;
                
            case beanshell: // Das Einlesen (Parsen) der Vorlaufdatei beanshell überlassen.
                // Testen, ob Datei vorhanden.
                if (datei.canRead()) {
                    
                    vorlauf = new String[1];
                    String absdateiname = datei.getAbsolutePath();
                    absdateiname = absdateiname.replace('\\','/');  // Beanshell erwartet '/', unabhängig vom Betriebssystem
                    if (!absdateiname.startsWith("/")) absdateiname = "/" + absdateiname;
                    vorlauf[0] = "source(\"" + absdateiname + "\");" ;
                } else  {
                    Fehlermeldung = tr.tr("File") + " " + dateiname + " "+tr.tr("NotExisting")+"!";
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    verbose = true;
                }
                break;
                
            case port:
            default:    // Vorlaufdatei Zeile für Zeile einlesen
                LinkedList vorlaufListe = new LinkedList();
                int zeilennr = 0;
                
                try {
                    FileReader eingabestrom = new FileReader(datei);
                    BufferedReader eingabe = new BufferedReader(eingabestrom);
                    
                    String zeile = "";
                    boolean EOF = false;
                    
                    do {
                        zeilennr++;
                        zeile = eingabe.readLine();
                        if (zeile != null) {
                            // Parsen: leere und kommentierte Zeilen auslassen.
                            boolean anweisung = true;
                            StringTokenizer tokens = new StringTokenizer(zeile, " \t");
                            if (tokens.hasMoreTokens()) {
                                String erstestoken = tokens.nextToken();
                                if (erstestoken.startsWith("#") || erstestoken.startsWith("//") ||
                                        erstestoken.startsWith("/*")) {
                                    anweisung = false;
                                }
                            } else anweisung = false;
                            
                            if (anweisung) vorlaufListe.add(zeile);
                        } else EOF = true;
                    }
                    while (EOF == false);
                    eingabe.close();
                    eingabestrom.close();
                    
                } catch(FileNotFoundException e) {
                    Fehlermeldung = tr.tr("File") + " " + dateiname + " "+tr.tr("NotExisting")+"!" + 
                            NZsys + tr.tr("ErrorMessage") + ": " + e;
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    verbose = true;
                } catch(IOException e) {
                    Fehlermeldung = tr.tr("ErrorInFile")+" " + dateiname + ", "+tr.tr("Line")+" " + zeilennr + NZsys + tr.tr("ErrorMessage") + ": " + e;
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    verbose = true;
                }
                
                // Array vorlauf schreiben
                vorlauf = new String[vorlaufListe.size()];
                int i = 0;
                for (Iterator it = vorlaufListe.iterator(); it.hasNext();) {
                    vorlauf[i] = (String) it.next();
                    i++;
                }
                
                if (!FEHLER && verbose) System.out.println(tr.tr("InitFile") +" " + dateiname + " " + tr.tr("Read"));
        }
    }
    
    private void quelldateiParsen() {
        clParser parser;
        if (dollarersetzen) parser = new clParser(quelle, NZ, dollarersatz);
        else parser = new clParser(quelle, NZ);
        zielListe = parser.getZiel();
    }
    
    private void befehlliste_schreiben() {
        StringBuffer zielbf = new StringBuffer();
        clBaustein baustein;
        String aktBefehl;
        
        // Vorlaufs schreiben
        if (mitvorlauf) {
            for (int i = 0; i < vorlauf.length; i++) {
                zielbf.append(vorlauf[i]);
                zielbf.append(NZ);
            }
        }
        
        
        // Befehle aus der zielListe (der eigentlichen Quelldatei) herausschreiben
        if (htmlkonvertieren) converter = new htmlConverter("HTML");
        if (utf2asciikonvertieren) UTFconverter = new clUTF2asciiConverter();
        for (Iterator it = zielListe.iterator(); it.hasNext();) {
            baustein = (clBaustein) it.next();
            if (baustein.istBefehl()) {
                aktBefehl = baustein.getInput();
                if (htmlkonvertieren && converter.containsHTMLcharacters(aktBefehl)) {
                    aktBefehl = converter.convert(aktBefehl);
                    if (verbose) {
                        System.out.println(tr.tr("ConvertFrom") + " " + baustein.getInput());
                        System.out.println(tr.tr("ConvertTo") + " " + aktBefehl);
                    }
                }
                if (utf2asciikonvertieren && UTFconverter.containsNonAsciiCharacters(aktBefehl)) {
                    aktBefehl = UTFconverter.convert2UnicodeHex(aktBefehl);
                    if (verbose) {
                        System.out.println(tr.tr("ConvertFrom") + " " + baustein.getInput());
                        System.out.println(tr.tr("ConvertTo") + " " + aktBefehl);
                    }
                }
                
                // aktBefehl schreiben
                zielbf.append(aktBefehl);
                
                // Strichpunkt anhängen wo nötig
                if (TRAILINGSEMICOLON) {
                    if (!aktBefehl.endsWith(";")) zielbf.append(";");
                }
                
                zielbf.append(NZ);
            }
        }
        
        ziel = zielbf.toString();
    }
    
    private void zieldateiSchreiben(String dateiname) {
        boolean ZIELDATEIGESCHRIEBEN = false;
        try {
            File datei = new File(dateiname);
            OutputStreamWriter ausgabestrom = new OutputStreamWriter(new FileOutputStream(datei), charsetName);
            BufferedWriter ausgabe = new BufferedWriter(ausgabestrom);
            
            ausgabe.write(ziel);
            ausgabe.flush();
            ausgabe.close();
            ZIELDATEIGESCHRIEBEN = true;
        } catch(IOException e) {
            String Fehlermeldung = tr.tr("ErrorWritingFile") + " " + dateiname + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        }
        System.out.println("");
        if (ZIELDATEIGESCHRIEBEN) System.out.println(tr.tr("File") + " " + dateiname + " " + tr.tr("Written"));
    }
}


