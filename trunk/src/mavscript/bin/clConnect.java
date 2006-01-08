/*
 * clConnect.java
 *
 * Created on 2005
 */

package mavscript.bin;

import java.util.*;


/* Copyright (c) 2005 A.Vontobel  <qwert2003@users.berlios.de>
 *                                <qwert2003@users.sourceforge.net>,
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
 * Stellt über einen Port die Verbindung zu einem Interpreter (i.d.R. Computer-Algebra-System) 
 * her und führt die Anfragen durch.
 *
 *
 * @author  A.Vontobel <qwert2003@users.berlios.de>
 */
public abstract class clConnect {
    
    
    
    /** Creates a new instance of clConnect
     */
    public clConnect() {
    }
    
    public abstract boolean connect();
    public abstract String[][] exec(String[] befehle) ;    
    public abstract String[] exec(String befehl) ;    
    public abstract void setVerbose(boolean verbose);
    
    public void stop() {
    }
    
}
