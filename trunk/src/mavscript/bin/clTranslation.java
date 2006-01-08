/*
 * clTranslation.java
 *
 * Created on 17. Dezember 2005
 */

package mavscript.bin;

import java.util.*;

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


/**clTranslation - Hilfsklasse für Übersetzungen
 *
 * @author av
 */
public class clTranslation {
    
    private ResourceBundle RB;
    private Locale locale;
    private String resource;
    
    
    /** Creates a new instance of clTranslation, using default locale
        @param String resource i.e. "Fachwerk/locales/gui-dialoge" */
    public clTranslation(String resource) {
        this.resource = resource;
        locale = Locale.getDefault();
        loadResource();
    }
    
    /** Creates a new instance of clTranslation   
        @param String resource i.e. "Fachwerk/locales/gui-dialoge" 
        @param Locale locale*/
    public clTranslation(String resource, Locale locale) {
        this.resource = resource;
        this.locale = locale;
        loadResource();
    }
    
    private void loadResource() {
        RB = ResourceBundle.getBundle(resource, locale);
        if (RB == null) {
            System.err.println("ERROR: Resource " + resource + " not found. Locale: " + locale.toString());
        }
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
        loadResource();
    }
    
    public String tr(String key) {        
        String übersetzt;
        try {übersetzt = RB.getString(key);}
        catch (MissingResourceException e) {
            System.err.println("Keyword " + key + " not found, locale " + locale.toString());
            System.err.println(e.toString());
            return key;
        }        
        return übersetzt;
    }
}
