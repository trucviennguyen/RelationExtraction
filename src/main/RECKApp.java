// Relation Extraction using Composite Kernel -- RECK
// -- a kernel-based relation extractor
// Copyright (c) 2011
// Truc-Vien T. Nguyen. All Rights Reserved.
//
// RECK is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// RECK is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with RECK.  If not, see <http://www.gnu.org/licenses/>.
//
// For more information, bug reports, fixes, contact:
//    Truc-Vien T. Nguyen
//    trucvien.nguyen@gmail.com
//    http://sites.google.com/site/trucviennguyen/

package main;

import reck.corpora.CorpusImpl;
import reck.util.RECKParameters;

/**
 * This class provides the top-level API and command-line interface 
 * <p>
 * See the package documentation for more details and examples of use.
 * See the main method documentation for details of invoking the extractor.
 * <p>
 * Note that the composite kernel integrated with dependency parse requires
 * a fair amount of memory and time.  Try -mx1024m.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RECKApp {
    public static RECKApp reckApp = null;
    public RECKParameters reckParams = null;
    
    /**
     * Construct a new RECKApp object from a set of parameters
     */
    public RECKApp(String[] args) {
        reckParams = new RECKParameters(args);
        CorpusImpl corpus = new CorpusImpl(reckParams);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length < 1) {
            System.out.println("inputFileName ...");
            return;
        }
        
        reckApp = new RECKApp(args);
    }

    /**
     * return the object <code>RECKApp</code> itself
     */
    public static RECKApp getRECKApp() {
        return reckApp;
    }
}
