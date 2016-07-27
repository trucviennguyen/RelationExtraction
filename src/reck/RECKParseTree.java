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

package reck;

import java.util.ArrayList;
import java.util.Hashtable;
import reck.trees.RECKCTTreeNodeImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.util.Charseq;

/**
 * An interface that act as a parse tree
 * containing the constituent/
 * dependency parse trees
 * 
 * @author Truc Vien
 */
public interface RECKParseTree {

    /**
     * Return the position of the parse tree (of a sentence)
     * in the document corresponding text.
     *
     * @return Charseq the object that contains the start
     * and end of the sentence
     */
    public Charseq getPosition();

    /**
     * Set the position for the parse tree
     *
     * @param position The position for the parse tree
     */
    public void setPosition(Charseq position);
    
    /**
     * Return the dependency parse tree.
     *
     * @return RECKDPTreeNodeImpl the dependency tree structure
     */
    public RECKDPTreeNodeImpl getDPParseTree();

    /**
     * Set the dependency parse tree.
     *
     * @param parseTree the dependency tree
     */
    public void setDPParseTree(RECKDPTreeNodeImpl parseTree);

    /**
     * Return a list of subtrees of the dependency parse tree.
     *
     * @return ArrayList the list of subtrees
     */
    public void setDPTreeList(ArrayList reckTreeList);

    /**
     * Return a list of subtrees of the dependency parse tree.
     *
     * @return ArrayList the list of subtrees
     */
    public ArrayList getDPTreeList();
    public void addToDPEntityTrees(Mention mention, ArrayList nodeList);

    public Hashtable getDPEntityTrees();
    
    /** Find the tree corresponding to an entity mention */
    public ArrayList getDPTreeListByEntity(Mention m);
    
    /** Find the first tree corresponding to an entity 
     */
    public RECKDPTreeNodeImpl getDPTreeByEntity(Mention m);
    
    /** Find the tree corresponding to an entity given its position */
    public RECKDPTreeNodeImpl getDPTreeByEntityPosition(Mention m, Charseq pos);
    
    // Object can be a LabeledScoredTreeNode or a list of LabeledScoredTreeNode
    public RECKCTTreeNodeImpl getCTParseTree();
    
    public void setCTParseTree(RECKCTTreeNodeImpl parseTree);
    
    public ArrayList getDependencyList();
    public void setDependencyList(ArrayList dpList);

    
    public void addToCTEntityTrees(Mention mention, ArrayList nodeList);
    
    public Hashtable getCTEntityTrees();
    
    /** Find the tree corresponding to an entity */
    public ArrayList getCTTreeListByEntity(Mention m);
    
    public RECKCTTreeNodeImpl getCTTreeByEntity(Mention m);
    
    public RECKCTTreeNodeImpl getCTTreeByEntityPosition(Mention m, Charseq pos);
    
}
