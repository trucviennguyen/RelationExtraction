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

package reck.trees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import reck.Mention;
import reck.RECKParseTree;
import reck.util.Charseq;

/**
 * A class representing a complex structure of a sentence.
 * It contains a constituent, a dependency parse tree
 * with the position in the sentence.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RECKParseTreeImpl implements RECKParseTree, Cloneable, Serializable {
    
    public RECKParseTreeImpl(List sentence, ArrayList dpList, Charseq position, RECKDPTreeNodeImpl DPparseTree, RECKCTTreeNodeImpl CTparseTree) {
        this.sentence = sentence;
        this.dpList = dpList;
        this.position = position;
        this.DPparseTree = DPparseTree;
        this.CTparseTree = CTparseTree;
        this.buildDPTreeList();
    }
    
    public Charseq getPosition() {
        return position;
    }
    
    public void setPosition(Charseq position) {
        this.position = position;
    }
    
    // Object can be a LabeledScoredTreeNode or a list of LabeledScoredTreeNode
    public RECKDPTreeNodeImpl getDPParseTree() {
        return DPparseTree;
    }
    
    public void setDPParseTree(RECKDPTreeNodeImpl DPparseTree) {
        this.DPparseTree = DPparseTree;
        this.buildDPTreeList();
    }

    public void buildDPTreeList() {
        
        ArrayList<RECKDPTreeNodeImpl> treeList = new ArrayList(DPparseTree.subTreeList());
        
        if (treeList.size() > 1) {
        
            reckDPTreeList.add(treeList.get(1));
            for (int i = 2; i < treeList.size(); i++) {
                RECKDPTreeNodeImpl treeNode = treeList.get(i);
                int j = 0;
                while ( (j < reckDPTreeList.size()) && (treeNode.index() > reckDPTreeList.get(j).index()) )
                    j++;
                reckDPTreeList.add(j, treeNode);
            }
            
        }
    }

    public void setDPTreeList(ArrayList reckDPTreeList) {
        this.reckDPTreeList = reckDPTreeList;
    }
    
    public ArrayList getDPTreeList() {
        return reckDPTreeList;
    }

    
    public void addToDPEntityTrees(Mention mention, ArrayList nodeList) {
        DPmentionTrees.put(mention, nodeList);
    }
    
    public Hashtable getDPEntityTrees() {
        return DPmentionTrees;
    }
    
    /** Find the tree corresponding to an entity */
    public ArrayList getDPTreeListByEntity(Mention m) {
        return (ArrayList)DPmentionTrees.get(m);
    }
    
    public RECKDPTreeNodeImpl getDPTreeByEntity(Mention m) {
        RECKDPTreeNodeImpl rtree = null;
        ArrayList treeList = getDPTreeListByEntity(m);
        if (treeList != null && !treeList.isEmpty())
            rtree = (RECKDPTreeNodeImpl)treeList.get(0);
        
        return rtree;
    }
    
    public RECKDPTreeNodeImpl getDPTreeByEntityPosition(Mention m, Charseq pos) {
        ArrayList treeList = getDPTreeListByEntity(m);
        for (int i = 0; i < treeList.size(); i++) {
            RECKDPTreeNodeImpl treenode = (RECKDPTreeNodeImpl)treeList.get(i);
            if (treenode.getPosition().equals(pos)) {
                return treenode;
            }
        }
        return null;
    }
    
    // Object can be a LabeledScoredTreeNode or a list of LabeledScoredTreeNode
    public RECKCTTreeNodeImpl getCTParseTree() {
        return CTparseTree;
    }
    
    public void setCTParseTree(RECKCTTreeNodeImpl CTparseTree) {
        this.CTparseTree = CTparseTree;
    }
    
    public ArrayList getDependencyList() {
        return dpList;
    }
    
    
    public void setDependencyList(ArrayList dpList) {
        this.dpList = dpList;
    }

    
    public void addToCTEntityTrees(Mention mention, ArrayList nodeList) {
        CTmentionTrees.put(mention, nodeList);
    }
    
    public Hashtable getCTEntityTrees() {
        return CTmentionTrees;
    }
    
    /** Find the tree corresponding to an entity */
    public ArrayList getCTTreeListByEntity(Mention m) {
        return (ArrayList)CTmentionTrees.get(m);
    }
    
    public RECKCTTreeNodeImpl getCTTreeByEntity(Mention m) {
        RECKCTTreeNodeImpl rtree = null;
        ArrayList treeList = getCTTreeListByEntity(m);
        if (treeList != null && !treeList.isEmpty())
            rtree = (RECKCTTreeNodeImpl)treeList.get(0);
        
        return rtree;
    }
    
    public RECKCTTreeNodeImpl getCTTreeByEntityPosition(Mention m, Charseq pos) {
        ArrayList treeList = getCTTreeListByEntity(m);
        for (int i = 0; i < treeList.size(); i++) {
            RECKCTTreeNodeImpl treenode = (RECKCTTreeNodeImpl)treeList.get(i);
            if (treenode.getPosition().equals(pos)) {
                return treenode;
            }
        }
        return null;
    }
    
    Charseq position = null;
    
    List sentence = null;
    ArrayList dpList = new ArrayList();
    
    // Object can be a LabeledScoredTreeNode or a list of LabeledScoredTreeNode
    RECKDPTreeNodeImpl DPparseTree = null;
    RECKCTTreeNodeImpl CTparseTree = null;
    Hashtable CTmentionTrees = new Hashtable();
    Hashtable DPmentionTrees = new Hashtable();
    ArrayList<RECKDPTreeNodeImpl> reckDPTreeList = new ArrayList();
}
