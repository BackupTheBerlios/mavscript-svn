/*
 * clBaustein.java
 *
 * Created on 31. Oktober 2004, 09:14
 */

package mavscript.bin;


/* Copyright (c) 2004 - 2005 A.Vontobel  <qwert2003@users.berlios.de>
 *                                       <qwert2003@users.sourceforge.net>,
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
 * Baustein des Textes. Entweder ist er ein Textabschnitt oder eine Mavscript-Anweisung.
 * 
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public class clBaustein {
    
    private boolean BEFEHL; // true: Befehl,  false: Text 
    private boolean zeigeInput = true;
    private boolean zeigeOutput = true;
    private String text = "_ERROR_";
    private String input = "_ERROR_";
    private String output = "_ERROR_";
    
    
    /** Creates a new instance of clBaustein */
    public clBaustein(boolean istBefehl) {
        BEFEHL = istBefehl;
    }
    
    public boolean istBefehl() {
        return BEFEHL;
    }
    
    public void zeigeInput(boolean zeige) {
        assert BEFEHL;
        zeigeInput = zeige;
    }
    
    public boolean zeigeInput() {
        return zeigeInput;
    }
    
    public void zeigeOutput(boolean zeige) {
        assert BEFEHL;
        zeigeOutput = zeige;
    }
    
    public boolean zeigeOutput() {
        return zeigeOutput;
    }
    
    public void setText(String text) {
        assert !BEFEHL;
        this.text = text;
    }
    
    public void setInput(String text) {
        assert BEFEHL;
        input = text;
    }
    
    public void setOutput(String text) {
        assert BEFEHL;
        output = text;
    }
    
    public String getText() {
        assert !BEFEHL;
        return text;
    }
    
    public String getInput() {
        assert BEFEHL;
        return input;
    }
    
    public String getOutput() {
        assert BEFEHL;
        return output;
    }
    
}
