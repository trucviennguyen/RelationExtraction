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

package reck.util;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A set of parameters for execution.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RECKParameters implements Cloneable, Serializable
{
    public int mode = 0;

    /** 
     * option "-r"
     * tree to cover two entities 
     * 0: PET the path-enclosed tree of the constituent syntactic parse tree
     * 1: two kinds of tree + six sequences
     *      PET;
     *      the dependency path tree
     *      six sequences (see our paper EMNLP'09)
     */
    public int tree_type = 0;

    /**
     * option "-n"
     * the proportion in separating training and test set
     */
    public int nbfolds = 5;
    
    /* the path of input data */
    public String inputFilename = null;
    
    /* the path of output data */
    public String outputFilename = null;
    
    public static RECKParameters reckParameters = null;
    
    public static int nbr_err_in_rel_corpora = 0;
    
    public RECKParameters () {
        reckParameters = this;
    }
    
    public RECKParameters (String[] args) {
        this();
        int k = 4;
        if (args[0].equalsIgnoreCase("-s")) 
            mode = Integer.parseInt(args[1]);
        else
            k = 2;
        if (args.length > (k - 2) )
            this.inputFilename = args[k - 2];
        if (args.length > (k - 1) )
            this.outputFilename = args[k - 1];
        String[] argv = RECKConstants.copyArray(args, k);
        parse(argv);
    }
    
    public RECKParameters(String sParams) {
        this();

        // parse options
        StringTokenizer st = new StringTokenizer(sParams);
        String[] args = new String[st.countTokens()];
        for (int i = 0;i < args.length;i++)
            args[i] = st.nextToken();
        int k = 4;
        if (args[0].equalsIgnoreCase("-s")) 
            mode = Integer.parseInt(args[1]);
        else
            k = 2;
        this.inputFilename = args[k - 2];
        this.outputFilename = args[k - 1];
        String[] argv = RECKConstants.copyArray(args, k);
        parse(argv);
    }
   
    public static RECKParameters getRECKParameters() {
        return reckParameters;
    }
    
    public void parse(String[] argv) {

        // parse options
        for (int i=0;i<argv.length;i++)
        {
            if(argv[i].charAt(0) != '-') break;
            if(++i>=argv.length)
            {
                    System.err.print("unknown option\n");
                    break;
            }
            switch(argv[i-1].charAt(1))
            {                
                case 'r':
                    tree_type = RECKConstants.atoi(argv[i]);
                    break;
                        
                case 'n':
                    nbfolds = RECKConstants.atoi(argv[i]);
                    break;
                    
                default:
                    System.err.print("unknown option\n");
            }
        }  
    }
    
    public String toString() {
        String st = "";

        st = "-t " + tree_type + " -k " + "-n " + nbfolds;
        
        return st;
    }

}