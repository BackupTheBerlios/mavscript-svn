/*
 * berechne.java
 *
 * Created on 30. Oktober 2004, 17:49
 */

package mavscript.bin;

import java.io.*;
import java.util.*;
import mavscript.bin.inConst;



/* Copyright (c) 2004 - 2007 A.Vontobel  <qwert2003@users.berlios.de>,
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
 * Mavscript - Hauptklasse für Textdateien
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clMavscript implements inConst {
    
    private String quelldatei;
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
    
    // Verbindungstyp
    private int verbindungstyp = 1;
//    private final int port = 1;
//    private final int beanshell = 2;
//    private final int yacas = 3;
    
    String serverAddress = "127.0.0.1";
    int serverPort = 9734;
    String charsetName = "UTF-8";
    
    // Fall IO
    private String trennzeichenIO = " --> ";
    
    private final String NZ = System.getProperty("line.separator"); //"\n"; // neue Zeile
    private Locale locale = Locale.getDefault();
    private final clTranslation tr = new clTranslation("mavscript/locales/clMavscript");
    
    
    /** Creates a new instance of clMavscript
     * Verwendet interne Interpreter: yacas, beanshell
     */
    public clMavscript(int verbindungsTyp, String indatei, String outdatei) {
        quelldatei = indatei;
        zieldatei = outdatei;
        this.verbindungstyp = verbindungsTyp;
    }
    
    /** Creates a new instance of clMavscript
     * Verbindet sich über den Port mit z.B. Yacas
     */
    public clMavscript(int verbindungsTyp, String indatei, String outdatei, String serverAddress, int serverPort) {
        quelldatei = indatei;
        zieldatei = outdatei;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        assert verbindungsTyp == port;
        this.verbindungstyp = verbindungsTyp;
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
            String Fehlermeldung = tr.tr("InvalidCharset") +" "+ neuerCharsetName  + '\n' + tr.tr("ErrorMessage") + ": " + e
                    + '\n' + charsetName + " " + tr.tr("UsedInstead");
            System.err.println(Fehlermeldung);
        }
        if (charsetOK) charsetName = neuerCharsetName;
        else {
            String Fehlermeldung = tr.tr("InvalidCharset") +" "+ neuerCharsetName + '\n' + charsetName + " " + tr.tr("UsedInstead");
            System.err.println(Fehlermeldung);
        }
    }
    
    /** Ein Zeichen (oder eine Zeichenfolge) angeben, welche '$' in $m/$i/etc ersetzt.*/
    public void setDollarErsatz(String dollarersatz) {
        if (dollarersatz.length() < 1) {
            System.err.println("Warnung: '$' can not be replaced by " + dollarersatz);
            return;
        }
        // else
        dollarersetzen = true;
        this.dollarersatz = dollarersatz;
    }
    
    /** Startet den Programmablauf: Einlesen, Parsen, Rechnen lassen, Rückeinsetzen, Schreiben*/
    public boolean run() {
        if (!FEHLER) quelldateiEinlesen(quelldatei);
        if (mitvorlauf && !FEHLER) vorlaufdateiEinlesen(vorlaufdatei);
        if (!FEHLER) quelldateiParsen();
        if (!FEHLER) rechnenlassen();
        if (!FEHLER) rückeinsetzen();
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
            Fehlermeldung = tr.tr("File")+" " + dateiname + " "+tr.tr("NotExisting")+"!" + '\n' + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        } catch(IOException e) {
            Fehlermeldung = tr.tr("ErrorInFile") +" " + dateiname + ", "+tr.tr("Line")+" " + zeilennr + '\n' + tr.tr("ErrorMessage") + ": " + e;
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
                            '\n' + tr.tr("ErrorMessage") + ": " + e;
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    verbose = true;
                } catch(IOException e) {
                    Fehlermeldung = tr.tr("ErrorInFile")+" " + dateiname + ", "+tr.tr("Line")+" " + zeilennr + '\n' + tr.tr("ErrorMessage") + ": " + e;
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
        if (dollarersetzen) parser = new clParser(quelle, dollarersatz);
        else parser = new clParser(quelle);
        zielListe = parser.getZiel();
    }

    private void rechnenlassen() {
        clConnect verbindung;
        switch (verbindungstyp) {
            case yacas:
                verbindung = new clConnectYacas();
                verbindung.setVerbose(verbose);
                boolean aufgestartet = verbindung.connect();
                if (aufgestartet == false) {
                    System.err.println(tr.tr("ErrorConnectingYacas"));
                    FEHLER = true;
                    verbose = true;
                    return;
                }
                break;
                
            case beanshell:
                verbindung = new clConnectBeanshell();
                verbindung.setVerbose(verbose);
                
//                // Das Arbeitsverzeichnis von BeanShell soll dem Verzeichnis der Zieldatei entsprechen
//                File d = new File(zieldatei).getAbsoluteFile();
//                String ausgabeverzeichnis = d.getParent();
//                if (ausgabeverzeichnis == null) ausgabeverzeichnis = System.getProperty("user.dir");
//                ausgabeverzeichnis = ausgabeverzeichnis.replace('\\','/');  // Beanshell erwartet '/', unabhängig vom Betriebssystem
//                verbindung.exec("cd(\"" + ausgabeverzeichnis + "\");");
                
                break;
                
            case port:
            default:
                verbindung = new clConnectPort(serverAddress, serverPort);
                verbindung.setVerbose(verbose);
                boolean verbunden = verbindung.connect();
                if (verbunden == false) {
                    System.err.println(tr.tr("ErrorConnectingServer")); // Keine Verbindung zum Server
                    System.err.println(tr.tr("ErrorConnectingServerTip1")); // Ist der Server gestartet? yacas --server 9734
                    System.err.println(tr.tr("ErrorConnectingServerTip2")); // Verhindert ein Firewall die Verbindung zum Port? Test: telnet 127.0.0.1 9734
                    FEHLER = true;
                    verbose = true;
                    return;
                }
        }
        System.out.println("");
        
        String aktBefehl;
        String[] aktResultat;
        
        // Ausführen des Vorlaufs
        if (mitvorlauf) {
            for (int i = 0; i < vorlauf.length; i++) {
                aktBefehl = vorlauf[i];
                aktResultat = verbindung.exec(aktBefehl);
                if (aktResultat.length > 1) {
                    System.out.println("");
                    System.out.println(tr.tr("WarningMoreThanOneLine")); //Warnung: Das Resultat besteht aus mehreren Zeilen.
                    System.out.println(tr.tr("InputInit") + ": " + aktBefehl);
                    System.out.println(tr.tr("ResultIgnoreWarning")); //Resultat: (Falls keine Fehlermeldung, diese Warnung ignorieren.
                    for (int z = 0; z < aktResultat.length; z++) {
                        System.out.println(aktResultat[z]);
                    }
                    System.out.println("");
                }
            }
        }
        
        
        // Ausführen der zielListe (der eigentlichen Quelldatei)
        clBaustein baustein;
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
                aktResultat = verbindung.exec(aktBefehl);
                assert aktResultat != null;
                if (aktResultat.length <= 1) {
                    if (aktResultat[0].endsWith(";")) {
                        aktResultat[0] = aktResultat[0].substring(0, aktResultat[0].length()-1);
                    }
                    if (aktResultat[0].startsWith("\"") && aktResultat[0].endsWith("\"")) {
                        aktResultat[0] = aktResultat[0].substring(1, aktResultat[0].length()-1);
                    }
                    baustein.setOutput(aktResultat[0]);
                } else {
                    System.out.println("");
                    System.out.println(tr.tr("WarningMoreThanOneLine"));//Warnung: Das Resultat besteht aus mehreren Zeilen.
                    System.out.println(tr.tr("Input") + ": " + baustein.getInput());
                    System.out.println(tr.tr("ResultCheckOutfile")); //"Resultat: (Tip: Darstellung in der Ausgabe-Datei kontrollieren.)
                    for (int z = 0; z < aktResultat.length; z++) {
                        System.out.println(aktResultat[z]);
                    }
                    System.out.println("");
                    
                    String mehrzeiligesres = "";
                    for (int z = 0; z < aktResultat.length; z++) {
                        mehrzeiligesres += NZ + aktResultat[z];
                    }
                    mehrzeiligesres += NZ;
                    baustein.setOutput(mehrzeiligesres);
                }
            }
        }
        
        verbindung.stop();
    }
    
    private void rückeinsetzen() {
        StringBuffer zielbf = new StringBuffer();
        clBaustein baustein;
        String aktOutput;
        htmlConverter ampconverter1 = new htmlConverter("XMLamp1"); // Konvertierung von & in %AMP1
        htmlConverter ampconverter2 = new htmlConverter("XMLamp2"); // Konvertierung von %AMP1 in &
        if (htmlkonvertieren) converter = new htmlConverter("XML"); // Rückkonvertierung von < > "
        if (utf2asciikonvertieren) UTFconverter = new clUTF2asciiConverter();
        for (Iterator it = zielListe.iterator(); it.hasNext();) {
            baustein = (clBaustein) it.next();
            if (baustein.istBefehl()) {
                if (baustein.zeigeInput()) {
                    zielbf.append(baustein.getInput());
                    if (baustein.zeigeOutput()) zielbf.append(trennzeichenIO);
                }
                
                if (baustein.zeigeOutput()) {
                    aktOutput = baustein.getOutput();
                    if (htmlkonvertieren) {
                        boolean etwaskonvertiert = false;
                        if (ampconverter1.containsHTMLcharacters(aktOutput)) {
                            aktOutput = ampconverter1.convert(aktOutput);
                            etwaskonvertiert = true;
                        }
                        if (converter.containsHTMLcharacters(aktOutput)) {
                            aktOutput = converter.convert(aktOutput);
                            etwaskonvertiert = true;
                        }
                        if (ampconverter2.containsHTMLcharacters(aktOutput)) {
                            aktOutput = ampconverter2.convert(aktOutput);
                            etwaskonvertiert = true;
                        }
                        if (etwaskonvertiert && verbose) {
                            System.out.println(tr.tr("ConvertFrom") + " " + baustein.getOutput());
                            System.out.println(tr.tr("ConvertTo") + " " + aktOutput);
                        }
                    }
                    if (utf2asciikonvertieren && UTFconverter.containsUnicodeHexCharacters(aktOutput)) {
                        aktOutput = UTFconverter.convert2UTF(aktOutput);
                        if (verbose) {
                            System.out.println(tr.tr("ConvertFrom") + " " + baustein.getOutput());
                            System.out.println(tr.tr("ConvertTo") + " " + aktOutput);
                        }
                    }
                    zielbf.append(aktOutput);
                }
            } else {
                zielbf.append(baustein.getText());
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
            String Fehlermeldung = tr.tr("ErrorWritingFile") + " " + dateiname + NZ + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            verbose = true;
        }
        System.out.println("");
        if (ZIELDATEIGESCHRIEBEN) System.out.println(tr.tr("File") + " " + dateiname + " " + tr.tr("Written"));
    }
}


