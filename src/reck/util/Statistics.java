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

/**
 * A set of properties used for statistics.
 * 
 * @author Truc-Vien T. Nguyen
 */
public final class Statistics {

    public static int nbr_documents = 0, nbr_entities = 0, nbr_mentions = 0, nbr_relations = 0;
    
    public static int nbr_newsPerCategory[] = new int[8];
    public static int nbr_entitiesPerType[] = new int[7];
    public static int nbr_entitiesPerSubType[] = new int[42];
    public static int nbr_entitiesPerClass[] = new int[5];
    public static int nbr_relationsPerType[] = new int[8];
    public static int nbr_relationsPerSubType[] = new int[22];
    public static int nbr_relationLDCLexicalConditions[] = new int[6];
    public static int nbr_mentionsPerType[] = new int[4];
    public static int nbr_mentionsPerLDCType[] = new int[18];
    public static int nbr_mentionsPerRole[] = new int[5];
    public static int nbr_mentionsPerReference[] = new int[2];
    
    public static int nbr_out_relations = 0;

    //static {
        
    //}
}
