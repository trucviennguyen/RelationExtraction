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

import edu.stanford.nlp.process.Morphology;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import reck.Entity;
import reck.Mention;

/**
 * A set of constants and static variables that should be shared among various classes.
 * 
 * @author Truc-Vien T. Nguyen
 */
public final class RECKConstants
{
    /**
     *  The default size to be used for Hashtable, HashMap and HashSet.
     *  The default is 11 and it leads to big memory usage. Having a default
     *  load factor of 0.75, table of size 4 can take 3 elements before being
     *  re-hashed - a values that seems to be optimal for most of the cases.
     */
    public static final int HASH_STH_SIZE = 4;
    
    public static final int INTERNAL_BUFFER_SIZE  = 16*1024;
    
    /**
     * used for dependency tree
     */
    public static final int DP_marked = 1;
    
    public static final Morphology morpha = new Morphology();
    
    public static final ArrayList newsCategories = new ArrayList();
    
    public static final ArrayList entityTypes = new ArrayList();
    public static final ArrayList entitySubTypes = new ArrayList();
    public static final ArrayList entityClasses = new ArrayList();
    
    public static final ArrayList relationTypes = new ArrayList();    
    public static final ArrayList relationSubTypes = new ArrayList();
    public static final ArrayList relationLDCLexicalConditions = new ArrayList();
    
    public static final ArrayList mentionTypes = new ArrayList();
    public static final ArrayList mentionLDCTypes = new ArrayList();
    public static final ArrayList mentionRoles = new ArrayList();
    public static final ArrayList mentionReferences = new ArrayList();
    
    public static final ArrayList treeTypes = new ArrayList();
    
    public static final String begin_tree = "|BT|";
    public static final String end_tree = "|ET|";
    
    public static final String begin_vector = "|BV|";
    public static final String end_vector = "|EV|";

    /** Local fashion for file separators. */
    public static String fileSeparator = System.getProperty("file.separator");
    
    static {
        /** News Categories */
        newsCategories.add("APW");
        newsCategories.add("NYT");
        newsCategories.add("ABC");
        newsCategories.add("CNN");
        newsCategories.add("MNB");
        newsCategories.add("NBC");
        newsCategories.add("PRI");
        newsCategories.add("VOA");

        /** Entity Types */
        entityTypes.add("PER");
        entityTypes.add("ORG");
        entityTypes.add("LOC");
        entityTypes.add("GPE");
        entityTypes.add("FAC");
        entityTypes.add("VEH");
        entityTypes.add("WEA");
        
        /** Entity Subtypes */
        entitySubTypes.add("ORG.Government");
        entitySubTypes.add("ORG.Commercial");
        entitySubTypes.add("ORG.Educational");
        entitySubTypes.add("ORG.Non-Profit");
        entitySubTypes.add("ORG.Other");

        entitySubTypes.add("LOC.Address");
        entitySubTypes.add("LOC.Boundary");
        entitySubTypes.add("LOC.Celestial");
        entitySubTypes.add("LOC.Water-Body");
        entitySubTypes.add("LOC.Land-Region-Natural");
        entitySubTypes.add("LOC.Region-Local");
        entitySubTypes.add("LOC.Region-Subnational");
        entitySubTypes.add("LOC.Region-National");
        entitySubTypes.add("LOC.Region-International");

        entitySubTypes.add("GPE.Continent");
        entitySubTypes.add("GPE.Nation");
        entitySubTypes.add("GPE.State-or-Province");
        entitySubTypes.add("GPE.County-or-District");
        entitySubTypes.add("GPE.Population-Center");
        entitySubTypes.add("GPE.Other");

        entitySubTypes.add("FAC.Building");
        entitySubTypes.add("FAC.Subarea-Building");
        entitySubTypes.add("FAC.Bounded-Area");
        entitySubTypes.add("FAC.Conduit");
        entitySubTypes.add("FAC.Path");
        entitySubTypes.add("FAC.Barrier");
        entitySubTypes.add("FAC.Plant");
        entitySubTypes.add("FAC.Other");

        entitySubTypes.add("VEH.Land");
        entitySubTypes.add("VEH.Air");
        entitySubTypes.add("VEH.Water");
        entitySubTypes.add("VEH.Subarea-Vehicle");
        entitySubTypes.add("VEH.Other");

        entitySubTypes.add("WEA.Blunt");
        entitySubTypes.add("WEA.Exploding");
        entitySubTypes.add("WEA.Sharp");
        entitySubTypes.add("WEA.Chemical");
        entitySubTypes.add("WEA.Biological");
        entitySubTypes.add("WEA.Shooting");
        entitySubTypes.add("WEA.Projectile");
        entitySubTypes.add("WEA.Nuclear");
        entitySubTypes.add("WEA.Other");
        
        /** Entity Classes */
        entityClasses.add("NEG");
        entityClasses.add("SPC");
        entityClasses.add("GEN");
        entityClasses.add("USP");
        entityClasses.add("ATR");
        
        /** Relation Types */
        relationTypes.add("NONE");
        relationTypes.add("PHYS");
        relationTypes.add("PER-SOC");
        relationTypes.add("EMP-ORG");
        relationTypes.add("ART");
        relationTypes.add("OTHER-AFF");
        relationTypes.add("GPE-AFF");
        relationTypes.add("DISC");
        
        /** Relation Subtypes */
        relationSubTypes.add("PHYS.Located");
        relationSubTypes.add("PHYS.Near");
        relationSubTypes.add("PHYS.Part-Whole");

        relationSubTypes.add("PER-SOC.Business");
        relationSubTypes.add("PER-SOC.Family");
        relationSubTypes.add("PER-SOC.Other");

        relationSubTypes.add("EMP-ORG.Employ-Executive");
        relationSubTypes.add("EMP-ORG.Employ-Staff");
        relationSubTypes.add("EMP-ORG.Employ-Undetermined");
        relationSubTypes.add("EMP-ORG.Member-of-Group");
        relationSubTypes.add("EMP-ORG.Subsidiary");
        relationSubTypes.add("EMP-ORG.Partner");
        relationSubTypes.add("EMP-ORG.Other");

        relationSubTypes.add("ART.User-or-Owner");
        relationSubTypes.add("ART.Inventor-or-Manufacturer");
        relationSubTypes.add("ART.Other");

        relationSubTypes.add("OTHER-AFF.Ethnic");
        relationSubTypes.add("OTHER-AFF.Ideology");
        relationSubTypes.add("OTHER-AFF.Other");

        relationSubTypes.add("GPE-AFF.Citizen-or-Resident");
        relationSubTypes.add("GPE-AFF.Based-In");
        relationSubTypes.add("GPE-AFF.Other");
        
        /** Relation LDCLexicalConditions */
        relationLDCLexicalConditions.add("Possessive");
        relationLDCLexicalConditions.add("Preposition");
        relationLDCLexicalConditions.add("PreMod");
        relationLDCLexicalConditions.add("Formulaic");
        relationLDCLexicalConditions.add("Verbal");
        relationLDCLexicalConditions.add("Participial");
        
        /** Mention Types */
        mentionTypes.add("NAM");
        mentionTypes.add("NOM");
        mentionTypes.add("PRO");
        mentionTypes.add("PRE");
        
        /** Mention LDC Types */
        mentionLDCTypes.add("NAM");
        mentionLDCTypes.add("NOM");
        mentionLDCTypes.add("BAR");
        mentionLDCTypes.add("MWH");
        mentionLDCTypes.add("PRO");
        mentionLDCTypes.add("WHQ");
        mentionLDCTypes.add("PRE");
        mentionLDCTypes.add("HLS");
        mentionLDCTypes.add("MSC");
        mentionLDCTypes.add("PTV");
        mentionLDCTypes.add("CMC");
        mentionLDCTypes.add("APP");
        mentionLDCTypes.add("ARC");
        mentionLDCTypes.add("DE");
        mentionLDCTypes.add("PCN");
        mentionLDCTypes.add("PMM");
        mentionLDCTypes.add("EPM");
        mentionLDCTypes.add("EAP");
        
        /** Mention Roles */
        mentionRoles.add("PER");
        mentionRoles.add("ORG");
        mentionRoles.add("LOC");
        mentionRoles.add("GPE");
        mentionRoles.add("FAC");
        
        /** Mention References */
        mentionReferences.add("Literal");
        mentionReferences.add("Intended");
        
        /** Tree portion for Relation Representation */
        treeTypes.add("constituent");
        treeTypes.add("dependency-based words");
        treeTypes.add("dependency-based grammatical relations");
        treeTypes.add("grammatical relations followed by word");
        treeTypes.add("dependency-based constituents");
    }
    
    /* kernel type */
    public static final int TREE_KERNEL = 0;
    public static final int COMPOSITE_KERNEL = 1;

    /* test type: n-fold cross validation or seperate training and test set */
    public static final int N_FOLD_CROSS_VALIDATION = 0;
    public static final int TRAINING_TEST = 1;

    /* parameter n in n-fold cross validation or in seperating training and test set */
    public static final int N = 5;    

    public static final String today() {
        Calendar calendar = new GregorianCalendar();
        String today = calendar.get(Calendar.YEAR) + "-"
                + calendar.get(Calendar.MONTH) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH);
        return today;
    }
    
    public static final void writeToFile(Object o, String outputFilename) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(outputFilename));
        out.writeObject(o);
        out.close(); // Also flushes output    
    }
    
    public static final Object readFromFile(String inputFilename) 
            throws IOException, FileNotFoundException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(inputFilename));
        return in.readObject();  
    }

    public static final float atof(String s)
    {
        return Float.valueOf(s).floatValue();
    }

    public static final int atoi(String s)
    {
        return Integer.parseInt(s);
    }
    
    public static final class ReckFilenameFilter implements FilenameFilter {

        /**
        * Construction method
        */
        public ReckFilenameFilter(String suffix) {
            this.suffix = suffix;
        }

        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param   dir    the directory in which the file was found.
         * @param   name   the name of the file.
         * @return  <code>true</code> if and only if the name should be
         * included in the file list; <code>false</code> otherwise.
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith(suffix))
                return true;
            return false;
        }

        // The expected suffix
        String suffix;

    }
    
    public static String[] copyArray(String[] src, int from) {
        String[] des = new String[0];
        
        if (from < src.length) {
            des = new String[src.length - from];
            for (int i = from; i < src.length; i++) 
                des[i - from] = src[i];
        }
        
        return des;
    }
    
    public static String[] copyArray(String[] src, int from, int to) {
        String[] des = new String[0];
        
        if ( (from <= to) && (to < src.length) ) {
            des = new String[to - from + 1];
            for (int i = from; i < to; i++) 
                des[i - from] = src[i];
        }
        
        return des;
    }

    public static boolean mentionOrder(Mention ment1, Mention ment2) {
        Entity e1 = ment1.getEntity();
        Entity e2 = ment2.getEntity();
        
        int eid1 = entityTypes.indexOf(e1.getType());
        int eid2 = entityTypes.indexOf(e2.getType());
        
        boolean incr = true;
        
        if (eid1 >= eid2)
            if (eid1 > eid2)
                incr = false;
            else {
                int mid1 = mentionTypes.indexOf(ment1.getType());
                int mid2 = mentionTypes.indexOf(ment2.getType());
                
                if (mid1 > mid2)
                    incr = false;
                else if (mid1 == mid2)
                    if (ment1.getHeadword().compareTo(ment2.getHeadword()) > 0)
                        incr = false;
            }
                
        
        return incr;
    }
    
    public static boolean DPMatching(Mention mention, Long start, Long end) {

        Charseq hwPos = mention.getHwPosition();
    	
    	if ( ((hwPos.getStart().longValue() <= start.longValue()) && (start.longValue() <= hwPos.getEnd().longValue())) 
                || ((start.longValue() <= hwPos.getStart().longValue()) && (hwPos.getStart().longValue() <= end.longValue())))
            return true;
    	
    	return false;
    	
    }
    
    public static boolean newMatching(Mention mention, Long start, Long end) {

        Charseq hwPos = mention.getHwPosition();
    	
    	if ( (start.longValue() == hwPos.getStart().longValue())
                && (end.longValue() == hwPos.getEnd().longValue()) )
            return true;
    	
    	return false;
    	
    }

    public static String readContentFromFile(URL u, String encoding) {
        String docContent = "";

        try {
            int readLength = 0;
            char[] readBuffer = new char[INTERNAL_BUFFER_SIZE];

            BufferedReader uReader = null;
            StringBuffer buf = new StringBuffer();
            char c;
            long toRead = Long.MAX_VALUE;
            
            if(encoding != null && !encoding.equalsIgnoreCase("")) {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream(), encoding), INTERNAL_BUFFER_SIZE);
            } 
            else {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream()), RECKConstants.INTERNAL_BUFFER_SIZE);
            }
            
            // read gtom source into buffer
            while (toRead > 0 && (readLength = uReader.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1) {
                if (toRead <  readLength) {
                    //well, if toRead(long) is less than readLenght(int)
                    //then there can be no overflow, so the cast is safe
                    readLength = (int)toRead;
                }

                buf.append(readBuffer, 0, readLength);
                toRead -= readLength;
            }

            // 4.close reader
            uReader.close();

            docContent = new String(buf);
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return docContent;
    }

    public static String trimReturn(String st) {
        String text = st;
        while (text.contains("\n")) {
            int i = text.indexOf("\n");
            text = text.substring(0, i) + " " + text.substring(i + 1);
        }
        
        return text;
    }

}
