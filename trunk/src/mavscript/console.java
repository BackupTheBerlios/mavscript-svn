/*
 * console.java
 *
 * Created on 31. Oktober 2004, 18:06
 */

package mavscript;

import gnu.getopt.*;
import java.io.*;
import java.util.Locale;

import mavscript.bin.clMavscript;
import mavscript.bin.clMavscriptZIP;
import mavscript.bin.inConst;
import mavscript.bin.clTranslation;
import mavscript.bin.clMavscriptExtractor;



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
 * Diese Klasse dient dazu, das Programm <code>Mavscript</code> über die Konsole aufzurufen.
 * Sie liest die übergebenen Parameter ein und startet dann die Hauptklasse
 * {@link clMavscript} resp. {@link clMavscriptZIP}
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class console implements inConst {
    
    int verbindungstyp = yacas; // default
    String quelldatei;
    String vorlaufdatei;
    String zieldatei;
    String serverAddress = "127.0.0.1";
    int serverPort = 9734; // Beispielwert
    String dateiImArchiv = "content.xml"; // in OpenDocument Text .odt
    String charset = "UTF-8";
    boolean verbose = false;
    boolean ausZIP = false;
    boolean mitvorlauf = false;
    boolean htmlkonvertieren = false;
    boolean utf2asciikonvertieren = false;
    boolean nurextrahieren = false;
    private boolean zieldateigesetzt = false;
    private boolean stdLocale = true;
    private boolean dollarersetzen = false;
    private String dollarersatz = "$"; // wird nur ausgewertet wenn dollarersetzen == true
    
    private Locale locale = Locale.getDefault();
    private final clTranslation tr = new clTranslation("mavscript/locales/console");
    
    private String[] iArgs;
    
    private final static String PGM     = "Mavscript";
    private final static String VERSION = "0.16";
    
    private final static String USAGE_de = "Gebrauch:\n" +
            "java -jar mavscript*.jar [-vHAxhV] [-l Sprache] \n" +
            "                         [-y | -b | -p Port [-s Server]] \n" +
            "                         [-z Name_in_ZIP] [-i Vorlaufdatei] \n" +
            "                         [-o Zieldatei] Vorlagedatei \n\n" +
            "-v, --verbose               Ausfuehrliche Ausgaben\n" +
            "-l, --language              Sprache (z.B. de)\n" +
            "-h, --help                  Ausgabe dieser Hilfe\n" +
            "-V, --version               Ausgabe von Versionsinformation\n" +
            "-y, --yacas                 Yacas (Standard)\n" +
            "-b, --beanshell             Beanshell\n" +
            "-p, --port Port             Port (z.B.: 9734)\n" +
            "-s, --server Server         Name des Servers (vorgegeben: 127.0.0.1)\n" +
            "-z, --name_in_zip Name      Dateiname in ZIP\n" +
            "-i  --init Datei            Liest zuerst die Anweisungen dieser Datei ein.\n" +
            "-H, --HTML                  Akzeptiert HTML-Spezialzeichen (z.B. &gt;)\n" +
            "-A, --ascii                 Kodiert Unicode-Zeichen in Ascii-Zeichenfolge.\n" +
            "-C, --charset Kodierung     Zeichenkodierung festlegen (vorgegeben UTF-8)\n" +
            "                            Beispiele: ISO-8859-1, system \n" +
            "-D, --controlchar $-Ersatz  Setzt das Anweisungszeichen (vorgegeben: $). \n" +
            "-x, --extract               Schreibt die Anweisungen in die Zieldatei. \n" +
            "                            Es wird keine Berechnung durchgefuehrt. \n" +
            "-o  --outfile Zieldatei     Name der Zieldatei (vorgegeben out.VorlagedateiName)\n" +
            "\n" +
            "OpenOffice-Writer Dateien mit der Endung .odt und .sxw werden automatisch\n" +
            "erkannt. Die Option \"-H -z content.xml\" kann daher weggelassen werden.\n"+
            "Mavscript verwendet standardmaessig Yacas. Alternativ kann Beanshell verwendet\n"+
            "werden. Ausserdem kann Mavscript ueber einen Port zu einem externen Programm\n" +
            "Verbindung aufnehmen.\n"+
            "\n" +
            "Beispiele:\n" +
            "java -jar mavscript*.jar ./vorlage.txt    Schreibt die Datei out.vorlage.txt\n" +
            "java -jar mavscript*.jar ./vorlage.odt    Schreibt die Datei out.vorlage.odt\n" +
            "java -jar mavscript*.jar --init ./StdFunktionen.ys ./vorlage.odt\n" +
            "java -jar mavscript*.jar -o ergebnis.odt ./vorlage.odt";
    
    private final static String USAGE_en = "Usage:\n" +
            "java -jar mavscript*.jar [-vHAxhV] [-l Language] \n" +
            "                         [-y | -b | -p Port [-s Server]] \n" +
            "                         [-z Name_in_ZIP] [-i InitFile] \n" +
            "                         [-o OutputFile] TemplateFile \n\n" +
            "-v, --verbose               Verbose output\n" +
            "-l, --language              Language (i.e. en)\n" +
            "-h, --help                  Usage information; this help screen\n" +
            "-V, --version               Display version\n" +
            "-y, --yacas                 Yacas (Standard)\n" +
            "-b, --beanshell             Beanshell\n" +
            "-p, --port Port             Port (i.e. 9734)\n" +
            "-s, --server Server         Server name (default: 127.0.0.1)\n" +
            "-z, --name_in_zip Name      File name within ZIP\n" +
            "-i  --init InitFile         First processes the commands of this file.\n" +
            "-H, --HTML                  Accepts HTML special characters (like &gt;)\n" +
            "-A, --ascii                 Converts unicode chars to a ascii representation.\n" +
            "-C, --charset Encoding      Charset name (default: UTF-8)\n" +
            "                            examples: ISO-8859-1, system \n" +
            "-D, --controlchar $-repl.   Sets the control character(s) (default: $). \n" +
            "-x, --extract               Writes the commands to the OutputFile. \n" +
            "                            No calculation is done. \n" +
            "-o  --outfile OutputFile    OutputFile name (default: out.InputFileName)\n" +
            "\n" +
            "OpenOffice-Writer files (suffix .odt or .sxw) are detected automatically.\n" +
            "The option \"-H -z content.xml\" therefore can be omitted.\n"+
            "The standard engine is the built-in computer-algebra-system Yacas. The built-in\n"+
            "java-interpreter BeanShell may be used instead. The third option is to connect\n" +
            "to a port (on localhost or on a remote server).\n"+
            "\n" +
            "Examples:\n" +
            "java -jar mavscript*.jar ./template.txt    Writes the file out.template.txt\n" +
            "java -jar mavscript*.jar ./template.odt    Writes the file out.template.odt\n" +
            "java -jar mavscript*.jar --init ./StdFunctions.ys ./template.odt\n" +
            "java -jar mavscript*.jar -o result.odt ./template.odt";
    
    /** Creates a new instance of console */
    public console(String[] args) {
        iArgs = args;
    }
    
    /** Zeigt die Hilfe an und beendet anschliessend das Programm.
     @param locale Sprache. Wenn nicht deutsch, wird die Hilfe auf Englisch angezeigt.
     @param exitStatus 0 wenn ordentlich beendet, sonst > 0
     */
    public void usage(Locale locale, int exitStatus) {
        if (locale.getLanguage().equals(new Locale("de").getLanguage())) {
            System.out.println(USAGE_de);
        }
        else System.out.println(USAGE_en);
        System.exit(exitStatus);
    }
    
    /////////////////////////////////////////////////////////////////////////
    
    void parseArguments() {
        LongOpt[] longopts = new LongOpt[16];
        longopts[0] = new LongOpt("help",LongOpt.NO_ARGUMENT,null,'h');
        longopts[1] = new LongOpt("verbose",LongOpt.NO_ARGUMENT,null,'v');
        longopts[2] = new LongOpt("version",LongOpt.NO_ARGUMENT,null,'V');
        longopts[3] = new LongOpt("port",LongOpt.REQUIRED_ARGUMENT,null,'p');
        longopts[4] = new LongOpt("server",LongOpt.REQUIRED_ARGUMENT,null,'s');
        longopts[5] = new LongOpt("name_in_zip",LongOpt.REQUIRED_ARGUMENT,null,'z');
        longopts[6] = new LongOpt("init",LongOpt.REQUIRED_ARGUMENT,null,'i');
        longopts[7] = new LongOpt("charset",LongOpt.REQUIRED_ARGUMENT,null,'C');
        longopts[8] = new LongOpt("outfile",LongOpt.REQUIRED_ARGUMENT,null,'o');
        longopts[9] = new LongOpt("beanshell",LongOpt.NO_ARGUMENT,null,'b');
        longopts[10] = new LongOpt("yacas",LongOpt.NO_ARGUMENT,null,'y');
        longopts[11] = new LongOpt("HTML",LongOpt.NO_ARGUMENT,null,'H');
        longopts[12] = new LongOpt("ascii",LongOpt.NO_ARGUMENT,null,'A');
        longopts[13] = new LongOpt("language",LongOpt.REQUIRED_ARGUMENT,null,'l');
        longopts[14] = new LongOpt("extract",LongOpt.NO_ARGUMENT,null,'x');
        longopts[15] = new LongOpt("controlchar",LongOpt.REQUIRED_ARGUMENT,null,'D');
        
        
        Getopt g = new Getopt(PGM,iArgs,":p:s:z:i:C:o:l:D:byvxHAhV",longopts);
        g.setOpterr(false);
        int c;
        
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'v':
                    verbose = true;
                    break;
                case 'l':
                    locale = new Locale(g.getOptarg());
                    stdLocale = false;
                    tr.setLocale(locale);
                    break;
                case 'h':
                    usage(locale, 0);
                    break;
                case 'V':
                    // Version bereits geschrieben (in main)
                    System.exit(0);
                    break; // diese Zeile wird nie erreicht.
                case 'p':
                    try {
                        serverPort = Integer.parseInt(g.getOptarg());
                        verbindungstyp = port;
                    } catch (NumberFormatException e) {
                        System.err.println(tr.tr("Port_not_an_Integer"));
                        usage(locale, 1);
                    }
                    break;
                case 's':
                    serverAddress = g.getOptarg();
                    break;
                case 'b':
                    verbindungstyp = beanshell;
                    break;
                case 'y':
                    verbindungstyp = yacas;
                    break;
                case 'H':
                    htmlkonvertieren = true;
                    break;
                case 'A':
                    utf2asciikonvertieren = true;
                    break;
                case 'z':
                    dateiImArchiv = g.getOptarg();
                    ausZIP = true;
                    break;
                case 'i':
                    vorlaufdatei = g.getOptarg();
                    mitvorlauf = true;
                    break;
                case 'C':
                    charset = g.getOptarg();
                    break;
                case 'D':
                    dollarersatz = g.getOptarg();
                    dollarersetzen = true;
                    break;
                case 'x':
                    nurextrahieren = true;
                    break;
                case 'o':
                    zieldatei = g.getOptarg();
                    zieldateigesetzt = true;
                    break;
                case ':':
                    System.err.println(tr.tr("MissingArgument") + " " + (char) g.getOptopt());
                    usage(locale, 1);
                    break;
                case '?':
                    System.err.println(tr.tr("OptionNotValid") + " " + (char) g.getOptopt());
                    usage(locale, 1);
                    break;
                default:
                    usage(locale, 1);
            }
        }
        String[] iDateien = new String[iArgs.length - g.getOptind()];
        for (int i=g.getOptind();i<iArgs.length;++i) iDateien[i-g.getOptind()] = iArgs[i];
        
        if (iDateien.length < 1) usage(locale, 1);
        quelldatei = iDateien[0];
        if (iDateien.length > 1) {
            System.out.println(tr.tr("MaxOneFile"));
        }
        if (!zieldateigesetzt) {
            File d = new File(quelldatei);
            if (d.getParent() == null) {
                zieldatei = "out." +d.getName();
            } else zieldatei = d.getParent() + System.getProperty("file.separator") + "out." +d.getName();
            if (nurextrahieren) {
                switch (verbindungstyp) {
                    case yacas:
                        zieldatei += ".ys";
                        break;
                    case beanshell:
                        zieldatei += ".bsh";
                        break;
                    case port:
                    default:
                        zieldatei += ".txt";
                }
            }
        }
    }
    
    public static void main(String[] args) {
        console c = new console(args);
        
        System.out.println("");
        System.out.println("            " + c.tr.tr("WelcomeTo") + " " + PGM);
        System.out.println("            Copyright (c) A. Vontobel, 2004-2007");
        System.out.println("            " + c.tr.tr("Version") + " " + VERSION);
        System.out.println("");
        
        boolean allesOK = false;
        c.parseArguments(); // Beendet das Prog, falls z.B. -h oder -V aufgerufen wird.
        if (c.ausZIP == false) {
            if (c.quelldatei.endsWith(".odt")) {
                System.out.println(c.tr.tr("TheFile") +" " + c.quelldatei + " "+ c.tr.tr("isODT"));
                c.ausZIP = true;
            } else if (c.quelldatei.endsWith(".sxw")) {
                System.out.println(c.tr.tr("TheFile") +" " + c.quelldatei + " "+ c.tr.tr("isSXW"));
                c.ausZIP = true;
            } else if (c.quelldatei.endsWith(".docx")) {
                System.out.println(c.tr.tr("TheFile") +" " + c.quelldatei + " "+ c.tr.tr("isDOCX"));
                c.dateiImArchiv = "word/document.xml";
                c.ausZIP = true;
            }
        }
        if (c.htmlkonvertieren == false) {
            if (c.quelldatei.endsWith(".odt") || c.quelldatei.endsWith(".sxw") 
            || c.quelldatei.endsWith(".odtx") 
            || c.quelldatei.endsWith(".html") || c.quelldatei.endsWith(".xml")) {
                c.htmlkonvertieren = true;
                if (c.verbose) System.out.println(c.tr.tr("setHTML"));
            }
        }
        
        if (c.verbose) {
            switch(c.verbindungstyp) {
                case port:
                    System.out.println(c.tr.tr("Mode") + ": Port");
                    System.out.println("Server: " + c.serverAddress + "\nPort:   " + c.serverPort);
                    break;
                case beanshell:
                    System.out.println(c.tr.tr("Mode") + ": BeanShell");
                    break;
                case yacas:
                    System.out.println(c.tr.tr("Mode") + ": Yacas");
                    break;
                default:
                    assert false;
                    System.out.println(c.tr.tr("Mode") + ": " + c.verbindungstyp);
            }
            if (c.ausZIP) {
                if (c.mitvorlauf) System.out.println(c.tr.tr("InitFile") + ": " + c.vorlaufdatei);
                System.out.print(c.tr.tr("Template") + " (zip): " + c.quelldatei);
                System.out.println(" ("+ c.tr.tr("ZippedFile")+" " + c.dateiImArchiv + ")");
                if (c.nurextrahieren) System.out.println(c.tr.tr("OutputFile") + ": " + c.zieldatei);
                else System.out.println(c.tr.tr("OutputArchive") + ":  " + c.zieldatei);
            } else {
                if (c.mitvorlauf) System.out.println(c.tr.tr("InitFile") + ": " + c.vorlaufdatei);
                System.out.println(c.tr.tr("TemplateFile") + ": " + c.quelldatei);
                System.out.println(c.tr.tr("OutputFile") + ": " + c.zieldatei);
            }
            System.out.println("");
        }
        
        if (c.nurextrahieren) {
            clMavscriptExtractor ber;
            if (c.ausZIP) ber = new clMavscriptExtractor(c.verbindungstyp, c.quelldatei, c.dateiImArchiv, c.zieldatei);
            else ber = new clMavscriptExtractor(c.verbindungstyp, c.quelldatei, c.zieldatei);
            
            ber.setVerbose(c.verbose);
            if (!c.stdLocale) ber.setLocale(c.locale);
            ber.setHTMLkonvertieren(c.htmlkonvertieren);
            ber.setUTF2asciikonvertieren(c.utf2asciikonvertieren);
            ber.setCharset(c.charset);
            if (c.dollarersetzen) ber.setDollarErsatz(c.dollarersatz);
            if (c.mitvorlauf) ber.setVorlauf(c.vorlaufdatei);
            allesOK = ber.run();            
        } 
        else { // Start
            
            if (c.ausZIP) { // Open Office Writer Datei oder sonstige Zipdatei
                clMavscriptZIP ber;
                switch(c.verbindungstyp) {
                    case port:
                        ber = new clMavscriptZIP(port, c.quelldatei, c.dateiImArchiv, c.zieldatei, c.serverAddress, c.serverPort);
                        break;
                    case beanshell:
                        ber = new clMavscriptZIP(beanshell, c.quelldatei, c.dateiImArchiv, c.zieldatei);
                        break;
                    case yacas:
                    default:
                        ber = new clMavscriptZIP(yacas, c.quelldatei, c.dateiImArchiv, c.zieldatei);
                }
                ber.setVerbose(c.verbose);
                if (!c.stdLocale) ber.setLocale(c.locale);
                ber.setHTMLkonvertieren(c.htmlkonvertieren);
                ber.setUTF2asciikonvertieren(c.utf2asciikonvertieren);
                ber.setCharset(c.charset);
                if (c.dollarersetzen) ber.setDollarErsatz(c.dollarersatz);
                if (c.mitvorlauf) ber.setVorlauf(c.vorlaufdatei);
                allesOK = ber.run();
            }
            else { // alle anderen Dateien
                clMavscript ber;
                switch(c.verbindungstyp) {
                    case port:
                        ber = new clMavscript(port, c.quelldatei, c.zieldatei, c.serverAddress, c.serverPort);
                        break;
                    case beanshell:
                        ber = new clMavscript(beanshell, c.quelldatei, c.zieldatei);
                        break;
                    case yacas:
                    default:
                        ber = new clMavscript(yacas, c.quelldatei, c.zieldatei);
                }
                ber.setVerbose(c.verbose);
                if (!c.stdLocale) ber.setLocale(c.locale);
                ber.setHTMLkonvertieren(c.htmlkonvertieren);
                ber.setUTF2asciikonvertieren(c.utf2asciikonvertieren);
                ber.setCharset(c.charset);
                if (c.dollarersetzen) ber.setDollarErsatz(c.dollarersatz);
                if (c.mitvorlauf) ber.setVorlauf(c.vorlaufdatei);
                allesOK = ber.run();
            }
        }
        
        // Ende
        if (allesOK) {
            System.out.println("");
            System.out.println(c.tr.tr("FinishedOK"));
            System.out.println("");
            System.exit(0);
        } else {
            System.out.println("");
            System.out.println(c.tr.tr("FinishedERROR"));
            System.out.println("");
            System.exit(1);
        }
    }
    
}