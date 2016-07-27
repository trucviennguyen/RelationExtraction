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

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import reck.util.Charseq;

/**
 * A class extending the <code>Tree</code> class,
 * representing a node in the dependency parse tree.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RECKDPTreeNodeImpl extends Tree implements Cloneable, Serializable {

    /**
     * A leaf node should have a zero-length array for its
     * children. For efficiency, subclasses can use this array as a
     * return value for children() for leaf nodes if desired. Should
     * this be public instead?
     */
    protected static final RECKDPTreeNodeImpl[] ZERONODETREES = new RECKDPTreeNodeImpl[0];
    
    /**
     * Label of the parse tree.
     */
    private Label label;
  
    /**
     * Daughters of the parse tree.
     */
    private Tree[] daughterTrees;

    /**
     * Create parse tree with given root and array of daughter trees.
     *
     * @param label             root label of tree to construct.
     * @param daughterTreesList List of daughter trees to construct.
     */
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(ZERONODETREES);
    }
    
    /**
     * Create parse tree with given root and array of daughter trees.
     *
     * @param label             root label of tree to construct.
     * @param daughterTreesList List of daughter trees to construct.
     */
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent, Tree daughterTreeArray[]) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(daughterTreeArray);
    }
    
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent, List<Tree> daughterTreesList) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(daughterTreesList);
    }
    
    public RECKDPTreeNodeImpl(Label label, Charseq position) {
        this.label = label;
        setChildren(ZERONODETREES);
        this.position = position;
    }
    
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent, Charseq position) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(ZERONODETREES);
        this.position = position;
    }

    public RECKDPTreeNodeImpl(Label label, Tree daughterTreeArray[], Charseq position) {
        this.label = label;
        setChildren(daughterTreeArray);
        this.position = position;
    }

    /**
     * Create parse tree with given root and array of daughter trees.
     *
     * @param label             root label of tree to construct.
     * @param daughterTreesList List of daughter trees to construct.
     */
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent, Tree daughterTreeArray[], Charseq position) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(daughterTreeArray);
        this.position = position;
    }
    
    public RECKDPTreeNodeImpl(Label label, List<Tree> daughterTreesList, Charseq position) {
        this.label = label;
        setChildren(daughterTreesList);
        this.position = position;
    }
    
    public RECKDPTreeNodeImpl(Label label, int idx, String role, String constituent, List<Tree> daughterTreesList, Charseq position) {
        this.label = label;
        this.idx = idx;
        this.role = role;
        this.constituent = constituent;
        setChildren(daughterTreesList);
        this.position = position;
    }
    
    /**
     * Returns a copy of this.
     */
    @Override
    public RECKDPTreeNodeImpl clone() {
        RECKDPTreeNodeImpl rtree = null;
        Label newLabel = null;
        Charseq newPosition = null;

        if (label != null)
            newLabel = label.labelFactory().newLabel(label);
        if (position != null)
            newPosition = position.clone();
        
        Tree kids[] = children();
        List<Tree> newKids = new ArrayList<Tree>(kids.length);
        for (int i = 0, n = kids.length; i < n; i++) {
            newKids.add(((RECKDPTreeNodeImpl)kids[i]).clone());
        }
        rtree = new RECKDPTreeNodeImpl(newLabel, idx, role, constituent, newKids, newPosition); 
        
        return rtree;
    }
    
    public boolean isEntity() {
        if (label instanceof StringLabel) {
            StringTokenizer tokenizer = new StringTokenizer(label.value());
            if (tokenizer.countTokens() == 4)
                return true;
        }
        return false;
    }
    
    public String getEntityType() {
        if (this.isEntity()) {
            StringTokenizer tokenizer = new StringTokenizer(label.value());
            tokenizer.nextElement();
            return (String)tokenizer.nextElement();
        }
        return null;
    }
    
    
    /**
     * Returns an array of children for the current node, or null
     * if it is a leaf.
     */
    public Tree[] children() {
        return daughterTrees;
    }

    /**
     * Sets the children of this <code>Tree</code>.  If given
     * <code>null</code>, this method prints a warning and sets the
     * Tree's children to the canonical zero-length Tree[] array.
     * Constructing a LabeledScoredTreeLeaf is preferable in this
     * case.
     *
     * @param children An array of child trees
     */
    public void setChildren(Tree[] children) {
        if (children == null) {
            System.err.println("Warning -- you tried to set the children of a LabeledScoredTreeNode to null.\nYou really should be using a zero-length array instead.\nConsider building a LabeledScoredTreeLeaf instead.");
            daughterTrees = ZEROCHILDREN;
        } 
        else {
            daughterTrees = children;
        }
    }
    
    /**
     * Destructively removes all the children from the left the index i
     * Note
     * that this method will throw an {@link ArrayIndexOutOfBoundsException} if
     * the index is too big for the list of daughters.
     *
     * @param i The daughter index
     * @return The tree at that daughter index
     */
    public void removeLeftChildren(int i) {
        Tree[] kids = children();
        Tree[] newKids = new Tree[kids.length - i];
        for (int j = 0; j < newKids.length; j++) {
            newKids[j] = kids[i + j];
        }
        setChildren(newKids);
    }
    
    /**
     * Destructively removes all the children from the index i to the right
     * Note
     * that this method will throw an {@link ArrayIndexOutOfBoundsException} if
     * the index is too big for the list of daughters.
     *
     * @param i The daughter index
     * @return The tree at that daughter index
     */
    public void removeRightChildren(int i) {
        Tree[] kids = children();
        Tree[] newKids = new Tree[i];
        for (int j = 0; j < newKids.length; j++) {
            newKids[j] = kids[j];
        }
        setChildren(newKids);
    }

    /**
     * Returns the index associated with the current node, or -1
     * if there is no index
     */
    public int index() {
        return idx;
    }

    /**
     * Sets the index associated with the current node, if there is one.
     */
    public void setIndex(final int idx) {
        this.idx = idx;
    }

    /**
     * Returns the role associated with the current node, or -1
     * if there is no role
     */
    public String role() {
        return role;
    }

    /**
     * Sets the index associated with the current node, if there is one.
     */
    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * Returns the role associated with the current node, or -1
     * if there is no role
     */
    public String constituent() {
        return constituent;
    }

    /**
     * Sets the index associated with the current node, if there is one.
     */
    public void setConstituent(final String constituent) {
        this.constituent = constituent;
    }

    /**
     * Returns the label associated with the current node, or null
     * if there is no label
     */
    @Override
    public Label label() {
        return label;
    }

    /**
     * Sets the label associated with the current node, if there is one.
     */
    @Override
    public void setLabel(final Label label) {
        this.label = label;
    }

    /**
     * Appends the printed form of a parse tree (as a bracketed String)
     * to a <code>StringBuffer</code>.
     *
     * @return StringBuffer returns the <code>StringBuffer</code>
     */
    @Override
    public StringBuffer toStringBuffer(StringBuffer sb) {
        sb.append("(");
        sb.append(nodeString());
        for (Tree daughterTree : daughterTrees) {
            sb.append(" ");
            daughterTree.toStringBuffer(sb);
        }
        return sb.append(")");
    }

    /**
     * Return a <code>TreeFactory</code> that produces trees of the
     * same type as the current <code>Tree</code>.  That is, this
     * implementation, will produce trees of type
     * <code>LabeledScoredTree(Node|Leaf)</code>.
     * The <code>Label</code> of <code>this</code>
     * is examined, and providing it is not <code>null</code>, a
     * <code>LabelFactory</code> which will produce that kind of
     * <code>Label</code> is supplied to the <code>TreeFactory</code>.
     * If the <code>Label</code> is <code>null</code>, a
     * <code>StringLabelFactory</code> will be used.
     * The factories returned on different calls a different: a new one is
     * allocated each time.
     *
     * @return a factory to produce labeled, scored trees
     */
    public TreeFactory treeFactory() {
        LabelFactory lf;
        if (label() != null) {
            lf = label().labelFactory();
        } 
        else {
            lf = StringLabel.factory();
        }
        return new LabeledScoredTreeFactory(lf);
    }

    // extra class guarantees correct lazy loading (Bloch p.194)
    private static class TreeFactoryHolder {
        static final TreeFactory tf = new LabeledScoredTreeFactory();
    }

    /**
     * Return a <code>TreeFactory</code> that produces trees of the
     * <code>LabeledScoredTree{Node|Leaf}</code> type.
     * The factory returned is always the same one (a singleton).
     *
     * @return a factory to produce labeled, scored trees
     */
    public static TreeFactory factory() {
        return TreeFactoryHolder.tf;
    }

    /**
     * Return a <code>TreeFactory</code> that produces trees of the
     * <code>LabeledScoredTree{Node|Leaf}</code> type, with
     * the <code>Label</code> made with the supplied
     * <code>LabelFactory</code>.
     * The factory returned is a different one each time
     *
     * @param lf The LabelFactory to use
     * @return a factory to produce labeled, scored trees
     */
    public static TreeFactory factory(LabelFactory lf) {
        return new LabeledScoredTreeFactory(lf);
    }

    @Override
    public String nodeString() {
        StringBuilder buff = new StringBuilder();
        buff.append(super.nodeString());
        return buff.toString();
    }
    
    public Charseq getPosition() {
        return position;
    }
    
    public void setPosition(Charseq position) {
        this.position = position;
    }
    
    /**
     * Implements equality for Tree's.  Two Tree objects are equal if they
     * have equal Labels, the same number of children, and their children
     * are pairwise equal.
     *
     * @param o The object to compare with
     * @return Whether two things are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } 
        else if (!(o instanceof RECKDPTreeNodeImpl)) {
            return false;
        }
        RECKDPTreeNodeImpl t = (RECKDPTreeNodeImpl) o;
        if (!(label().equals(t.label()))) {
            return false;
        }
        
        if (position == null && t.getPosition() != null)
            return false;
        else if (t.getPosition() == null && position != null)
            return false;
        else if (!position.equals(t.getPosition()))
            return false;
        
        Tree[] mykids = children();
        Tree[] theirkids = t.children();
        //if((mykids == null && (theirkids == null || theirkids.length != 0)) || (theirkids == null && mykids.length != 0) || (mykids.length != theirkids.length)){
        if (mykids.length != theirkids.length) {
            return false;
        }
        for (int i = 0; i < mykids.length; i++) {
            if (!mykids[i].equals(theirkids[i])) {
                return false;
            }
        }        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.label != null ? this.label.hashCode() : 0);
        hash = 67 * hash + (this.position != null ? this.position.hashCode() : 0);
        return hash;
    }
    
    /**
     * Get the set of all subtrees inside the tree by returning a tree
     * rooted at each node.  These are <i>not</i> copies, but all share
     * structure.  The tree is regarded as a subtree of itself.
     * <p/>
     * <i>Note:</i> If you only want to form this Set so that you can
     * iterate over it, it is more efficient to simply use the Tree class's
     * own <code>iterator() method. This will iterate over the exact same
     * elements (but perhaps/probably in a different order).
     *
     * @return the <code>Set</code> of all subtrees in the tree.
     */
    @Override
    public Set<Tree> subTrees() {
        return (Set<Tree>) subTrees(new HashSet<Tree>());
    }

    /**
     * Get the list of all subtrees inside the tree by returning a tree
     * rooted at each node.  These are <i>not</i> copies, but all share
     * structure.  The tree is regarded as a subtree of itself.
     * <p/>
     * <i>Note:</i> If you only want to form this Collection so that you can
     * iterate over it, it is more efficient to simply use the Tree class's
     * own <code>iterator() method. This will iterate over the exact same
     * elements (but perhaps/probably in a different order).
     *
     * @return the <code>List</code> of all subtrees in the tree.
     */
    @Override
    public List<Tree> subTreeList() {
        return (List<Tree>) subTrees(new ArrayList<Tree>());
    }


    /**
     * Add the set of all subtrees inside a tree (including the tree itself)
     * to the given <code>Collection</code>.
     * <p/>
     * <i>Note:</i> If you only want to form this Collection so that you can
     * iterate over it, it is more efficient to simply use the Tree class's
     * own <code>iterator() method. This will iterate over the exact same
     * elements (but perhaps/probably in a different order).
     *
     * @param n A collection of nodes to which the subtrees will be added.
     * @return The collection parameter with the subtrees added.
     */
    @Override
    public Collection<Tree> subTrees(Collection<Tree> n) {
        n.add(this);
        Tree[] kids = children();
        for (int i = 0; i < kids.length; i++) {
            kids[i].subTrees(n);
        }
        
        return n;
    }
    
  /**
   * Return the parent of the tree node.  This routine will traverse
   * a tree (depth first) from the given <code>root</code>, and will
   * correctly find the parent, regardless of whether the concrete
   * class stores parents.  It will only return <code>null</code> if this
   * node is the <code>root</code> node, or if this node is not
   * contained within the tree rooted at <code>root</code>.
   *
   * @param root The root node of the whole Tree
   * @return the parent <code>Tree</code> node if any;
   *         else <code>null</code>
   */
    public RECKDPTreeNodeImpl parent(RECKDPTreeNodeImpl root) {
        Tree[] kids = root.children();
        RECKDPTreeNodeImpl newKids[] = new RECKDPTreeNodeImpl[kids.length];
        
        for (int i = 0; i < kids.length; i++)
            newKids[i] = (RECKDPTreeNodeImpl)kids[i];
        
        return parentHelper(root, newKids, this);
    }


    private RECKDPTreeNodeImpl parentHelper(RECKDPTreeNodeImpl parent, RECKDPTreeNodeImpl[] kids, RECKDPTreeNodeImpl node) {
        for (int i = 0, n = kids.length; i < n; i++) {
            if (kids[i].equals(node)) {
                return parent;
            }
            RECKDPTreeNodeImpl ret = ((RECKDPTreeNodeImpl)node).parent(kids[i]);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }
   
    private int idx = -1;
    private String role = "";
    private String constituent = "";
    private Charseq position = null;
}
