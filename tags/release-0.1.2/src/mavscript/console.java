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
    String dateiImArchiv = "content.xml";
    String charset = "UTF-8";
    boolean verbose = false;
    boolean ausZIP = false;
    boolean mitvorlauf = false;
    boolean htmlkonvertieren = false;
    private boolean zieldateigesetzt = false;
    private boolean stdLocale = true;
    
    private Locale locale = Locale.getDefault();
    private final clTranslation tr = new clTranslation("mavscript/locales/console");
    
    private String[] iArgs;
    
    private final static String PGM     = "Mavscript";
    private final static String VERSION = "0.12";
    
    private final static String USAGE_de = "Gebrauch:\n" +
            "java -jar mavscript*.jar [-vHhV] [-l Sprache] \n" +
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
            "-C, --charset Kodierung     Zeichenkodierung festlegen (vorgegeben UTF-8)\n" +
            "                            Beispiele: ISO-8859-1, system \n" +
            "-o  --outfile Zieldatei     Name der Zieldatei (vorgegeben out.VorlagedateiName)\n" +
            "\n" +
            "OpenOffice-Writer Dateien mit der Endung .sxw und .odt werden automatisch\n" +
            "erkannt. Die Option \"-H -z content.xml\" kann daher weggelassen werden.\n"+
            "Mavscript verwendet standardmaessig Yacas. Alternativ kann Beanshell verwendet\n"+
            "werden. \n" +
            "Mavscript kann auch ueber einen Port zu einem externen Programm Verbindung aufnehmen.\n"+
            "\n" +
            "Beispiele:\n" +
            "java -jar mavscript*.jar ./vorlage.txt    Schreibt die Datei out.vorlage.txt\n" +
            "java -jar mavscript*.jar ./vorlage.odt    Schreibt die Datei out.vorlage.odt\n" +
            "java -jar mavscript*.jar --init ./StdFunktionen.txt ./vorlage.odt\n" +
            "java -jar mavscript*.jar -o ergebnis.sxw ./vorlage.sxw";
    
    private final static String USAGE_en = "Usage:\n" +
            "java -jar mavscript*.jar [-vHhV] [-l Language] \n" +
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
            "-C, --charset Encoding      Charset name (default: UTF-8)\n" +
            "                            examples: ISO-8859-1, system \n" +
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
            "java -jar mavscript*.jar --init ./StdFunctions.txt ./template.odt\n" +
            "java -jar mavscript*.jar -o result.sxw ./template.sxw";
    
    /** Creates a new instance of console */
    public console(String[] args) {
        iArgs = args;
    }
    
    public void usage(Locale locale) {
        if (locale.getLanguage().equals(new Locale("de").getLanguage())) {
            System.out.println(USAGE_de);
        }
        else System.out.println(USAGE_en);
        System.exit(1);
    }
    
    /////////////////////////////////////////////////////////////////////////
    
    void parseArguments() {
        LongOpt[] longopts = new LongOpt[13];
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
        longopts[12] = new LongOpt("language",LongOpt.REQUIRED_ARGUMENT,null,'l');
        
        
        Getopt g = new Getopt(PGM,iArgs,":p:s:z:i:C:o:l:byvHhV",longopts);
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
                    usage(locale);
                    break;
                case 'V':
                    System.out.println(PGM + " " + tr.tr("Version") + " " + VERSION + "\n");
                    break;
                case 'p':
                    try {
                        serverPort = Integer.parseInt(g.getOptarg());
                        verbindungstyp = port;
                    } catch (NumberFormatException e) {
                        System.err.println(tr.tr("Port_not_an_Integer"));
                        usage(locale);
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
                case 'o':
                    zieldatei = g.getOptarg();
                    zieldateigesetzt = true;
                    break;
                case ':':
                    System.err.println(tr.tr("MissingArgument") + " " + (char) g.getOptopt());
                    usage(locale);
                    break;
                case '?':
                    System.err.println(tr.tr("OptionNotValid") + " " + (char) g.getOptopt());
                    usage(locale);
                    break;
                default:
                    usage(locale);
            }
        }
        String[] iDateien = new String[iArgs.length - g.getOptind()];
        for (int i=g.getOptind();i<iArgs.length;++i) iDateien[i-g.getOptind()] = iArgs[i];
        
        if (iDateien.length < 1) usage(locale);
        quelldatei = iDateien[0];
        if (iDateien.length > 1) {
            System.out.println(tr.tr("MaxOneFile"));
        }
        if (!zieldateigesetzt) {
            File d = new File(quelldatei);
            if (d.getParent() == null) {
                zieldatei = "out." +d.getName();
            } else
                zieldatei = d.getParent() + System.getProperty("file.separator") + "out." +d.getName();
        }
    }
    
    public static void main(String[] args) {
        console c = new console(args);
        
        System.out.println("");
        System.out.println("            " + c.tr.tr("WelcomeTo") + " " + PGM);
        System.out.println("            Copyright (c) A. Vontobel, 2004-2005");
        System.out.println("            " + c.tr.tr("Version") + " " + VERSION);
        System.out.println("");
        
        boolean allesOK = false;
        c.parseArguments();
        if (c.ausZIP == false) {
            if (c.quelldatei.endsWith(".odt")) {
                System.out.println(c.tr.tr("TheFile") +" " + c.quelldatei + " "+ c.tr.tr("isODT"));
                c.ausZIP = true;
            } else if (c.quelldatei.endsWith(".sxw")) {
                System.out.println(c.tr.tr("TheFile") +" " + c.quelldatei + " "+ c.tr.tr("isSXW"));
                c.ausZIP = true;
            }
        }
        if (c.htmlkonvertieren == false) {
            if (c.quelldatei.endsWith(".odt") || c.quelldatei.endsWith(".sxw") || c.quelldatei.endsWith(".html")) {
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
                System.out.println(c.tr.tr("OutputArchive") + ":  " + c.zieldatei);
            } else {
                if (c.mitvorlauf) System.out.println(c.tr.tr("InitFile") + ": " + c.vorlaufdatei);
                System.out.println(c.tr.tr("TemplateFile") + ": " + c.quelldatei);
                System.out.println(c.tr.tr("OutputFile") + ": " + c.zieldatei);
            }
            System.out.println("");
        }
        
        // Start
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
            ber.setCharset(c.charset);
            if (c.mitvorlauf) ber.setVorlauf(c.vorlaufdatei);
            allesOK = ber.run();
        } else { // alle anderen Dateien
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
            ber.setCharset(c.charset);
            if (c.mitvorlauf) ber.setVorlauf(c.vorlaufdatei);
            allesOK = ber.run();
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