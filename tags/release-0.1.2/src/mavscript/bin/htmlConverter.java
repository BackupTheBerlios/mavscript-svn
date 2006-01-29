/*
 * htmlConverter.java
 *
 * Created on 14. August 2005, 10:22
 *
 */

package mavscript.bin;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/* Copyright (c) 2005, 2006 A.Vontobel  <qwert2003@users.berlios.de>,
 *                                      <qwert2003@users.sourceforge.net>
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
 * Wandelt &gt; $lt; $quot; etc. in die Zeichen > < " etc. um.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class htmlConverter {
    
    private ResourceBundle wertepaar;
    int maxIterations = 20000; // A bad file XML.properties could lead to an endless loop.
    
    /**
     * Creates a new instance of htmlConverter
     */
    public htmlConverter(String nameResourceBundle) {
        String datei = "mavscript/util/" + nameResourceBundle; // HTML.properties
        try {wertepaar = ResourceBundle.getBundle(datei);} catch (MissingResourceException e) {
            System.err.println(e.toString());
            System.err.println("Fehler: "+nameResourceBundle+"HTML.properties nicht gefunden.");
        }
        assert wertepaar != null;
    }
    
    public boolean containsHTMLcharacters(String text) {
        for (Enumeration en = wertepaar.getKeys(); en.hasMoreElements(); ) {
            String specialCharacter = (String) en.nextElement();
            if (text.indexOf(specialCharacter) >= 0) return true;
        }
        return false;
    }
    
    public String convert(String text) {
        int index = -1;
        for (Enumeration en = wertepaar.getKeys(); en.hasMoreElements(); ) {
            String specialCharacter = (String) en.nextElement();
            index = text.indexOf(specialCharacter);
            
            if (index >= 0) {
                int kontrollsumme;
                int alte_kontrollsumme = text.hashCode();
                
                text = replaceAll(text, specialCharacter, wertepaar.getString(specialCharacter));
                kontrollsumme = text.hashCode();
                if (kontrollsumme == alte_kontrollsumme) {
                    System.err.println("Fehler: Das HTML-Spezialzeichen " + specialCharacter + " konnte nicht ersetzt werden in:");
                    System.err.println(text);
                    System.err.println("");
                }
                else assert text.indexOf(specialCharacter) < 0;
                
            }
        }
        return text;
    }
    
    public String replaceAll(String text, String oldsequence, String newsequence) {
        int index = text.indexOf(oldsequence);
        if (index < 0) return text; // nothing to replace
        
        int checksum;
        int old_checksum = text.hashCode();
        int count = 0;
        while (index >= 0) {
            text = text.substring(0, index) + newsequence + text.substring(index + oldsequence.length());            
            
            checksum = text.hashCode();
            if (checksum == old_checksum) {
                System.err.println("Error: could not replace \"" + oldsequence + "\" by  \"" + newsequence + "\" in:");
                System.err.println(text);
                System.err.println("");
                index = -2; // Schlaufe unterbrechen
            }
            index = text.indexOf(oldsequence);
            count++;
            if (count > maxIterations) {
                index = -3; // Schlaufe abbrechen
                System.err.println("Too much replacements, might be endless loop! " + count);
                System.err.println("while replacing " + oldsequence + " by " + newsequence);
            }
        }
        return text;
    }
    
    // nur zu Testzwecken
    public static void main(String[] args) {
        String text = "&gt; &lt; &amp; &quot; &apos; If (a&gt;b, x:=1, x:=0)";        
        if (args.length > 0) text = args[0];
        
        String konvertierterText;
        htmlConverter converter = new htmlConverter("HTML");
        
        System.out.println("");
        System.out.println("zu untersuchender Text:");
        System.out.println(text);
        System.out.println("");
        
        if (converter.containsHTMLcharacters(text)) {
            konvertierterText = converter.convert(text);
            System.out.println("konvertierter Text:");
            System.out.println(konvertierterText);
        } else {
            System.out.println("Der untersuchte Text enthält keine HTML-Spezialzeichen.");
            konvertierterText = text;
        }
        
        System.out.println("------------------");
        System.out.println("Rueckkonvertierung der XML-Steuerzeichen");
        System.out.println("zu untersuchender Text: ");
        System.out.println(konvertierterText);
        System.out.println("");
        converter = new htmlConverter("XML");
        if (converter.containsHTMLcharacters(konvertierterText)) {
            String rueckkonvertierterText = converter.convert(konvertierterText);
            System.out.println("rueckkonvertierter Text:");
            System.out.println(rueckkonvertierterText);
        } else {
            System.out.println("Der untersuchte Text enthält keine HTML-Spezialzeichen.");
        }
        
        System.exit(0);
    }
    
}
