/*
 * clTranslation.java
 *
 * Created on 17. Dezember 2005
 */

package mavscript.bin;

import java.util.*;

/* Copyright (c) 2005 - 2007 A.Vontobel  <qwert2003@users.berlios.de>,
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
