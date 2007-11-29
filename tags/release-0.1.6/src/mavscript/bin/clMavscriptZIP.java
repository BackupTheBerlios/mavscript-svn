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
 * Mavscript - Hauptklasse für gezippte Textdateien
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clMavscriptZIP implements inConst {
    
    private String quellarchiv;
    private String dateiImArchiv; // = "content.xml";
    private String zielarchiv;
    private String vorlaufdatei;
    private boolean STDIN = false; // Lese von stdin
    private boolean STDOUT = false; // Schreibe auf stdout
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
    private boolean quiet = false;
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
    
    private final String NZsys = System.getProperty("line.separator");
    private String NZ = NZsys; //"\n"; // neue Zeile
    private Locale locale = Locale.getDefault();
    private final clTranslation tr = new clTranslation("mavscript/locales/clMavscript");
    public static final int EOF = -1; // End Of File
    
    /** Creates a new instance of clMathscriptZIP
     * dies ist die Mavscriptklasse für OpenOffice .odt Dateien
     * Verwendet interne Interpreter: yacas, beanshell
     * @param inarchiv: Quelldatei oder "_opt_stdin" (falls STDIN wird berechnetes content.xml geschrieben, nicht odt)
     * @param outarchiv: Zieldatei oder "_opt_stdout"
     */
    public clMavscriptZIP(int verbindungsTyp, String inarchiv, String dateiimarchiv, String outarchiv) {
        quellarchiv = inarchiv;
        zielarchiv = outarchiv;
        if (quellarchiv.equals("_opt_stdin")) STDIN = true;
        if (zielarchiv.equals("_opt_stdout")) STDOUT = true;
        dateiImArchiv = dateiimarchiv;
        this.verbindungstyp = verbindungsTyp;
    }
    
    /** Creates a new instance of clMathscriptZIP
     * dies ist die Mavscriptklasse für OpenOffice .odt Dateien
     * Verbindet sich über den Port mit z.B. Yacas
     * @param inarchiv: Quelldatei oder "_opt_stdin" (falls STDIN wird berechnetes content.xml geschrieben, nicht odt)
     * @param outarchiv: Zieldatei oder "_opt_stdout"
     */
    public clMavscriptZIP(int verbindungsTyp, String inarchiv, String dateiimarchiv, String outarchiv, String serverAddress, int serverPort) {
        quellarchiv = inarchiv;
        zielarchiv = outarchiv;
        if (quellarchiv.equals("_opt_stdin")) STDIN = true;
        if (zielarchiv.equals("_opt_stdout")) STDOUT = true;
        dateiImArchiv = dateiimarchiv;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        assert verbindungsTyp == port;
        this.verbindungstyp = verbindungsTyp;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setQuiet(boolean quiet) {
        if (quiet) verbose = false;
        this.quiet = quiet;
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
        if (!FEHLER) quellarchivEinlesen(quellarchiv, dateiImArchiv);
        if (mitvorlauf && !FEHLER) vorlaufdateiEinlesen(vorlaufdatei);
        if (!FEHLER) quelldateiParsen();
        if (!FEHLER) rechnenlassen();
        if (!FEHLER) rückeinsetzen();
        if (!FEHLER) {
        // Falls Input von stdin --> nicht das Archiv (odt) wird ausgegeben, sondern die berechnete DateiImArchiv
            if (STDIN) zieldateiSchreiben(zielarchiv);
            else zielarchivSchreiben(); // normal zielarchiv (odt) schreiben
        }
        return !FEHLER;
    }
    
    private void quellarchivEinlesen(String dateiname, String dateiimarchivname) {
        LinkedList quelleListe = new LinkedList();
        String Fehlermeldung;
        int zeilennr = 0;
        try {
            InputStreamReader eingabestrom;
            ZipEntry entry;
            if (STDIN) {
                ZipInputStream zipeingabestrom = new ZipInputStream(System.in); // Lese von Stdin
                boolean dateiimarchivGEFUNDEN = false;
                while (zipeingabestrom.available() > 0) {
                    entry = zipeingabestrom.getNextEntry();
                    String aktDateiinZip = entry.getName();
                    if (aktDateiinZip.equals(dateiimarchivname)) {
                        dateiimarchivGEFUNDEN = true;
                        break;
                    }
                }
                if (!dateiimarchivGEFUNDEN) throw new ZipException(dateiimarchivname + " not found."); // TODO übersetzen
                eingabestrom = new InputStreamReader(zipeingabestrom, charsetName);
            }
            else {
                ZipFile datei = new ZipFile(dateiname);
                entry = datei.getEntry(dateiimarchivname);
                eingabestrom = new InputStreamReader(datei.getInputStream(entry), charsetName);
            }
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
        } catch(FileNotFoundException e) {
            Fehlermeldung = tr.tr("File")+" " + dateiname + " "+tr.tr("NotExisting")+"!" + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            if (!quiet) verbose = true;
        } catch(IOException e) {
            Fehlermeldung = tr.tr("ErrorInFile") +" " + dateiname +  ", "+tr.tr("Line")+" " + zeilennr + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            if (!quiet) verbose = true;
        }
        
        // Array quelle schreiben
        quelle = new String[quelleListe.size()];
        int i = 0;
        for (Iterator it = quelleListe.iterator(); it.hasNext();) {
            quelle[i] = (String) it.next();
            i++;
        }
        
        if (!STDIN && !FEHLER && !quiet) System.out.println(tr.tr("Template") + " " + dateiname + " " + tr.tr("Read"));
        
        // Zeilenend-Zeichen feststellen "\n", "\r\n" oder "\r"
        if (STDIN) NZ = NZsys;
        else try {
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
                    if (!quiet) verbose = true;
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
                    if (!quiet) verbose = true;
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
                    if (!quiet) verbose = true;
                } catch(IOException e) {
                    Fehlermeldung = tr.tr("ErrorInFile")+" " + dateiname + ", "+tr.tr("Line")+" " + zeilennr + NZsys + tr.tr("ErrorMessage") + ": " + e;
                    System.err.println(Fehlermeldung);
                    FEHLER = true;
                    if (!quiet) verbose = true;
                }
                
                // Array quelle schreiben
                vorlauf = new String[vorlaufListe.size()];
                int i = 0;
                for (Iterator it = vorlaufListe.iterator(); it.hasNext();) {
                    vorlauf[i] = (String) it.next();
                    i++;
                }
                
                if (!FEHLER  && !quiet && verbose) System.out.println(tr.tr("InitFile") +" " + dateiname + " " + tr.tr("Read"));
        }
    }
    
    private void quelldateiParsen() {
        clParser parser;
        if (dollarersetzen) parser = new clParser(quelle, NZ, dollarersatz);
        else parser = new clParser(quelle, NZ);
        zielListe = parser.getZiel();
    }
    
    private void rechnenlassen() {
        clConnect verbindung;
        switch (verbindungstyp) {
            case yacas:
                verbindung = new clConnectYacas();
                verbindung.setVerbose(verbose);
                verbindung.setQuiet(quiet);
                boolean aufgestartet = verbindung.connect();
                if (aufgestartet == false) {
                    System.err.println(tr.tr("ErrorConnectingYacas"));
                    FEHLER = true;
                    if (!quiet) verbose = true;
                    return;
                }                
                break;
            
            case beanshell:
                verbindung = new clConnectBeanshell();
                verbindung.setVerbose(verbose);
                verbindung.setQuiet(quiet);
                break;
                
            case port:
            default:
                verbindung = new clConnectPort(serverAddress, serverPort);
                verbindung.setVerbose(verbose);
                verbindung.setQuiet(quiet);
                boolean verbunden = verbindung.connect();
                if (verbunden == false) {
                    System.err.println(tr.tr("ErrorConnectingServer")); // Keine Verbindung zum Server
                    System.err.println(tr.tr("ErrorConnectingServerTip1")); // Ist der Server gestartet? yacas --server 9734
                    System.err.println(tr.tr("ErrorConnectingServerTip2")); // Verhindert ein Firewall die Verbindung zum Port? Test: telnet 127.0.0.1 9734
                    FEHLER = true;
                    if (!quiet) verbose = true;
                    return;
                }
        }
        if (!quiet) System.out.println("");
        
        String aktBefehl;
        String[] aktResultat;
        
        // Ausführen des Vorlaufs
        if (mitvorlauf) {
            for (int i = 0; i < vorlauf.length; i++) {
                aktBefehl = vorlauf[i];
                aktResultat = verbindung.exec(aktBefehl);
                if (aktResultat.length > 1 && !quiet) {
                    System.out.println("");
                    System.out.println(tr.tr("WarningMoreThanOneLine")); //Warnung: Das Resultat besteht aus mehreren Zeilen.
                    System.out.println(tr.tr("InputInit") + ": " + aktBefehl);
                    System.out.println(tr.tr("ResultIgnoreWarning")); //Resultat: (Falls keine Fehlermeldung, diese Warnung ignorieren.
                    for (int z = 0; z < aktResultat.length; z++) {
                        System.out.println(aktResultat[z]);
                    }
                    if (!quiet) System.out.println("");
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
                        assert !quiet;
                        System.out.println(tr.tr("ConvertFrom") + " " + baustein.getInput());
                        System.out.println(tr.tr("ConvertTo") + " " + aktBefehl);
                    }
                }
                if (utf2asciikonvertieren && UTFconverter.containsNonAsciiCharacters(aktBefehl)) {
                    aktBefehl = UTFconverter.convert2UnicodeHex(aktBefehl);
                    if (verbose) {
                        assert !quiet;
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
                    if (!quiet) {
                        System.out.println("");
                        System.out.println(tr.tr("WarningMoreThanOneLine"));//Warnung: Das Resultat besteht aus mehreren Zeilen.
                        System.out.println(tr.tr("Input") + ": " + baustein.getInput());
                        System.out.println(tr.tr("ResultCheckOutfile")); //"Resultat: (Tip: Darstellung in der Ausgabe-Datei kontrollieren.)
                        for (int z = 0; z < aktResultat.length; z++) {
                            System.out.println(aktResultat[z]);
                        }
                        System.out.println("");
                    }
                    
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
                            assert !quiet;
                            System.out.println(tr.tr("ConvertFrom") + " " + baustein.getOutput());
                            System.out.println(tr.tr("ConvertTo") + " " + aktOutput);
                        }
                    }
                    if (utf2asciikonvertieren && UTFconverter.containsUnicodeHexCharacters(aktOutput)) {
                        aktOutput = UTFconverter.convert2UTF(aktOutput);
                        if (verbose) {
                            assert !quiet;
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
    
    /** Schreibt die aus dem Archiv (odt/zip) extrahierte und berechnete Datei (z.B. content.xml) in eine Datei oder nach stdout.*/
    private void zieldateiSchreiben(String dateiname) {
        boolean ZIELDATEIGESCHRIEBEN = false;
        try {
            OutputStreamWriter ausgabestrom;
            if (STDOUT) ausgabestrom = new OutputStreamWriter(System.out); // Schreibe stdout
            else ausgabestrom = new OutputStreamWriter(new FileOutputStream(new File(dateiname)), charsetName);
            BufferedWriter ausgabe = new BufferedWriter(ausgabestrom);
            
            ausgabe.write(ziel);
            ausgabe.flush();
            ausgabe.close();
            ZIELDATEIGESCHRIEBEN = true;
        } catch(IOException e) {
            String Fehlermeldung = tr.tr("ErrorWritingFile") + " " + dateiname + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            if (!quiet) verbose = true;
        }
        if (!quiet) {
            System.out.println("");
            if (ZIELDATEIGESCHRIEBEN && !STDOUT) System.out.println(tr.tr("File") + " " + dateiname + " " + tr.tr("Written"));
        }
    }
    
    /** Schreibt das Zielarchiv. Z.B. out.datei.odt oder out.datei.zip */
    private void zielarchivSchreiben() {
        boolean ZIELDATEIGESCHRIEBEN = false;
        String aktDateiinZip = "";
        try {
            ZipOutputStream zipStrom;
            if (STDOUT) zipStrom = new ZipOutputStream(System.out); // Schreibe stdout
            else zipStrom = new ZipOutputStream(new FileOutputStream(zielarchiv));
            zipStrom.setMethod(ZipOutputStream.DEFLATED);
            
            // kopieren vom Quellarchiv
            ZipFile quellzip = new ZipFile(quellarchiv);
            for (Enumeration e = quellzip.entries(); e.hasMoreElements();) {
                ZipEntry quellentry = (ZipEntry) e.nextElement();
                aktDateiinZip = quellentry.getName();
                if (aktDateiinZip.equals(dateiImArchiv)) continue;
                // kopieren
                InputStream instream = quellzip.getInputStream(quellentry);
                zipStrom.putNextEntry(new ZipEntry(quellentry.getName()));
                OutputStream outstream = zipStrom;
                
                byte  buffer[] = new byte[0xffff];  // 64KB Puffer
                int   nbytes;
                while ( (nbytes = instream.read(buffer)) != EOF )
                    outstream.write( buffer, 0, nbytes );
                
            }
            
            // transformierte Datei ins Archiv schreiben
            aktDateiinZip = dateiImArchiv;
            ZipEntry entry = new ZipEntry(dateiImArchiv);
            zipStrom.putNextEntry(entry);
            BufferedWriter ausgabe = new BufferedWriter(new OutputStreamWriter(zipStrom, charsetName));
            ausgabe.write(ziel);
            ausgabe.flush();
            
            zipStrom.closeEntry();
            zipStrom.close();
            ZIELDATEIGESCHRIEBEN = true;
        } catch(IOException e) {
            String Fehlermeldung = tr.tr("ErrorWritingFile") + " " + aktDateiinZip + " "+tr.tr("ToArchive")+" " + zielarchiv
                    + NZsys + tr.tr("ErrorMessage") + ": " + e;
            System.err.println(Fehlermeldung);
            FEHLER = true;
            if (!quiet) verbose = true;
        }
        if (!quiet) {
            System.out.println("");
            if (ZIELDATEIGESCHRIEBEN && !STDOUT) System.out.println(tr.tr("File") + " " + zielarchiv + " " + tr.tr("Written"));
        }
    }
}


