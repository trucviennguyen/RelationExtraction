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

import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import reck.Mention;
import reck.Relation;
import reck.trees.RECKCTTreeNodeImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.trees.RECKParseTreeImpl;
import reck.trees.RECKTypedDependency;

/**
 * A set of utility functions to process trees.
 * 
 * @author Truc-Vien T. Nguyen
 */
public final class TreeUtils {

    public static final void getTargetTree(Relation relation) {
        RECKDPTreeNodeImpl currentDPNode, child;
        RECKCTTreeNodeImpl currentCTNode;
        Long start, end;        
        Mention mention1 = relation.getMention(0), mention2 = relation.getMention(1);
        String t1 = "", t2 = "";
        
        StringLabel newLabel = null;
        Charseq newPosition = null;
        RECKDPTreeNodeImpl newNode = null;
        
        if (RECKConstants.mentionOrder(mention1, mention2)) {
            t1 = "T1-";
            t2 = "T2-";
        }
        else {
            t1 = "T2-";
            t2 = "T1-";
        }
        
        /* if (mention2.getId().equals("32-77")) 
            System.out.println("");*/
        
        ArrayList treeList = relation.getRelationTree();
        ArrayList open = new ArrayList(), close = new ArrayList();

        for (int i = 0; i < treeList.size(); i++) {

            Object relationTree = treeList.get(i);
            
            open.add(relationTree);
            boolean foundM1 = false, foundM2 = false;

            while ( !open.isEmpty() ) {
                
                Object o = open.remove(open.size() - 1);
                if (o instanceof RECKDPTreeNodeImpl) {
                    currentDPNode = (RECKDPTreeNodeImpl)o;
                    
                    start = new Long(currentDPNode.getPosition().getStart().longValue() );
                    end  = new Long(currentDPNode.getPosition().getEnd().longValue() - 1);

                    if (!foundM1 && (mention1.getEntity().getType().equals(currentDPNode.label().value())) && RECKConstants.DPMatching(mention1, start, end) ) {
                        currentDPNode.setLabel(new StringLabel(t1 + currentDPNode.label().value()));
                        foundM1 = true;
                    }

                    else if (!foundM2 && (mention2.getEntity().getType().equals(currentDPNode.label().value())) && RECKConstants.DPMatching(mention2, start, end) ) {
                        currentDPNode.setLabel(new StringLabel(t2 + currentDPNode.label().value()));
                        foundM2 = true;
                    }
                    else if (!RECKConstants.entityTypes.contains(currentDPNode.label().value()) && (i == 1) ) {
                        if ( (currentDPNode.role() != null) && (!currentDPNode.role().trim().equals("")) ) {
                            String tmp = currentDPNode.label().toString();
                            currentDPNode.setLabel(new StringLabel(currentDPNode.role()));
                            currentDPNode.setRole(tmp);
                        }
                    }
                    
                    open.addAll(currentDPNode.getChildrenAsList());
                    close.add(currentDPNode);

                    if (i == 3) {
                        Tree[] children = currentDPNode.children();
                        for (int j = 0; j < children.length; j++) {
                            child = (RECKDPTreeNodeImpl)children[j];
                            if (!RECKConstants.entityTypes.contains(child.label().value())) {
                                newLabel = new StringLabel(child.role());
                                newPosition = child.getPosition().clone();
                                Tree[] newChildren = {child};
                                newNode = new RECKDPTreeNodeImpl(newLabel, newChildren, newPosition) ;
                                currentDPNode.setChild(j, newNode);
                            }
                        }
                    }
                }
                else {
                    currentCTNode = (RECKCTTreeNodeImpl)o;
                    start = new Long(currentCTNode.getPosition().getStart().longValue() );
                    end  = new Long(currentCTNode.getPosition().getEnd().longValue() - 1);

                    if (!foundM1 && (mention1.getEntity().getType().equals(currentCTNode.label().value())) && RECKConstants.newMatching(mention1, start, end) ) {
                        currentCTNode.setLabel(new StringLabel(t1 + currentCTNode.label().value()));
                        foundM1 = true;
                    }

                    else if (!foundM2 && (mention2.getEntity().getType().equals(currentCTNode.label().value())) && RECKConstants.newMatching(mention2, start, end) ) {
                        currentCTNode.setLabel(new StringLabel(t2 + currentCTNode.label().value()));
                        foundM2 = true;
                    }

                    open.addAll(currentCTNode.getChildrenAsList());
                    close.add(currentCTNode);
                }

            }
        }
    }
    
    /** get a copy of the Path-enclosed Tree */
    public static final RECKDPTreeNodeImpl buildDependencyPT(RECKParseTreeImpl rpTree, Mention m1, Mention m2) {
        
        // get a copy to re-assign old value later
        RECKDPTreeNodeImpl parseTree = rpTree.getDPParseTree();
        RECKDPTreeNodeImpl rtree = null;
        RECKDPTreeNodeImpl node1 = rpTree.getDPTreeByEntity(m1);
        RECKDPTreeNodeImpl node2 = rpTree.getDPTreeByEntity(m2);
        
        if (node1 != null && node2 != null) {
            if (node1.getPosition().getStart().longValue() > node2.getPosition().getStart().longValue()) {
                Mention tempM = m1;
                m1 = m2;
                m2 = tempM;
                
                RECKDPTreeNodeImpl tempN = node1;
                node1 = node2;
                node2 = tempN;
            }
            
            rtree = buildDependencyPT(parseTree, node1, node2);
            
            /* if (!RECKConstants.mentionOrder(m1, m2))
                reverseDPTreeDirection(rtree);*/
            
        }
        else {
            System.err.println("Relation out of one sentence between " + m1.getId() + " and " + m2.getId());
            Statistics.nbr_out_relations++;
        }
        
        return rtree;
    } // buildDependencyPT
    
    /** get a copy of the Path-enclosed Tree */
    public static final RECKDPTreeNodeImpl buildDependencyPT(RECKDPTreeNodeImpl parseTree, RECKDPTreeNodeImpl node1, RECKDPTreeNodeImpl node2) {
        
        // get a copy to re-assign old value later
        RECKDPTreeNodeImpl rtree = null;
        boolean reach = false;
        
        RECKDPTreeNodeImpl currentNode = node1;

        if (node1.subTreeList().contains(node2))
            return buildDependencyPT_Contains(parseTree, node1, node2);
        else if (node2.subTreeList().contains(node1))
            return buildDependencyPT_Contains(parseTree, node2, node1);

        RECKDPTreeNodeImpl parent = currentNode.clone(), nextNode = null, child = null;

        // go up one level until find common ancestor
        while (!reach) {
            nextNode = (RECKDPTreeNodeImpl)currentNode.parent(parseTree);
            int index = nextNode.indexOf(currentNode);

            child = parent;
            parent = nextNode.clone();
            parent.setChild(index, child);
            parent.removeLeftChildren(index);

            currentNode = nextNode;

            reach = parent.subTreeList().contains(node2);
            
            if (!reach && (index < nextNode.numChildren() - 1) ) {
                parent.removeRightChildren(1);
            }
        }

        // common ancestor
        rtree = parent;

        currentNode = node2;
        nextNode = null;
        parent = currentNode.clone();
        // go up one level until encounter common ancestor
        while (currentNode != rtree) {
            nextNode = (RECKDPTreeNodeImpl)currentNode.parent(rtree);
            int index = nextNode.indexOf(currentNode);

            child = parent;
            parent = nextNode;
            parent.setChild(index, child);

            if (index < nextNode.numChildren() - 1) {
                parent.removeRightChildren(index + 1);
            }

            currentNode = nextNode;
            
            if (currentNode != rtree) 
                parent.removeLeftChildren(index);            
        }
        
        removeChildrenBetween(rtree, 0, rtree.numChildren() - 1);
        
        return rtree;
    } // buildDependencyPT
    
    private static void removeChildrenBetween(RECKDPTreeNodeImpl node, int l, int r) {
        Tree[] kids = node.children();
        Tree newKids[] = new Tree[] {kids[l], kids[r]};
        node.setChildren(newKids);
    }
    
    public static final RECKDPTreeNodeImpl buildDependencyPT_Contains(RECKDPTreeNodeImpl parseTree, RECKDPTreeNodeImpl node1, RECKDPTreeNodeImpl node2) {
        
        // get a copy to re-assign old value later
        RECKDPTreeNodeImpl rtree = null;
        boolean reach = false;
        
        RECKDPTreeNodeImpl currentNode = node2;

        RECKDPTreeNodeImpl parent = currentNode.clone(), nextNode = null, child = null;

        while (!reach) {
            nextNode = (RECKDPTreeNodeImpl)currentNode.parent(parseTree);
            int index = nextNode.indexOf(currentNode);

            child = parent;
            parent = nextNode.clone();
            parent.setChild(index, child);
            parent.removeLeftChildren(index);

            if (index < nextNode.numChildren() - 1) {
                parent.removeRightChildren(1);
            }

            currentNode = nextNode;

            reach = (currentNode == node1);
        }

        rtree = parent;
        
        return rtree;
    } // buildDependencyPT
    
    /** get a copy of the Path-enclosed Tree */
    public static final RECKCTTreeNodeImpl buildPT(RECKParseTreeImpl rpTree, Mention m1, Mention m2) {
        
        // get a copy to re-assign old value later
        RECKCTTreeNodeImpl parseTree = rpTree.getCTParseTree();
        RECKCTTreeNodeImpl rtree = null;
        RECKCTTreeNodeImpl node1 = rpTree.getCTTreeByEntity(m1);
        RECKCTTreeNodeImpl node2 = rpTree.getCTTreeByEntity(m2);
        
        if (node1 != null && node2 != null) {
            if (node1.getPosition().getStart().longValue() > node2.getPosition().getStart().longValue()) {
                Mention tempM = m1;
                m1 = m2;
                m2 = tempM;
                
                RECKCTTreeNodeImpl tempN = node1;
                node1 = node2;
                node2 = tempN;
            }
            
            rtree = buildPT(parseTree, node1, node2);
            
        }
        else {
            System.err.println("Relation out of one sentence between " + m1.getId() + " and " + m2.getId());
            Statistics.nbr_out_relations++;
        }
        
        return rtree;
    } // buildPT
    
    /** get a copy of the Path-enclosed Tree */
    public static final RECKCTTreeNodeImpl buildPT(RECKCTTreeNodeImpl parseTree, RECKCTTreeNodeImpl node1, RECKCTTreeNodeImpl node2) {
        
        // get a copy to re-assign old value later
        RECKCTTreeNodeImpl rtree = null;
        boolean reach = false;
        
        RECKCTTreeNodeImpl currentNode = node1;

        if (node1.subTreeList().contains(node2))
            return node1.clone();
        else if (node2.subTreeList().contains(node1))
            return node2.clone();

        RECKCTTreeNodeImpl parent = currentNode.clone(), nextNode = null, child = null;

        while (!reach) {
            nextNode = (RECKCTTreeNodeImpl)currentNode.parent(parseTree);
            int index = nextNode.indexOf(currentNode);

            child = parent;
            parent = nextNode.clone();
            parent.setChild(index, child);
            parent.removeLeftChildren(index);   

            currentNode = nextNode;

            reach = parent.subTreeList().contains(node2);
        }

        rtree = parent;

        currentNode = node2;
        nextNode = null;
        parent = currentNode.clone();
        while (currentNode != rtree) {
            nextNode = (RECKCTTreeNodeImpl)currentNode.parent(rtree);
            int index = nextNode.indexOf(currentNode);

            child = parent;
            parent = nextNode;
            parent.setChild(index, child);

            if (index < nextNode.numChildren() - 1) {
                parent.removeRightChildren(index + 1);
            }

            currentNode = nextNode;
        }
        
        return rtree;
    } // getPT for nodes

  /** Print the internal part of a tree having already identified it.
   *  The ID and outer XML element is printed wrapping this method, but none
   *  of the internal content.
   *
   * @param t The tree to print. Now known to be non-null
   * @param pw Where to print it to
   * @param inXml Whether to use XML style printing
   */
  
    public static final ArrayList getDependencyList(RECKParseTreeImpl rpTree, final RECKCTTreeNodeImpl CTTree) {
        
        ArrayList TDs = rpTree.getDependencyList();
        if (TDs.isEmpty())
            return null;
        
        ArrayList rpLeaves = new ArrayList(rpTree.getCTParseTree().getLeaves());
        
        ArrayList GRs = new ArrayList();
        ArrayList leaves = new ArrayList(CTTree.getLeaves());
        int i = 0, j;
        RECKCTTreeNodeImpl leaf = (RECKCTTreeNodeImpl)leaves.get(i);
        Object o = null;
        String reln = null;
        RECKCTTreeNodeImpl rpleaf = null;

        for (j = 0; j < TDs.size(); j++) {
            rpleaf = (RECKCTTreeNodeImpl)rpLeaves.get(j);
            o = TDs.get(j);
            if (o instanceof RECKTypedDependency)
                reln = ((RECKTypedDependency)o).reln();
            else {
                int k = ((Integer)o).intValue();
                if (k == -1) reln = rpleaf.label().toString();
                else reln = "root";
            }
            if (rpleaf.getPosition().getStart().intValue() == leaf.getPosition().getStart().intValue())
                break;
        }

        while (rpleaf.getPosition().getStart().intValue() == leaf.getPosition().getStart().intValue()) {
            GRs.add(reln);
            if (i < leaves.size() - 1) {
                o = TDs.get(++j);
                if (o instanceof RECKTypedDependency)
                    reln = ((RECKTypedDependency)o).reln();
                else {
                    int k = ((Integer)o).intValue();
                    if (k == -1) reln = rpleaf.label().toString();
                    else reln = "root";
                }
                rpleaf = (RECKCTTreeNodeImpl)rpLeaves.get(j);
                leaf = (RECKCTTreeNodeImpl)leaves.get(++i);
                while (rpleaf.getPosition().getStart().intValue() < leaf.getPosition().getStart().intValue()) {
                    o = TDs.get(++j);
                    if (o instanceof RECKTypedDependency)
                        reln = ((RECKTypedDependency)o).reln();
                    else {
                        int k = ((Integer)o).intValue();
                        if (k == -1) reln = rpleaf.label().toString();
                        else reln = "root";
                    }
                    rpleaf = (RECKCTTreeNodeImpl)rpLeaves.get(j);   
                }
            }
            else
                break;
        }
        
        return GRs;

    }
    
    public static final ArrayList getDependencyPath(final RECKDPTreeNodeImpl DPTree) {
        
        ArrayList path = new ArrayList();
        RECKDPTreeNodeImpl dp;
        
        if (DPTree.numChildren() == 1) {
            dp = (RECKDPTreeNodeImpl)DPTree.firstChild();
            // Child Branch on the left
            if (dp.getPosition().getStart().intValue() < DPTree.getPosition().getStart().intValue()) {
                dp = (RECKDPTreeNodeImpl)DPTree.getLeaves().get(0);
                while (!dp.equals(DPTree)) {
                    path.add(dp);
                    dp = dp.parent(DPTree);
                }
                path.add(DPTree);
            }
            // Child Branch on the right
            else {                
                dp = DPTree;
                while (!dp.isLeaf()) {
                    path.add(dp);
                    dp = (RECKDPTreeNodeImpl)dp.firstChild();
                }
                path.add(dp);
            }
        }
        
        else if (DPTree.numChildren() == 2) {
            dp = (RECKDPTreeNodeImpl)DPTree.getLeaves().get(0);
            // Construct Path on the left
            while (!dp.equals(DPTree)) {
                path.add(dp);
                dp = dp.parent(DPTree);
            }
            path.add(DPTree);
            
            dp = (RECKDPTreeNodeImpl)dp.getChild(1);
            // Construct Path on the right
            while (!dp.isLeaf()) {
                path.add(dp);
                dp = (RECKDPTreeNodeImpl)dp.firstChild();
            }
            path.add(dp);
        }
            
        for (int k = 0; k < path.size(); k++) {
            dp = (RECKDPTreeNodeImpl)path.get(k);
            String label = dp.label().toString();
            String role = dp.role();
            if ((role !=null) && (!role.equals(""))) dp.setLabel(new StringLabel(role));
            if ((label !=null) && (!label.equals(""))) dp.setRole(label);
        }
        
        return path;
    }
    
}
