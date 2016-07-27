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

package reck.annotation;

import java.io.Serializable;
import reck.util.Charseq;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import reck.*;
import reck.corpora.CorpusImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.util.RECKConstants;

/**
 * A list of entity mentions.
 *
 * @author Truc-Vien T. Nguyen
 */
public class MentionListImpl extends AbstractSet implements MentionList, Cloneable, Serializable {

    /** Construction from Document. */
    public MentionListImpl(Document doc) {
        this.doc = doc;
        Iterator entityIter = doc.getEntities().iterator();
        while (entityIter.hasNext()) {
            Entity entity = (Entity)entityIter.next();
            this.addAll(entity.getMentionsAsList());
        }
    } // construction from document
    
    /** Construction from Document. */
    public MentionListImpl(EntitySet entities) {
        this.doc = entities.getDocument();
        Iterator entityIter = entities.iterator();
        while (entityIter.hasNext()) {
            Entity entity = (Entity)entityIter.next();
            this.addAll(entity.getMentionsAsList());
        }
    } // construction from document
    
    /** Construction from Entity. */
    public MentionListImpl(Entity entity) {
        this.addAll(entity.getMentionsAsList());
    } // construction from entity

    /** Construction from Collection (which must be an AnnotationSet) */
    public MentionListImpl(Collection c) throws ClassCastException {
        this(((MentionList) c).getDocument());
        if (c instanceof MentionListImpl) {
            MentionListImpl theC = (MentionListImpl) c;
            mentionsById = (HashMap) theC.mentionsById.clone();
            if (theC.mentionsByType != null) {
                mentionsByType = (HashMap) theC.mentionsByType.clone();
            }
        } else {
            addAll(c);
        }
    } // construction from collection

    public int size() {
        return mentionList.size();
    }

    public boolean isEmpty() {
        return mentionList.isEmpty();
    }

    public boolean contains(Object o){
        return mentionList.contains(o);
    }

    public Iterator iterator(){
        return mentionList.iterator();
    }

    public Object[] toArray(){
        return mentionList.toArray();
    }

    public Object[] toArray(Object[] a){
        return mentionList.toArray(a);
    }
    
    /** Add an existing annotation. Returns true when the set is modified. */
    public boolean add(Object o) throws ClassCastException {
        // add mention according to the order of mention extent
        Mention e = (Mention) o;
        int i = 0;
        while ((i < mentionList.size()) 
                && (((Mention)mentionList.get(i)).getExtent().getStart().intValue() < e.getExtent().getStart().intValue()
                || ((((Mention)mentionList.get(i)).getExtent().getStart().intValue() == e.getExtent().getStart().intValue())
                && (((Mention)mentionList.get(i)).getExtent().getEnd().intValue() < e.getExtent().getEnd().intValue()) ) ) )
            i++;        
        if (i < mentionList.size()) mentionList.add(i, o);
        else mentionList.add(o);
        Object oldValue = mentionsById.put(e.getId(), e);
        if (mentionsByType != null) {
            addToTypeIndex(e);
        }
        return oldValue != e;
    } // add(o)
    
    /** Remove an element from this set. */
    public boolean remove(Object o) throws ClassCastException {
        mentionList.remove(o);
        Mention e = (Mention) o;
        boolean wasPresent = removeFromIdIndex(e);
        if (wasPresent) {
            removeFromTypeIndex(e);
        }

        return wasPresent;
    } // remove(o)


    public boolean containsAll(Collection c){
        return mentionList.containsAll(c);
    }

    public boolean addAll(Collection c){
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!add(o))
                return false;
        }
        return true;
    }

    public boolean addAll(int index, Collection c){
        return mentionList.addAll(index, c);
    }

    public boolean removeAll(Collection c){
        return mentionList.removeAll(c);
    }

    public boolean retainAll(Collection c){
        return mentionList.retainAll(c);
    }

    public void clear(){
        mentionsById.clear();
        mentionsByType.clear();
        mentionList.clear();
    }

    public boolean equals(Object o){
        if (! (o instanceof MentionListImpl))
            return false;

        return mentionList.equals(o);
    }

    public int hashCode(){
        return mentionList.hashCode();
    }

    public Object get(int index){
        return mentionList.get(index);
    }

    public Object set(int index, Object element){
        return mentionList.set(index, element);
    }

    public void add(int index, Object element){
        mentionList.add(index, element);
        Mention e = (Mention) element;
        mentionsById.put(e.getId(), e);
        if (mentionsByType != null) {
            addToTypeIndex(e);
        }
    }

    public Object remove(int index){
        Mention e = (Mention)mentionList.get(index);
        mentionList.remove(index);
        boolean wasPresent = removeFromIdIndex(e);
        if (wasPresent) {
            removeFromTypeIndex(e);
        }

        return wasPresent;
    }

    public int indexOf(Object o){
        return mentionList.indexOf(o);
    }

    public int lastIndexOf(Object o){
    return lastIndexOf(o);
    }

    public ListIterator listIterator(){
        return mentionList.listIterator();
    }

    public ListIterator listIterator(int index){
        return mentionList.listIterator(index);
    }

    public Corpus subList(int fromIndex, int toIndex){
        return new CorpusImpl(new ArrayList(mentionList.subList(fromIndex, toIndex)));
    }

    /** This inner class serves as the return value from the iterator()
     * method.
     */
    class MentionListIterator implements Iterator {

        private Iterator iter;
        protected Mention lastNext = null;

        MentionListIterator() {
            iter = mentionsById.values().iterator();
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public Object next() {
            return (lastNext = (Mention) iter.next());
        }

        public void remove() {
            // this takes care of the ID index
            iter.remove();

            // remove from type index
            removeFromTypeIndex(lastNext);
        } // remove()
    }; // MentionListIterator

    /** Add an existing Mention. Returns true when the set is modified. */
    public void add(Entity entity, String id, String headword, String type, 
            String ldcType, String role, String reference, 
            Charseq extent, Charseq head, Charseq hwPosition) {
        Mention mention = new MentionImpl(entity, id, headword, type, ldcType, role, reference, extent, head, hwPosition);
        add(mention);
    }
    
    /** Add an existing Mention. Returns true when the set is modified. */
    public void add(Entity entity, String id, String headword, String type, 
            String ldcType, Charseq extent, Charseq head, Charseq hwPosition) {
        Mention mention = new MentionImpl(entity, id, headword, type, ldcType, extent, head, hwPosition);
        add(mention);
    }
    
    /** Add an existing Mention. Returns true when the set is modified. */
    public boolean remove(String id) {
        return remove(mentionsById.get(id));
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation)
     */
    // public boolean retainAll(Collection c);
    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation)
     */
    // public boolean removeAll(Collection c);
    /** Removes all of the elements from this set (optional operation) */
    // public void clear();
    // Comparison and hashing
    /** Compares the specified object with this set for equality */
    // public boolean equals(Object o);
    /** Returns the hash code value for this set */
    // public int hashCode();
    /** Get all entities */
    public MentionList get() {
        MentionListImpl resultSet = new MentionListImpl(doc);
        resultSet.addAllKeepIDs(mentionsById.values());
        if (resultSet.isEmpty()) {
            return null;
        }
        return resultSet;
    } // get()

    /** Get an Mention by its Id */
    public Mention getMentionById(String id) {
        return (Mention) mentionsById.get(id);
    }

    /** Select entities by type */
    public MentionList get(String type) {
        if (mentionsByType == null) {
            indexByType();
        }
        // the aliasing that happens when returning a set directly from the
        // types index can cause concurrent access problems; but the fix below
        // breaks the tests....
        //AnnotationSet newSet =
        //  new AnnotationSetImpl((Collection) annotsByType.get(type));
        //return newSet;
        return (MentionList) mentionsByType.get(type);
    } // get(type)

    /** Select entities by a set of types. Expects a Set of String. */
    /** Select annotations by a set of types. Expects a Set of String. */
    public MentionList get(Set types) throws ClassCastException {
        if (mentionsByType == null) {
            indexByType();
        }
        Iterator iter = types.iterator();
        MentionListImpl resultSet = new MentionListImpl(doc);
        while (iter.hasNext()) {
            String type = (String) iter.next();
            MentionList as = (MentionList) mentionsByType.get(type);
            if (as != null) {
                resultSet.addAllKeepIDs(as);
            }
        // need an addAllOfOneType method
        } // while
        if (resultSet.isEmpty()) {
            return null;
        }
        return resultSet;
    } // get(types)
    
    /** Select Mention within start and end */
    public ArrayList getExact(Long start, Long end) {        
        ArrayList retList = new ArrayList();
        
        Iterator MentionIter = this.iterator();
        while (MentionIter.hasNext()) {
            Mention m = (Mention)MentionIter.next();
            if (m.matchedRange(start, end))
                retList.add(m);
        }
        
        return retList;
    }
    
    /** Select entity mention beginning at start and finishing before end */
    public String[] getChildrenInside(RECKDPTreeNodeImpl currentNode) {
        
        String[] ret = {"", ""};
        int first_StartIndex = -1, first_EndIndex = -1, second_StartIndex = -1, second_EndIndex = -1;
        Mention ment1 = null, ment2 = null;
        boolean ok = false;
        
        Iterator MentionIter = this.iterator();
        while (MentionIter.hasNext()) {
            Mention m = (Mention)MentionIter.next();
            
            long m_start = m.getHead().getStart().longValue();
            long m_end = m.getHead().getEnd().longValue();
            
            // try to see if mention m' head is mathced within current node
            if ( (currentNode.getPosition().getStart().longValue() <= m_start) 
                    && (m_end <= currentNode.getPosition().getEnd().longValue()) ) {
                
                List children = (ArrayList)currentNode.getChildrenAsList();
                
                int startIndex = 0, endIndex = children.size() - 1;
                
                // find the first starting point matched with mention
                while ( (startIndex < children.size()) && ( ((RECKDPTreeNodeImpl)children.get(startIndex)).getPosition().getStart().longValue() < m_start) )
                    startIndex++;
                
                // find the last starting point matched with mention
                while ( (endIndex >= 0) && ( m_end < ((RECKDPTreeNodeImpl)children.get(endIndex)).getPosition().getEnd().longValue() - 1) )
                    endIndex--;
                
                if ( (startIndex < children.size()) && (endIndex >= 0) && (startIndex < endIndex) 
                        && ( startIndex > 0 || endIndex < children.size() - 1 ) ) {
                    RECKDPTreeNodeImpl node_start = (RECKDPTreeNodeImpl)children.get(startIndex);
                    RECKDPTreeNodeImpl node_end = (RECKDPTreeNodeImpl)children.get(endIndex);
                    
                    // if mention is matched within "start index" and "end index"
                    if ( (m.matchedHeadFromNode(node_start)) && (m.matchedHeadToNode(node_end)) ) {

                        // if index just found has bigger distance than the previous one
                        if ( (first_StartIndex == -1) || ( (endIndex - startIndex) > (first_EndIndex - first_StartIndex)) ) {
                            first_StartIndex = startIndex;
                            first_EndIndex = endIndex;
                            ment1 = m;
                            ok = true;
                        }
                        else if ( (second_StartIndex == -1) || ( (endIndex - startIndex) > (second_EndIndex - second_StartIndex)) ) {
                            second_StartIndex = startIndex;
                            second_EndIndex = endIndex;
                            ment2 = m;
                            ok = true;
                        }

                    }
                }

            }
            
            // not success with Head, now turn to Extent
            if (!ok) {

                m_start = m.getExtent().getStart().longValue();
                m_end = m.getExtent().getEnd().longValue();

                if ( (currentNode.getPosition().getStart().longValue() <= m_start) 
                        && (m_end <= currentNode.getPosition().getEnd().longValue()) ) {

                    List children = (ArrayList)currentNode.getChildrenAsList();

                    int startIndex = 0, endIndex = children.size() - 1;

                    // find the first starting point matched with mention
                    while ( (startIndex < children.size()) && ( ((RECKDPTreeNodeImpl)children.get(startIndex)).getPosition().getStart().longValue() < m_start) )
                        startIndex++;

                    // find the last starting point matched with mention
                    while ( (endIndex >= 0) && ( m_end < ((RECKDPTreeNodeImpl)children.get(endIndex)).getPosition().getEnd().longValue() - 1) )
                        endIndex--;

                    if ( (startIndex < children.size()) && (endIndex >= 0) && (startIndex < endIndex) 
                        && ( startIndex > 0 || endIndex < children.size() - 1 ) ) {

                        RECKDPTreeNodeImpl node_start = (RECKDPTreeNodeImpl)children.get(startIndex);
                        RECKDPTreeNodeImpl node_end = (RECKDPTreeNodeImpl)children.get(endIndex);

                        // if mention is matched within "start index" and "end index"
                        if ( (m.matchedExtentFromNode(node_start)) && (m.matchedExtentToNode(node_end)) ) {

                            // if index just found has bigger distance than the previous one
                            if ( (first_StartIndex == -1) || ( (endIndex - startIndex) > (first_EndIndex - first_StartIndex)) ) {
                                first_StartIndex = startIndex;
                                first_EndIndex = endIndex;
                                ment1 = m;
                                ok = true;
                            }
                            else if ( (second_StartIndex == -1) || ( (endIndex - startIndex) > (second_EndIndex - second_StartIndex)) ) {
                                second_StartIndex = startIndex;
                                second_EndIndex = endIndex;
                                ment2 = m;
                                ok = true;
                            }

                        }
                    }

                }
            }
        }
        if (ment1 != null)
            ret[0] = first_StartIndex + " " + ment1.getId() + " " + first_EndIndex;
        if (ment2 != null)
            ret[1] = second_StartIndex + " " + ment2.getId() + " " + second_EndIndex;

        return ret;
    }
    
    /** Get the document this set is attached to. */
    public Document getDocument() {
        return doc;
    }

    protected boolean addAllKeepIDs(Collection c) {
        Iterator MentionIter = c.iterator();
        boolean changed = false;
        while (MentionIter.hasNext()) {
            Mention e = (Mention) MentionIter.next();
            changed |= add(e);
        }
        return changed;
    }

    /** Construct the positional index. */
    protected void indexByType() {
        if (mentionsByType != null) {
            return;
        }
        mentionsByType = new HashMap(RECKConstants.HASH_STH_SIZE);
        Iterator MentionIter = mentionsById.values().iterator();
        while (MentionIter.hasNext()) {
            addToTypeIndex((Mention) MentionIter.next());
        }
    } // indexByType()

    /** Add an annotation to the type index. Does nothing if the index
     * doesn't exist.
     */
    void addToTypeIndex(Mention e) {
        if (mentionsByType == null) {
            return;
        }
        String type = e.getType();
        MentionList sameType = (MentionList) mentionsByType.get(type);
        if (sameType == null) {
            sameType = new MentionListImpl(doc);
            mentionsByType.put(type, sameType);
        }
        sameType.add(e);
    } // addToTypeIndex(a)

    /** Remove from the ID index. */
    protected boolean removeFromIdIndex(Mention e) {
        if (mentionsById.remove(e.getId()) == null) {
            return false;
        }
        return true;
    } // removeFromIdIndex(e)

    /** Remove from the type index. */
    protected void removeFromTypeIndex(Mention e) {
        if (mentionsByType != null) {
            MentionList sameType = (MentionList) mentionsByType.get(e.getType());
            if (sameType != null) {
                sameType.remove(e);
            }
            if (sameType.isEmpty()) // none left of this type
            {
                mentionsByType.remove(e.getType());
            }
        }
    } // removeFromTypeIndex(a) 

    /** The document this set belongs to */
    protected Document doc = null;
    
    protected ArrayList mentionList = new ArrayList();
    
    /** Maps annotation ids (Integers) to Annotations */
    protected HashMap mentionsById = new HashMap();

    /** Maps annotation types (Strings) to AnnotationSets */
    protected HashMap mentionsByType = null;
}
