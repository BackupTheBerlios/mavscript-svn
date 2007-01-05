/*
 * clConnectPort.java
 *
 * Created on 28. Oktober 2004, 19:02
 */

package mavscript.bin;

import java.net.*;
import java.io.*;
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
 * Stellt über einen Port die Verbindung zu <code> yacas </code> her und
 * führt die Anfragen durch.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clConnectPort extends clConnect {
    
    private Socket client;
    private String serverAddress;
    private int serverPort;
    private boolean verbose = false;
    private final clTranslation tr = new clTranslation("mavscript/locales/clConnect");
    
    /**
     * Creates a new instance of clConnectPort
     * @args Serveradresse, Port Nr.
     */
    public clConnectPort(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    /**
     * Creates a new instance of clConnectPort
     *  Standardwerte: "127.0.0.1", 9734
     */
    public clConnectPort() {
        serverAddress = "127.0.0.1";
        serverPort = 9734;
    }
    
    public boolean connect() {
        //System.out.println("Connecting to the server at address "+serverAddress+" on port "+serverPort);
        System.out.println(tr.tr("ConnectingAtAddress")+" "+serverAddress+" "+tr.tr("OnPort")+" "+serverPort);
        
        try {
            client = new Socket(serverAddress,serverPort);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        
        if (client == null) {
            System.out.println(tr.tr("NotConnected"));
            return false;
        } else {
            System.out.println(tr.tr("Connected"));
            return true;
        }
    }
    
    /** nur zu Testzwecken*/
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; //"localhost"; // "127.0.0.1"
        int serverPort = 9734;
        if (args.length == 2) {
            serverAddress = args[0];
            serverPort = Integer.parseInt(args[1]);
        }
        clConnectPort verbindung = new clConnectPort(serverAddress, serverPort);
        verbindung.setVerbose(true);
        
        String[] befehle = new String[4];
        befehle[0] = "a := 5;";
        befehle[1] = "b := 3*a";
        befehle[2] = "a/b";
        befehle[3] = "c";
        
        if (verbindung.connect()) {
            verbindung.exec(befehle);
        } else System.out.println("keine Verbindung zum Server");
    }
    
    public String[][] exec(String[] befehle) {
        String[][] antworten = new String[befehle.length][];
        for (int i = 0; i < befehle.length; i++) {
            antworten[i] = PerformRequest(befehle[i]);
            if (verbose) {
                System.out.println("# " + i + ": " + befehle[i]);
                for (int j = 0; j < antworten[i].length; j++) {
                    System.out.println("@ " + antworten[i][j]);
                }
            } 
            else for (int j = 0; j < antworten[i].length; j++) {System.out.print("#");}
        }
        
        return antworten;
    }
    
    public String[] exec(String befehl) {
        String[] antwort = PerformRequest(befehl);
        System.out.print("#");
        if (verbose) {
            System.out.println(" " + befehl);
            for (int j = 0; j < antwort.length; j++) {
                System.out.println("@ " + antwort[j]);
            }
        }
        return antwort;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    
    // ----------------------------------------------------------------------------------------
    private String[] PerformRequest(String inputLine) {
        boolean succeed = false;
        LinkedList antwortliste = new LinkedList();
        String[] antwort = null;
        
        try {
            BufferedOutputStream buffered = new BufferedOutputStream(client.getOutputStream());
            DataOutputStream outbound = new DataOutputStream(buffered);
            //DataInputStream inbound = new DataInputStream(client.getInputStream()); // uses a deprecated API
            BufferedReader inbound = new BufferedReader(new InputStreamReader(client.getInputStream()));            
            outbound.writeBytes(inputLine+";"); // hängt Strichpunkt an. POTENTIELLES PROBLEM für Zusammenarbeit mit externen Programmen
            outbound.flush();
            String responseLine;
            
            // FIXME Blockierung des Programms durch inbound.readline() verhindern
            while ((responseLine = inbound.readLine()) != null) {
                if (responseLine.length()>0)
                    if (responseLine.charAt(0) == ']') break;
                antwortliste.add(responseLine);
            }
            
            while ((responseLine = inbound.readLine()) != null) {
                if (responseLine.length()>0)
                    if (responseLine.charAt(0) == ']') break;
                antwortliste.add(responseLine);
            }
            
            antwort = new String[antwortliste.size()];
            int i = 0;
            for (Iterator it = antwortliste.iterator(); it.hasNext();) {
                antwort[i] = (String) it.next();
                i++;
            }
            
            succeed = true;
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (!succeed) {
                try {
                    client.close();
                } catch (Exception e) {
                    System.err.println(e);
                }
                client=null;
                System.err.println(tr.tr("RequestFailed"));
            }
        }
        
        return antwort;
        
    }
    
    public void stop() {
        try {
            client.close();
        } catch (Exception e) {
        }
        client = null;
    }
    
    
}
