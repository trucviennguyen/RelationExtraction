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

package reck.corpora;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import java.util.Vector;
import reck.Corpus;
import reck.Document;
import reck.Entity;
import reck.Mention;
import reck.Relation;
import reck.annotation.RelationImpl;
import reck.trees.RECKCTTreeNodeImpl;
import reck.trees.RECKParseTreeImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.util.RECKConstants;
import reck.util.RECKParameters;
import reck.util.RECKConstants.ReckFilenameFilter;
import reck.util.SortedFile;
import reck.util.Statistics;
import reck.util.TreeUtils;

/**
 * A corpus that contains a list of documents.
 * 
 * @author Truc-Vien T. Nguyen
 */
public class CorpusImpl implements Corpus, Cloneable, Serializable {

    /**
     * Parameters for program execution.
     */
    RECKParameters reckParams = null;

    /**
     * A list of head words of entity mentions.
     */
    public final Vector headwords = new Vector();
    
    /**
     * Construction method.
     */
    public CorpusImpl() {
        
    }
    
    /**
     * Construct a corpus from a list of available documents.
     * 
     */
    public CorpusImpl(ArrayList documentList) {
        this();
        this.documentList = documentList;
    }
    
    /**
     * Construct a corpus from a set of parameters.
     */
    public CorpusImpl(RECKParameters reckParams) {
        this();
        this.reckParams = reckParams;
        
       switch (reckParams.mode) {

            /**
             * read from ACE corpora, process them
             * and export to serialized files
             */
            case 0:
                readAndWriteToDataFile(reckParams.inputFilename, reckParams.outputFilename);
                break;

            /** 
             * read documents from serialized files,
             * and print out statistics
             */
            case 1:
                readFromSerializedFile(reckParams.inputFilename);
                printStatistics();
                break;

            /** 
             * read documents from serialized files,
             * output file containing all the headword
             * this will be used later for real application on RE
             */
            case 2:
                readFromSerializedFile(reckParams.inputFilename);
                constructHeadwordDictionary();
                writeHeadwordsToFile(reckParams.outputFilename);
                break;
               
            /** 
             * read from serialized files, 
             * generate potential relations
             * splitting data into training/test parts
             */
            case 3:
                readFromSerializedFile(reckParams.inputFilename);
                constructHeadwordDictionary();
                creatingFolds();
                generatePossibleRelations();

                // with n-fold cross validation
                separatingTrainingAndTestSetWithFolds();

                printStatistics();
                break;

            /** 
             * from binary to multi-class classification
             * one-vs-rest approach
             * @input: all output files (one file/class) from SVM-Light 
             * @oputput: one output file, each line contains corresponding class
             */
            case 4:
            	oneVsRest(reckParams.inputFilename, reckParams.outputFilename);
                break;

            /** 
             * compare between gold data and test outcome
             * output results in form of Precision/Recall/F-measure
             * for 1. each relation type and 2. overall evaluation
             */
            case 5:
                compare(reckParams.inputFilename, reckParams.outputFilename);
                break;

            default:
                
        }

    }

    /** The size of this corpus */
    public int size() {
        return documentList.size();
    }

    /** Returns true if this corpus contains no document */
    public boolean isEmpty() {
        return documentList.isEmpty();
    }

    /** Returns true if this list contains the specified element.
     * 
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(Object o){
        return documentList.contains(o);
    }

    /** Get an iterator for this list. */
    public Iterator iterator(){
        return documentList.iterator();
    }

    /** Returns an array containing all of the documents in this corpus. */
    public Object[] toArray(){
        return documentList.toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    public Object[] toArray(Object[] a){
        return documentList.toArray(a);
    }

    /**
     * Add a document to the corpus
     *
     * @param o a document object
     * @return <tt>true</tt>
     */
    public boolean add(Object o){
        return documentList.add(o);
    }

    /**
     * Removes the first occurrence of the object document
     * from this corpus, if it is present.
     *
     * @param o document to be removed from this corpus, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(Object o){
        return documentList.remove(o);
    }

    /**
     * Check if the corpus contains all the elements of c
     * 
     * @param c a collection of documents.
     */
    public boolean containsAll(Collection c){
        return documentList.containsAll(c);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list.
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection c){
        return documentList.addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection c){
        return documentList.addAll(index, c);
    }

    /**
     * Check if it's contained in the specified collection.
     */
    public boolean removeAll(Collection c){
        return documentList.removeAll(c);
    }

    public boolean retainAll(Collection c){
        return documentList.retainAll(c);
    }

    public void clear(){
        documentList.clear();
    }

    public boolean equals(Object o){
        if (! (o instanceof CorpusImpl))
            return false;

        return documentList.equals(o);
    }

    public int hashCode(){
        return documentList.hashCode();
    }

    public Object get(int index){
        return documentList.get(index);
    }

    public Object set(int index, Object element){
        return documentList.set(index, element);
    }

    public void add(int index, Object element){
    documentList.add(index, element);
    }

    public Object remove(int index){
        return documentList.remove(index);
    }

    public int indexOf(Object o){
        return documentList.indexOf(o);
    }

    public int lastIndexOf(Object o){
    return lastIndexOf(o);
    }

    public ListIterator listIterator(){
        return documentList.listIterator();
    }

    public ListIterator listIterator(int index){
        return documentList.listIterator(index);
    }

    public Corpus subList(int fromIndex, int toIndex){
        return new CorpusImpl(new ArrayList(documentList.subList(fromIndex, toIndex)));
    }

    /**
     * Print out statistical figures of the corpus
     * number of documents
     * number of entities
     * number of entity mentions
     * number of relations
     */
    public void printStatistics() {
        System.out.println("Statistics: ");
        System.out.println("\t" + Statistics.nbr_documents + " documents");
        System.out.println("\t" + Statistics.nbr_entities + " entities");
        System.out.println("\t" + Statistics.nbr_mentions + " mentions");
        System.out.println("\t" + Statistics.nbr_relations + " relations");
        // System.out.println("\t" + Statistics.nbr_out_relations + " relations out");
        System.out.println();
    }

    /**
     * Write the corpus to serialized files
     */
    public void writeToFile(String outputFilename) {
        try {
            for (int i = 0; i < documentList.size(); i++) {
                ((DocumentImpl)documentList.get(i)).writeToFile(outputFilename);
            }
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Read the corpus from a folder.
     */
    public void readFromDataFile(String inputFilename) {
        File df = new File(inputFilename);
        DocumentImpl doc = null;

        if (df.isDirectory()) {
            ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".XML");
            File[] fs_annotated = df.listFiles(annotated_filter);
            for (int i = 0; i < fs_annotated.length; i++) {
                doc = new DocumentImpl(fs_annotated[i]);
                add(doc);
                relationListACE.addAll(doc.getRelations());
                nbr_entities += doc.getEntities().size();
                nbr_relations += doc.getRelations().size();
                
                Statistics.nbr_documents++;
                
                int news_Category_Index = RECKConstants.newsCategories.indexOf(doc.docId.substring(0, 3));
                Statistics.nbr_newsPerCategory[news_Category_Index]++;
            }
        }
        else {
            doc = new DocumentImpl(df);
            add(doc);
            relationListACE.addAll(doc.getRelations());
            nbr_entities += doc.getEntities().size();
            nbr_relations += doc.getRelations().size();
                
            Statistics.nbr_documents++;
        }
    }

    /**
     * read from ACE 2004 corpus, process them
     * and export to serialized files
     * @param inputFilename  the input folder that contains ACE 2004 documents
     * @param outputFilename the output folder that contains serialized files
     */
    public void readAndWriteToDataFile(String inputFilename, String outputFilename) {
        File df = new File(inputFilename);
        DocumentImpl doc = null;
        
        File f = new File(outputFilename);
        if (!f.exists())
            f.mkdir();

        try {
            if (df.isDirectory()) {
                ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".XML");
                File[] fs_annotated = df.listFiles(annotated_filter);
                for (int i = 0; i < fs_annotated.length; i++) {
                    doc = new DocumentImpl(fs_annotated[i]);
                    add(doc);
                    doc.writeToFile(outputFilename);
                    relationListACE.addAll(doc.getRelations());
                    Statistics.nbr_documents++;

                    int news_Category_Index = RECKConstants.newsCategories.indexOf(doc.docId.substring(0, 3));
                    Statistics.nbr_newsPerCategory[news_Category_Index]++;
                }
            }
            else {
                doc = new DocumentImpl(df);
                add(doc);
                doc.writeToFile(outputFilename);
                relationListACE.addAll(doc.getRelations());
                Statistics.nbr_documents++;
            }
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Write entity head words to a file.
     */
    public void writeHeadwordsToFile(String outputFilename) {

        BufferedWriter writer_headwords = null;
        
        try {
            File f = new File(outputFilename);
            if (!f.exists())
                f.mkdir();

            writer_headwords = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename + RECKConstants.fileSeparator + "headwords.data"))));
            
            for (int i = 0; i < headwords.size(); i ++) {
                writer_headwords.write((String)headwords.elementAt(i));
                writer_headwords.newLine();
            }
            
            writer_headwords.close();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Write features of relation instances to training/test files
     * in n folds
     */
    public void writeToKernelFile(int n_fold, int nbr_training, int nbr_test,
            ArrayList trainingRelationList, ArrayList testRelationList,
            ArrayList<String> markTrainList, ArrayList<String> markTestList,
            String outputFilename) {

        ArrayList trainingList = new ArrayList();
        ArrayList testList = new ArrayList();
        int i, j, k, nbr_rel_types = RECKConstants.relationTypes.size();

        int index1 = RECKConstants.entityTypes.size();
        int index2 = index1 + RECKConstants.entitySubTypes.size();
        int index3 = index2 + RECKConstants.mentionTypes.size();
        int index4 = index3 + RECKConstants.mentionLDCTypes.size();
        int index5 = index4 + RECKConstants.mentionRoles.size();
        int index6 = index5 + RECKConstants.mentionReferences.size();

        BufferedWriter writer_training = null;
        BufferedWriter writer_test = null;

        try {
            File f = new File(outputFilename);
            if (!f.exists())
                f.mkdir();

            for (i = 0; i < reckParams.nbfolds; i++) {
                f = new File(outputFilename + RECKConstants.fileSeparator + n_fold);
                f.mkdir();
            }

            for (i = 0; i < nbr_rel_types; i++) {
                writer_training = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(outputFilename
                        + RECKConstants.fileSeparator + n_fold
                        + RECKConstants.fileSeparator + i + ".train"))));
                trainingList.add(writer_training);
                writer_test = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(outputFilename
                        + RECKConstants.fileSeparator + n_fold
                        + RECKConstants.fileSeparator + i + ".test"))));
                testList.add(writer_test);
            }

            BufferedWriter gold_train = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "gold_train.data"))));

            BufferedWriter gold_test = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "gold_test.data"))));

            BufferedWriter mark_train = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "train.sent"))));

            BufferedWriter mark_test = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "test.sent"))));

            System.out.println("Output Tree ...");
            RECKCTTreeNodeImpl CTTree = null;

            for (i = 0; i < nbr_training; i++) {
                Relation relation = (Relation)trainingRelationList.get(i);
                int relIndex = RECKConstants.relationTypes.indexOf(relation.getType());
                ArrayList treeList = relation.getRelationTree();
                Mention mention1 = relation.getMention(0);
                Entity entity1 = mention1.getEntity();
                Mention mention2 = relation.getMention(1);
                Entity entity2 = mention2.getEntity();

                String st = "";

                /** Tree Kernel
                  * 0: Constituent Tree with the portion is the Path-Enclosed Tree
                  */

                Object o = treeList.get(0);
                StringBuffer sb = new StringBuffer(RECKConstants.INTERNAL_BUFFER_SIZE);
                CTTree = (RECKCTTreeNodeImpl)o;
                CTTree.toStringBuffer(sb);
                st += RECKConstants.begin_tree + " " + sb.toString() + " " + RECKConstants.end_tree;

                int i1 = RECKConstants.entityTypes.indexOf(entity1.getType()) + 1;
                int i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity1.getSubType()) + 2;
                int i3 = index2 + RECKConstants.mentionTypes.indexOf(mention1.getType()) + 3;
                int i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention1.getLDCType()) + 4;
                int i5 = index4 + RECKConstants.mentionRoles.indexOf(mention1.getRole()) + 5;
                int i6 = index5 + RECKConstants.mentionReferences.indexOf(mention1.getReference()) + 6;
                int i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention1.getHeadword()).toLowerCase()) + 7;

                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";

                st += " " + RECKConstants.begin_vector;
                i1 = RECKConstants.entityTypes.indexOf(entity2.getType()) + 1;
                i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity2.getSubType()) + 2;
                i3 = index2 + RECKConstants.mentionTypes.indexOf(mention2.getType()) + 3;
                i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention2.getLDCType()) + 4;
                i5 = index4 + RECKConstants.mentionRoles.indexOf(mention2.getRole()) + 5;
                i6 = index5 + RECKConstants.mentionReferences.indexOf(mention2.getReference()) + 6;
                i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention2.getHeadword()).toLowerCase()) + 7;

                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                st += " " + RECKConstants.end_vector;

                if ((st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being (human))))))") == -1)
                        && (st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being)))))") == -1)) {

                    gold_train.write(new Integer(relIndex).toString());
                    gold_train.newLine();

                    ((BufferedWriter)trainingList.get(relIndex)).write("1 " + st);
                    ((BufferedWriter)trainingList.get(relIndex)).newLine();

                    for (j = 0; j < nbr_rel_types; j++)
                        if (j != relIndex) {
                            ((BufferedWriter)trainingList.get(j)).write("-1 " + st);
                            ((BufferedWriter)trainingList.get(j)).newLine();
                        }

                    mark_train.write((String)markTrainList.get(i));
                    mark_train.newLine();
                }

            }

            for (i = 0; i < nbr_test; i++) {
                Relation relation = (Relation)testRelationList.get(i);
                int relIndex = RECKConstants.relationTypes.indexOf(relation.getType());

                ArrayList treeList = relation.getRelationTree();
                Mention mention1 = relation.getMention(0);
                Entity entity1 = mention1.getEntity();
                Mention mention2 = relation.getMention(1);
                Entity entity2 = mention2.getEntity();

                String st = "";

                Object o = treeList.get(0);
                StringBuffer sb = new StringBuffer(RECKConstants.INTERNAL_BUFFER_SIZE);
                CTTree = (RECKCTTreeNodeImpl)o;
                CTTree.toStringBuffer(sb);
                st += RECKConstants.begin_tree + " " + sb.toString() + " " + RECKConstants.end_tree;

                int i1 = RECKConstants.entityTypes.indexOf(entity1.getType()) + 1;
                int i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity1.getSubType()) + 2;
                int i3 = index2 + RECKConstants.mentionTypes.indexOf(mention1.getType()) + 3;
                int i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention1.getLDCType()) + 4;
                int i5 = index4 + RECKConstants.mentionRoles.indexOf(mention1.getRole()) + 5;
                int i6 = index5 + RECKConstants.mentionReferences.indexOf(mention1.getReference()) + 6;
                int i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention1.getHeadword()).toLowerCase()) + 7;

                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";

                st += " " + RECKConstants.begin_vector;
                i1 = RECKConstants.entityTypes.indexOf(entity2.getType()) + 1;
                i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity2.getSubType()) + 2;
                i3 = index2 + RECKConstants.mentionTypes.indexOf(mention2.getType()) + 3;
                i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention2.getLDCType()) + 4;
                i5 = index4 + RECKConstants.mentionRoles.indexOf(mention2.getRole()) + 5;
                i6 = index5 + RECKConstants.mentionReferences.indexOf(mention2.getReference()) + 6;
                i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention2.getHeadword()).toLowerCase()) + 7;

                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                st += " " + RECKConstants.end_vector;

                if ((st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being (human))))))") == -1)
                        && (st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being)))))") == -1)) {

                    gold_test.write(new Integer(relIndex).toString());
                    gold_test.newLine();

                    ((BufferedWriter)testList.get(relIndex)).write("1 " + st);
                    ((BufferedWriter)testList.get(relIndex)).newLine();

                    for (j = 0; j < nbr_rel_types; j++)
                        if (j != relIndex) {
                            ((BufferedWriter)testList.get(j)).write("-1 " + st);
                            ((BufferedWriter)testList.get(j)).newLine();
                        }

                    mark_test.write((String)markTestList.get(i));
                    mark_test.newLine();
                }
            }

            System.out.println("Output Tree completed");

            for (i = 0; i < nbr_rel_types; i++) {
                ((BufferedWriter)trainingList.get(i)).close();
                ((BufferedWriter)testList.get(i)).close();
            }

            mark_train.close();
            mark_test.close();
            gold_train.close();
            gold_test.close();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    } // writeToKernelFile ___ with n folds

    /**
     * Write features of relation instances to training/test files
     * in n folds
     */
    public void writeToKernelFile_All(int n_fold, int nbr_training, int nbr_test,
            ArrayList trainingRelationList, ArrayList testRelationList, 
            ArrayList<String> markTrainList, ArrayList<String> markTestList, 
            String outputFilename) {

        ArrayList trainingList = new ArrayList();
        ArrayList testList = new ArrayList();
        int i, j, k, nbr_rel_types = RECKConstants.relationTypes.size();
        
        int index1 = RECKConstants.entityTypes.size();
        int index2 = index1 + RECKConstants.entitySubTypes.size();
        int index3 = index2 + RECKConstants.mentionTypes.size();
        int index4 = index3 + RECKConstants.mentionLDCTypes.size();
        int index5 = index4 + RECKConstants.mentionRoles.size();
        int index6 = index5 + RECKConstants.mentionReferences.size();
        
        BufferedWriter writer_training = null;
        BufferedWriter writer_test = null;
        
        try {
            File f = new File(outputFilename);
            if (!f.exists())
                f.mkdir();

            for (i = 0; i < reckParams.nbfolds; i++) {
                f = new File(outputFilename + RECKConstants.fileSeparator + n_fold);
                f.mkdir();
            }
            
            for (i = 0; i < nbr_rel_types; i++) {
                writer_training = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(outputFilename 
                        + RECKConstants.fileSeparator + n_fold
                        + RECKConstants.fileSeparator + i + ".train"))));
                trainingList.add(writer_training);
                writer_test = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(outputFilename 
                        + RECKConstants.fileSeparator + n_fold
                        + RECKConstants.fileSeparator + i + ".test"))));
                testList.add(writer_test);
            }

            BufferedWriter gold_train = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename 
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "gold_train.data"))));

            BufferedWriter gold_test = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename 
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "gold_test.data"))));

            BufferedWriter mark_train = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename 
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "train.sent"))));

            BufferedWriter mark_test = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename 
                    + RECKConstants.fileSeparator + n_fold
                    + RECKConstants.fileSeparator + "test.sent"))));

            System.out.println("Output Tree ...");
            RECKDPTreeNodeImpl DPTree = null;
            RECKCTTreeNodeImpl CTTree = null;

            for (i = 0; i < nbr_training; i++) {
                Relation relation = (Relation)trainingRelationList.get(i);
                int relIndex = RECKConstants.relationTypes.indexOf(relation.getType());
                ArrayList treeList = relation.getRelationTree();
                Mention mention1 = relation.getMention(0);
                Entity entity1 = mention1.getEntity();
                Mention mention2 = relation.getMention(1);
                Entity entity2 = mention2.getEntity();
                
                String st = "";
                
                /** Tree Kernel 
                  * 0: Constituent Tree with the portion is the Path-Enclosed Tree
                  * 1: Dependency based-Words
                  * 2: Dependency based-Grammatical Relations
                  * 3: Dependency based-Grammatical Relations inserted before Words
                  */
                
                for (k = 0; k < treeList.size(); k++) {
                    Object o = treeList.get(k);
                    StringBuffer sb = new StringBuffer(RECKConstants.INTERNAL_BUFFER_SIZE);
                    if (o instanceof RECKDPTreeNodeImpl) {
                        DPTree = (RECKDPTreeNodeImpl)o;
                        DPTree.toStringBuffer(sb);
                    }
                    else {
                        CTTree = (RECKCTTreeNodeImpl)o;
                        CTTree.toStringBuffer(sb);
                    }
                    st += RECKConstants.begin_tree + " " + sb.toString() + " ";
                }
                
                /** Sequence Kernel 
                  * Chain of the Constituent --- Path-Enclosed Tree
                  *     0: Sequence of Words
                  *     1: Sequence of Part-of-Speechs
                  *     2: Sequence of Grammatical Relations
                  * Chain of the Dependency --- Path linking two entities
                  *     0: Sequence of Words
                  *     1: Sequence of Part-of-Speechs
                  *     2: Sequence of Grammatical Relations
                  */
                
                CTTree = (RECKCTTreeNodeImpl)treeList.get(0);
                List CT_SK = CTTree.getLeaves();
                DPTree = (RECKDPTreeNodeImpl)treeList.get(1);
                List DP_SK = TreeUtils.getDependencyPath(DPTree);
                int markCT = -1;
                
                String t1 = "(T1-", t2 = "(T2-";
                if (RECKConstants.mentionOrder(mention1, mention2)) {
                    t1 += entity1.getType() + ")";
                    t2 += entity2.getType() + ")";
                }
                else {
                    t2 += entity1.getType() + ")";
                    t1 += entity2.getType() + ")";
                }
                if (mention1.getHwPosition().getStart().intValue() > mention2.getHwPosition().getStart().intValue()) {
                    String tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }

                for (k = 0; k < CT_SK.size(); k++) {
                    if (((RECKCTTreeNodeImpl)CT_SK.get(k)).getPosition().getStart().intValue() 
                            == mention2.getHwPosition().getStart().intValue()) {
                        markCT = k;
                        break;
                    }
                }

                // Constituent --- 0: Sequence of Words
                st += RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < CT_SK.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    st += " (" + ((RECKCTTreeNodeImpl)CT_SK.get(k)).label().toString() + ")";
                }
                st += ")";
                
                // Constituent --- 1: Sequence of Preterminals
                st += " " + RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < CT_SK.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    st += " (" + ((RECKCTTreeNodeImpl)CT_SK.get(k)).parent(CTTree).label().toString() + ")";
                }
                st += ")";
                
                // Constituent --- 2: Sequence of Grammatical Relations
                ArrayList GRs = TreeUtils.getDependencyList(relation.getRECKParseTree(), CTTree);
                if (GRs.size() != CTTree.getLeaves().size())
                    System.err.println("Error ...");
                st += " " + RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < GRs.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    String role = (String)GRs.get(k);
                    st += " (" + role + ")";
                }
                st += ")";
                
                // Dependency --- 0: Sequence of Words
                // Dependency --- 1: Sequence of Grammatical Relations
                // Dependency --- 2: Sequence of Constituents
                String lbSequence = " " + RECKConstants.begin_tree + " (FAKEROOT",
                       grSequence = " " + RECKConstants.begin_tree + " (FAKEROOT",
                       ctSequence = " " + RECKConstants.begin_tree + " (FAKEROOT";
                for (k = 0; k < DP_SK.size(); k++) {
                    RECKDPTreeNodeImpl dp = (RECKDPTreeNodeImpl)DP_SK.get(k);

                    lbSequence += " (" + dp.label().toString() + ")";

                    if ( (dp.role() == null) || (dp.role().trim().equals("")) )
                        grSequence += " (" + dp.label().toString() + ")";
                    else
                        grSequence += " (" + dp.role() + ")";

                    if ( (dp.constituent() == null) || (dp.constituent().trim().equals("")) )
                        ctSequence += " (" + dp.label().toString() + ")";
                    else
                        ctSequence += " (" + dp.constituent() + ")";
                }
                lbSequence += ")";
                grSequence += ")";
                ctSequence += ")";
                
                st += lbSequence + grSequence + ctSequence;
                
                st += " " + RECKConstants.end_tree;

                int i1 = RECKConstants.entityTypes.indexOf(entity1.getType()) + 1;
                int i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity1.getSubType()) + 2;
                int i3 = index2 + RECKConstants.mentionTypes.indexOf(mention1.getType()) + 3;
                int i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention1.getLDCType()) + 4;
                int i5 = index4 + RECKConstants.mentionRoles.indexOf(mention1.getRole()) + 5;
                int i6 = index5 + RECKConstants.mentionReferences.indexOf(mention1.getReference()) + 6;
                int i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention1.getHeadword()).toLowerCase()) + 7;
                
                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                                
                st += " " + RECKConstants.begin_vector;
                i1 = RECKConstants.entityTypes.indexOf(entity2.getType()) + 1;
                i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity2.getSubType()) + 2;
                i3 = index2 + RECKConstants.mentionTypes.indexOf(mention2.getType()) + 3;
                i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention2.getLDCType()) + 4;
                i5 = index4 + RECKConstants.mentionRoles.indexOf(mention2.getRole()) + 5;
                i6 = index5 + RECKConstants.mentionReferences.indexOf(mention2.getReference()) + 6;
                i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention2.getHeadword()).toLowerCase()) + 7;
                
                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                st += " " + RECKConstants.end_vector;
                
                if ((st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being (human))))))") == -1)
                        && (st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being)))))") == -1)) {

                    gold_train.write(new Integer(relIndex).toString());
                    gold_train.newLine();
                    
                    ((BufferedWriter)trainingList.get(relIndex)).write("1 " + st);
                    ((BufferedWriter)trainingList.get(relIndex)).newLine();

                    for (j = 0; j < nbr_rel_types; j++) 
                        if (j != relIndex) {
                            ((BufferedWriter)trainingList.get(j)).write("-1 " + st);
                            ((BufferedWriter)trainingList.get(j)).newLine();
                        }
                    
                    mark_train.write((String)markTrainList.get(i));
                    mark_train.newLine();
                }

            }
            
            for (i = 0; i < nbr_test; i++) {
                Relation relation = (Relation)testRelationList.get(i);
                int relIndex = RECKConstants.relationTypes.indexOf(relation.getType());
                
                ArrayList treeList = relation.getRelationTree();
                Mention mention1 = relation.getMention(0);
                Entity entity1 = mention1.getEntity();
                Mention mention2 = relation.getMention(1);
                Entity entity2 = mention2.getEntity();
                
                String st = "";
                
                for (k = 0; k < treeList.size(); k++) {
                    Object o = treeList.get(k);
                    StringBuffer sb = new StringBuffer(RECKConstants.INTERNAL_BUFFER_SIZE);
                    if (o instanceof RECKDPTreeNodeImpl) {
                        DPTree = (RECKDPTreeNodeImpl)o;
                        DPTree.toStringBuffer(sb);
                    }
                    else {
                        CTTree = (RECKCTTreeNodeImpl)o;
                        CTTree.toStringBuffer(sb);
                    }
                    st += RECKConstants.begin_tree + " " + sb.toString() + " ";
                }
                
                /** Sequence Kernel 
                  * Chain of the Constituent --- Path-Enclosed Tree
                  *     0: Sequence of Words
                  *     1: Sequence of Part-of-Speechs
                  *     2: Sequence of Grammatical Relations
                  * Chain of the Dependency --- Path linking two entities
                  *     0: Sequence of Words
                  *     1: Sequence of Part-of-Speechs
                  *     2: Sequence of Grammatical Relations
                  */
                
                CTTree = (RECKCTTreeNodeImpl)treeList.get(0);
                List CT_SK = CTTree.getLeaves();
                DPTree = (RECKDPTreeNodeImpl)treeList.get(1);
                List DP_SK = TreeUtils.getDependencyPath(DPTree);
                int markCT = -1;
                
                String t1 = "(T1-", t2 = "(T2-";
                if (RECKConstants.mentionOrder(mention1, mention2)) {
                    t1 += entity1.getType() + ")";
                    t2 += entity2.getType() + ")";
                }
                else {
                    t2 += entity1.getType() + ")";
                    t1 += entity2.getType() + ")";
                }
                if (mention1.getHwPosition().getStart().intValue() > mention2.getHwPosition().getStart().intValue()) {
                    String tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }

                for (k = 0; k < CT_SK.size(); k++) {
                    if (((RECKCTTreeNodeImpl)CT_SK.get(k)).getPosition().getStart().intValue() 
                            == mention2.getHwPosition().getStart().intValue()) {
                        markCT = k;
                        break;
                    }
                }

                // Constituent --- 0: Sequence of Words
                st += RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < CT_SK.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    st += " (" + ((RECKCTTreeNodeImpl)CT_SK.get(k)).label().toString() + ")";
                }
                st += ")";
                
                // Constituent --- 1: Sequence of Preterminals
                st += " " + RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < CT_SK.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    st += " (" + ((RECKCTTreeNodeImpl)CT_SK.get(k)).parent(CTTree).label().toString() + ")";
                }
                st += ")";
                
                // Constituent --- 2: Sequence of Grammatical Relations
                ArrayList GRs = TreeUtils.getDependencyList(relation.getRECKParseTree(), CTTree);
                if (GRs.size() != CTTree.getLeaves().size())
                    System.err.println("Error ...");
                st += " " + RECKConstants.begin_tree + " (FAKEROOT " + t1;
                for (k = 0; k < GRs.size(); k++) {
                    if (k == markCT) st += " " + t2;
                    String role = (String)GRs.get(k);
                    st += " (" + role + ")";
                }
                st += ")";
                
                // Dependency --- 0: Sequence of Words
                // Dependency --- 1: Sequence of Grammatical Relations
                // Dependency --- 2: Sequence of Constituents
                String lbSequence = " " + RECKConstants.begin_tree + " (FAKEROOT",
                       grSequence = " " + RECKConstants.begin_tree + " (FAKEROOT",
                       ctSequence = " " + RECKConstants.begin_tree + " (FAKEROOT";
                for (k = 0; k < DP_SK.size(); k++) {
                    RECKDPTreeNodeImpl dp = (RECKDPTreeNodeImpl)DP_SK.get(k);

                    lbSequence += " (" + dp.label().toString() + ")";

                    if ( (dp.role() == null) || (dp.role().trim().equals("")) )
                        grSequence += " (" + dp.label().toString() + ")";
                    else
                        grSequence += " (" + dp.role() + ")";

                    if ( (dp.constituent() == null) || (dp.constituent().trim().equals("")) )
                        ctSequence += " (" + dp.label().toString() + ")";
                    else
                        ctSequence += " (" + dp.constituent() + ")";
                }
                lbSequence += ")";
                grSequence += ")";
                ctSequence += ")";
                
                st += lbSequence + grSequence + ctSequence;
                
                st += " " + RECKConstants.end_tree;
                
                int i1 = RECKConstants.entityTypes.indexOf(entity1.getType()) + 1;
                int i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity1.getSubType()) + 2;
                int i3 = index2 + RECKConstants.mentionTypes.indexOf(mention1.getType()) + 3;
                int i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention1.getLDCType()) + 4;
                int i5 = index4 + RECKConstants.mentionRoles.indexOf(mention1.getRole()) + 5;
                int i6 = index5 + RECKConstants.mentionReferences.indexOf(mention1.getReference()) + 6;
                int i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention1.getHeadword()).toLowerCase()) + 7;
                
                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                                
                st += " " + RECKConstants.begin_vector;
                i1 = RECKConstants.entityTypes.indexOf(entity2.getType()) + 1;
                i2 = index1 + RECKConstants.entitySubTypes.indexOf(entity2.getSubType()) + 2;
                i3 = index2 + RECKConstants.mentionTypes.indexOf(mention2.getType()) + 3;
                i4 = index3 + RECKConstants.mentionLDCTypes.indexOf(mention2.getLDCType()) + 4;
                i5 = index4 + RECKConstants.mentionRoles.indexOf(mention2.getRole()) + 5;
                i6 = index5 + RECKConstants.mentionReferences.indexOf(mention2.getReference()) + 6;
                i7 = index6 + headwords.indexOf(RECKConstants.trimReturn(mention2.getHeadword()).toLowerCase()) + 7;
                
                if (i1 > 0) st += " " + i1 + ":2";
                if (i2 > 0) st += " " + i2 + ":1";
                if (i3 > 0) st += " " + i3 + ":1";
                if (i4 > 0) st += " " + i4 + ":1";
                if (i5 > 0) st += " " + i5 + ":1";
                if (i6 > 0) st += " " + i6 + ":1";
                if (i7 > 0) st += " " + i7 + ":1";
                st += " " + RECKConstants.end_vector;
                
                if ((st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being (human))))))") == -1)
                        && (st.indexOf("(talking (T2-PER (we)) (3 1/2 (pound (T1-PER (being)))))") == -1)) {
                    
                    gold_test.write(new Integer(relIndex).toString());
                    gold_test.newLine();
                
                    ((BufferedWriter)testList.get(relIndex)).write("1 " + st);
                    ((BufferedWriter)testList.get(relIndex)).newLine();

                    for (j = 0; j < nbr_rel_types; j++) 
                        if (j != relIndex) {
                            ((BufferedWriter)testList.get(j)).write("-1 " + st);
                            ((BufferedWriter)testList.get(j)).newLine();
                        }
                    
                    mark_test.write((String)markTestList.get(i));
                    mark_test.newLine();
                }
            }
            
            System.out.println("Output Tree completed");

            for (i = 0; i < nbr_rel_types; i++) {
                ((BufferedWriter)trainingList.get(i)).close();
                ((BufferedWriter)testList.get(i)).close();
            }
            
            mark_train.close();
            mark_test.close();
            gold_train.close();
            gold_test.close();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    } // writeToKernelFile_All ___ with n folds

    /**
     * read documents from serialized files,
     * and print out statistics
     */
    public void readFromSerializedFile(String inputFilename) {
        SortedFile df = new SortedFile(inputFilename);
        DocumentImpl doc = null;

        try {
            if (df.isDirectory()) {
                clear();
                ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".document");
                File[] fs_annotated = df.listFiles(annotated_filter);
                for (int i = 0; i < fs_annotated.length; i++) {
                    doc = (DocumentImpl)RECKConstants.readFromFile(fs_annotated[i].getCanonicalPath());
                    add(doc);
                    relationListACE.addAll(doc.getRelations());
                    nbr_entities += doc.getEntities().size();
                    nbr_relations += doc.getRelations().size();
                    Statistics.nbr_documents++;
                    Statistics.nbr_entities += doc.getEntities().size();
                    Statistics.nbr_mentions += doc.getMentions().size();
                    Statistics.nbr_relations += doc.getRelations().size();
                }
            }
            else {
                doc = (DocumentImpl)RECKConstants.readFromFile(inputFilename);
                add(doc);
                relationListACE.addAll(doc.getRelations());
                nbr_entities += doc.getEntities().size();
                nbr_relations += doc.getRelations().size();
                Statistics.nbr_documents++;
                Statistics.nbr_entities += doc.getEntities().size();
                Statistics.nbr_mentions += doc.getMentions().size();
                Statistics.nbr_relations += doc.getRelations().size();
            }
        }
        catch (java.lang.ClassNotFoundException classEx) {
            classEx.printStackTrace();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
        System.out.println("nbr_relations = " + Statistics.nbr_relations);
    } // readFromSerializedFile

    /**
     * Construct a list of entity head words
     */
    public void constructHeadwordDictionary() {
        for (int i = 0; i < size(); i++) {
            Document doc = (Document)get(i);
            Iterator mentionIter = doc.getMentions().iterator();
            while (mentionIter.hasNext()) {
                Mention mention = (Mention)mentionIter.next();
                String st = RECKConstants.trimReturn(mention.getHeadword()).toLowerCase();
                if (headwords.indexOf(st) == -1) 
                    headwords.add(st);
            }
        }
    } // constructHeadwordDictionary

    /**
     * Sort entity mentions according to their positions in the document.
     */
    public ArrayList sortEntityList(ArrayList entityList) {
        ArrayList newList = new ArrayList();
        for (int i = 0; i < entityList.size(); i++) {
            long start = ((Mention)entityList.get(i)).getHwPosition().getStart().longValue(),
                   end = ((Mention)entityList.get(i)).getHwPosition().getEnd().longValue();
            int j = 0;
            while ( (j < i) && ((start > ((Mention)newList.get(j)).getHwPosition().getStart().longValue())
                    || ((start == ((Mention)newList.get(j)).getHwPosition().getStart().longValue()) 
                    && (end > ((Mention)newList.get(j)).getHwPosition().getEnd().longValue()))))
                j++;
            if (j == i)
                newList.add(entityList.get(i));
            else
                newList.add(j, entityList.get(i));
        }
        return newList;
    }

    /**
     * Generate potential relation instances
     * by iterating all pairs of entity mentions
     * in each document
     */
    public void generatePossibleRelations() {
        int length_tree_size = 0;
        RECKDPTreeNodeImpl dpTree = null;
        int ifold = 0, currentSize = 0;
        
        // number of possible relations
        for (int i = 0; i < size(); i++) {
            if (currentSize < nfold[ifold]) {
                currentSize++;
            }
            else {
                ifold++;
                currentSize = 0;
            }
            Document doc = (Document)get(i);
            ArrayList treeList = doc.getTreeList();
            for (int j = 0; j < treeList.size(); j++) {
                RECKParseTreeImpl rpTree = (RECKParseTreeImpl)treeList.get(j);
                
                if (rpTree.getDependencyList().size() > 0) {
                
                    ArrayList entityList = new ArrayList(rpTree.getDPEntityTrees().keySet());
                    entityList = sortEntityList(entityList);
                    for (int k = 0; k < entityList.size() - 1; k++) {
                        for (int l = k+1; l < entityList.size(); l++) {
                            Mention mention1 = (Mention)entityList.get(k);
                            Mention mention2 = (Mention)entityList.get(l);
                            String mark = "D" + i + "-S" + j + "-M1_" + mention1.getId() + "-M2_" + mention2.getId();

                            String entityId1 = mention1.getEntity().getId();
                            String entityId2 = mention2.getEntity().getId();

                            if (!entityId1.equals(entityId2)) {
                                if (!RECKConstants.mentionOrder(mention1, mention2)) {
                                    Mention tm = mention1;
                                    mention1 = mention2;
                                    mention2 = tm;
                                }

                                Relation relation = doc.getRelations().getRelationByMentions(mention1, mention2);
                                ArrayList relationTreeList = new ArrayList();

                                switch (RECKParameters.getRECKParameters().tree_type) {

                                    case 0:
                                        relationTreeList.add(TreeUtils.buildPT(rpTree, mention1, mention2));
                                        break;

                                    case 1:
                                        relationTreeList.add(TreeUtils.buildPT(rpTree, mention1, mention2));
                                        dpTree = TreeUtils.buildDependencyPT(rpTree, mention1, mention2);

                                        if (dpTree == null)
                                            relationTreeList = null;
                                        else
                                            relationTreeList.add(dpTree);

                                        break;

                                    default:
                                        relationTreeList.add(TreeUtils.buildPT(rpTree, mention1, mention2));
                                }

                                if (relation != null) {
                                    relation.setRelationTree(relationTreeList);
                                    // length_tree_size += ((RECKCTTreeNodeImpl)relation.getRelationTree().get(1)).getLeaves().size();
                                    nbr_avail_rels++;
                                } // if
                                else {
                                    if ( (relationTreeList != null) && (relationTreeList.size() > 0) ) {
                                        relation = new RelationImpl("NONE", mention1, mention2, relationTreeList);
                                        relation.setRECKParseTree(rpTree);
                                        nbr_none_rels++;
                                    }
                                } // else

                                if ((relation != null) && (!possibleRelationList.contains(relation))) {
                                    TreeUtils.getTargetTree(relation);
                                    possibleRelationList.add(relation);
                                    markDocumentList.add(mark);
                                    foldRelationList[ifold].add(relation);
                                    markFoldList[ifold].add(mark);
                                }
                            }
                        }
                    }
                }
            }
        }
        nbr_relations = Statistics.nbr_relations = possibleRelationList.size();
        // avg_tree_size = length_tree_size/(nbr_avail_rels/3);
        System.out.println("Generation completed");
        System.out.println("Number of all relations = " + Statistics.nbr_relations);
        System.out.println("Number of available relations =  = " + nbr_avail_rels);
        System.out.println("Number of negative relations = " + nbr_none_rels);
        // System.out.println("Total length of all relation tree = " + length_tree_size);
        // System.out.println("Average length of all relation tree = " + avg_tree_size);
    }

    /**
     * Separate relation instances in training/test sets
     * of n folds
     */
    public void separatingTrainingAndTestSetWithFolds() {

        ArrayList trainingRelationList, testRelationList;
        ArrayList<String> markTrainList, markTestList;
        int nbr_training = 0, nbr_test = 0;
        int i, j;

        for (i = 0; i < reckParams.nbfolds; i++) {
            trainingRelationList = new ArrayList();
            testRelationList = new ArrayList();
            markTrainList = new ArrayList();
            markTestList = new ArrayList();

            nbr_test = foldRelationList[i].size();
            nbr_training = nbr_relations - nbr_test;

            System.out.println("Fold no" + i);
            System.out.println("    Training set: " + nbr_training + " relation instances");
            System.out.println("    Test set    : " + nbr_test + " relation instances");

            testRelationList.addAll(foldRelationList[i]);
            markTestList.addAll(markFoldList[i]);
            
            for (j = 0; j < reckParams.nbfolds; j++) {
                if (j != i) {
                    trainingRelationList.addAll(foldRelationList[j]);
                    markTrainList.addAll(markFoldList[j]);
                }
            }

            if (reckParams.tree_type == 0)
                writeToKernelFile(i, nbr_training, nbr_test, trainingRelationList, testRelationList,
                        markTrainList, markTestList, reckParams.outputFilename);
            else
                writeToKernelFile_All(i, nbr_training, nbr_test, trainingRelationList, testRelationList,
                        markTrainList, markTestList, reckParams.outputFilename);
        }
    }

    /**
     * Create n folds for cross-validation
     * by splitting documents in order.
     */
    public void creatingFolds() {
        // nfold = new int[]{57, 87, 75, 43, 86};
        nfold = new int[reckParams.nbfolds];
        nbr_fold = (int)(Statistics.nbr_documents/reckParams.nbfolds);
        int nbr_last_fold = Statistics.nbr_documents - nbr_fold*(reckParams.nbfolds - 1);
        for (int i = 0; i < reckParams.nbfolds - 1; i++)
            nfold[i] = nbr_fold;
        nfold[reckParams.nbfolds - 1] = nbr_last_fold;
    }

    /**
     * from binary to multi-class classification
     * one-vs-rest approach
     * @inputFilename: all output files (one file/class) from SVM-Light
     * @outputFilename: one output file, each line contains corresponding class
     */
    public void oneVsRest(String inputFilename, String outputFilename) {
       
        int i, j, nbr_rel_types = RECKConstants.relationTypes.size();
        float max;
        int clas = -1;
        boolean eof = false;
        
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename))));
            BufferedReader reader[] = new BufferedReader[nbr_rel_types];
            for (i = 1; i < nbr_rel_types; i++) {
                File f = new File(inputFilename + RECKConstants.fileSeparator + i + ".out");
                reader[i] = new BufferedReader(new InputStreamReader(
                        new FileInputStream(f)));
            }
            
            while (!eof) {
                String line = reader[1].readLine();
                if (line == null)
                    eof = true;
                else {
                    max = Float.parseFloat(line);
                    clas = 1;
                    for (j = 2; j < nbr_rel_types; j++) {
                        line = reader[j].readLine();
                        float f = Float.parseFloat(line);
                        if (f > max) {
                            max = f;
                            clas = j;
                        }
                    }
                    if (max >= -0.15) {
                        writer.write(new Integer(clas).toString());
                    }
                    else {
                        writer.write(new Integer(0).toString());
                    }
                    writer.newLine();
                }
            }
            
            for (i = 1; i < nbr_rel_types; i++) {
                reader[i].close();
            }
            writer.close();
        }
        catch (java.io.FileNotFoundException fe) {
            fe.printStackTrace();
        }
        catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * Compare gold vs. test out come
     * for evaluation Precision, Recall, F-measure
     */
    public void compare(String input1, String input2) {
        // input1: "Gold" standard        
        // input2: Test outcome
        
        int TP = 0, FP = 0, FN = 0, TN = 0;
        int nbr_rel_types = RECKConstants.relationTypes.size();
        int TPperClass[] = new int[nbr_rel_types], 
                FPperClass[] = new int[nbr_rel_types], 
                FNperClass[] = new int[nbr_rel_types], 
                TNperClass[] = new int[nbr_rel_types];
        
        try {
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                    new FileInputStream(input1)));
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(
                    new FileInputStream(input2)));
            
            boolean eof = false;
            
            while (!eof) {
                String clas1 = reader1.readLine();
                String clas2 = reader2.readLine();

                if ((clas1 == null) || (clas2 == null))
                    eof = true;
                else {
                    int intClas1 = Integer.parseInt(clas1);
                    int intClas2 = Integer.parseInt(clas2);
                    
                    if ( (intClas1 != 0) && (intClas1 == intClas2) ) {
                        TP++;
                        TPperClass[intClas1]++;
                    }
                    else if ( (intClas1 != 0) && (intClas1 != intClas2)) {
                        FN++;
                        FNperClass[intClas1]++;
                    }
                    else if ( (intClas1 == 0) && (intClas2 != 0)) {
                        FP++;
                        FPperClass[intClas2]++;
                    }
                    else // if ( (intClas1 == intClas2) && (intClas1 == 0) ) 
                    {
                        TN++;
                        for (int i = 1; i < nbr_rel_types; i++)
                            TNperClass[i]++;
                    }
                }
            }
            
            reader1.close();
            reader2.close();
            
            float p = (float)TP/(TP + FP);
            float r = (float)TP/(TP + FN);
            float f1 = 2*(p*r)/(p+r);
            
            System.out.println("ALL CLASSES:");
            System.out.println("\t\tTrue Positive   = " + TP);
            System.out.println("\t\tFalse Positive  = " + FP);
            System.out.println("\t\tTrue Negative   = " + TN);
            System.out.println("\t\tFalse Negative  = " + FN);
            System.out.println("\t\tPrecision       = " + p);
            System.out.println("\t\tRecall          = " + r);
            System.out.println("\t\tF1              = " + f1);
            
            for (int i = 1; i < nbr_rel_types; i++) {
                float pr = (float)TPperClass[i]/(TPperClass[i] + FPperClass[i]);
                float re = (float)TPperClass[i]/(TPperClass[i] + FNperClass[i]);
                float fm = 2*(pr*re)/(pr+re);
                
                System.out.println("\t\tCLASS " + i + ":");
                System.out.println("\t\t\t\tTrue Positive   = " + TPperClass[i]);
                System.out.println("\t\t\t\tFalse Positive  = " + FPperClass[i]);
                System.out.println("\t\t\t\tTrue Negative   = " + TNperClass[i]);
                System.out.println("\t\t\t\tFalse Negative  = " + FNperClass[i]);
                System.out.println("\t\t\t\tPrecision       = " + pr);
                System.out.println("\t\t\t\tRecall          = " + re);
                System.out.println("\t\t\t\tF1              = " + fm);                
            }
        }
        catch (java.io.FileNotFoundException fe) {
            fe.printStackTrace();
        }
        catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

       
    }
    
    /**
     * The list that holds the documents in this corpus.
     */
    protected ArrayList documentList = new ArrayList();
    protected ArrayList relationListACE = new ArrayList();
    protected ArrayList possibleRelationList = new ArrayList();
    protected ArrayList<String> markDocumentList = new ArrayList();
    
    // 5-fold cross-validation
    int[] nfold = null;
    protected ArrayList[] foldRelationList = {new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList()};
    
    // 5-fold cross-validation
    protected ArrayList[] markFoldList = {new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList()};
    
    public int nbr_entities = 0, nbr_relations = 0;    
    public int nbr_none_rels = 0, nbr_avail_rels = 0;
    public int nbr_fold = 0;
    public int avg_tree_size = 0;
    
}
