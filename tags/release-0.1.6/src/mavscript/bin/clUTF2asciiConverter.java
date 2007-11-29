/*
 * clUTF2asciiConverter.java
 *
 * Created on 1. Januar 2007
 *
 */

package mavscript.bin;


/* Copyright (c) 2007 A.Vontobel  <qwert2003@users.berlios.de>,
 *                                <qwert2003@users.sourceforge.net>
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
 * Wandelt Nicht-Ascii Zeichen (ä,é,ω) in Unicode-Hex-kodierte Darstellung (escu00e4,escu00e9,escu03c9) um.
 * Und umgekehrt
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clUTF2asciiConverter {
    
    private String escape = "esc";
    
    /**
     * Creates a new instance of clUTF2asciiConverter
     * Als escape-Zeichenfolge wird "esc" verwendet.
     */
    public clUTF2asciiConverter() {
    }
    
    /**
     * Creates a new instance of clUTF2asciiConverter
     * @param escape Zeichenfolge für Escape (Backslash kann i.d.R. nicht benutzt werden).
     */
    public clUTF2asciiConverter(String escape) {
        this.escape = escape;
    }
    
    /** Gibt an, ob ein nicht Zeichen ausserhalb des Ascii-Zeichensatzes vorhanden ist. Z.B. ä,é,ω */
    public boolean containsNonAsciiCharacters(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((int)c > 127) return true;
        }
        return false;
    }
    
    
    /** Gibt an, ob eine Unicode-Hex Zeichenfolge vorhanden ist. Z.B. escu00e4,escu00e9,escu03c9 */
    public boolean containsUnicodeHexCharacters(String text) {
        int escL = escape.length(); // Nb of chars of escape. Default "esc" : 3
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i + escL + 5 <= text.length()
            && text.substring(i, i+escL).equals(escape)
            && text.charAt(i + escL) == 'u') {
                String hex = text.substring(i + escL + 1, i + escL + 5);
                assert hex.length() == 4;
                try {
                    int num = Integer.parseInt(hex, 16);
                    return true;
                } catch (NumberFormatException nume) { // not 4 hex digits following "escu"
                    // z.B. "escu45/3" ist keine Unicode-Hex Zeichenfolge
                }
            }
        }
        return false;
    }
    
    /** Wandelt Nicht-Ascii Zeichen (ä,é,ω) in Unicode-Hex-kodierte Darstellung (escu00e4,escu00e9,escu03c9) um. */
    public String convert2UnicodeHex(String text) {
        if (text == null) {
            assert false;
            return null;
        }
        int escL = escape.length(); // Nb of chars of escape. Default "esc" : 3
        try {
            StringBuffer sb = new StringBuffer(text.length() + 80);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if ((int)c <= 127) sb.append(c);
                else {
                    sb.append(escape);
                    sb.append("u");
                    if ((int)c <= 0xff)
                        sb.append("00");
                    else if ((int)c <= 0xfff)
                        sb.append("0");
                    sb.append(Integer.toHexString((int) c));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[clUTF2asciiConverter] Error converting the text to UnicodeHex: " + text);
            System.err.println(e.getMessage());
            return text;
        }
    }
    
    // Die Konvertierung ist stellenweise von GNU Classpath Native2ASCII.java (CVS 1.1.2007) übernommen.
    
    /** Wandelt UnicodeHex-kodierte Zeichen (escu00e4,escu00e9,escu03c9) in Nicht-Ascii Zeichen (ä,é,ω) (escu00e4,escu00e9,escu03c9) um. */
    public String convert2UTF(String text) {
        if (text == null) {
            assert false;
            return null;
        }
        int escL = escape.length(); // Nb of chars of escape. Default "esc" : 3
        try {
            StringBuffer sb = new StringBuffer(text.length() + 80);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (i + escL + 5 <= text.length()
                        && text.substring(i, i+escL).equals(escape)
                        && text.charAt(i + escL) == 'u') {
                    String hex = text.substring(i + escL + 1, i + escL + 5);
                    assert hex.length() == 4;
                    try {
                        int num = Integer.parseInt(hex, 16);
                        sb.append((char) num);
                        i += escape.length() + 4;
                    } catch (NumberFormatException nume) { // not 4 hex digits following "escu"
                        sb.append(c);
                    }
                }
                else sb.append(c);
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[clUTF2asciiConverter] Error converting the text to UTF: " + text);
            System.err.println(e.getMessage());
            return text;
        }
    }
    
    
    /** nur zu Testzwecken */
    public static void main(String[] args) {
        String text = "Zeile enthält NichtAsciiZeichen: ä,é,ω. Π := Pi;";        
        if (args.length > 0) text = args[0];
        
        String konvertierterText;
        clUTF2asciiConverter converter = new clUTF2asciiConverter();
        
        System.out.println("");
        System.out.println("zu untersuchender Text:");
        System.out.println(text);
        System.out.println("");
        
        if (converter.containsNonAsciiCharacters(text)) {
            konvertierterText = converter.convert2UnicodeHex(text);
            System.out.println("konvertierter Text:");
            System.out.println(konvertierterText);
        } else {
            System.out.println("Der untersuchte Text enthaelt nur Ascii-Zeichen.");
            konvertierterText = text;
        }
        
        System.out.println("------------------");
        System.out.println("Rueckkonvertierung in Unicode-Zeichen");
        System.out.println("zu untersuchender Text: ");
        System.out.println(konvertierterText);
        System.out.println("");
        converter = new clUTF2asciiConverter();
        if (converter.containsUnicodeHexCharacters(konvertierterText)) {
            String rueckkonvertierterText = converter.convert2UTF(konvertierterText);
            System.out.println("rueckkonvertierter Text:");
            System.out.println(rueckkonvertierterText);
        } else {
            System.out.println("Der untersuchte Text enthaelt keine Zeichen in UnicodeHex-Darstellung.");
        }
        
        System.exit(0);
    }
    
}
