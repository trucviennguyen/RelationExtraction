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
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jdom.Element;
import reck.*;
import reck.corpora.DocumentImpl;
import reck.util.RECKConstants;
import reck.util.Statistics;

/**
 * A set of relations.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RelationSetImpl extends AbstractSet implements RelationSet, Cloneable, Serializable {
    
    public RelationSetImpl() {
    }
    
    /** Construction from Document. */
    public RelationSetImpl(Document doc) {
        this.doc = (DocumentImpl)doc;
    } // construction from document
    
    /** Construction from Document. */
    public RelationSetImpl(Document doc, Element document) {
        this(doc);
        Iterator relationIter = document.getChildren("relation").iterator();
        
        while (relationIter.hasNext()) {
        	
            Element relElement = (Element)relationIter.next();            
            
            String id = relElement.getAttributeValue("ID");
            String type = relElement.getAttributeValue("TYPE");
            String subtype = relElement.getAttributeValue("SUBTYPE");
            
            Iterator<Element> entityIter = relElement.getChildren("rel_entity_arg").iterator();
            String entityId1 = ((Element)entityIter.next()).getAttributeValue("ENTITYID");
            String entityId2 = ((Element)entityIter.next()).getAttributeValue("ENTITYID");
            
            if (!entityId1.equals(entityId2)) {
                Entity entity1 = doc.getEntities().getEntityById(entityId1);
                Entity entity2 = doc.getEntities().getEntityById(entityId2);

                // Consider only explicit relations
                Iterator<Element> relIter = relElement.getChildren("relation_mention").iterator();

                while (relIter.hasNext()) {

                    Element relMention = relIter.next();

                    RelationImpl relation = new RelationImpl(doc, id, type, subtype, entity1, entity2, relMention);

                    if ( (relation.getRelationTree() != null ) && (relation.getRelationTree().size() > 0) ) {
                        add(relation);
                    
                    int rel_Type_Index = RECKConstants.relationTypes.indexOf(type);
                    int rel_SubType_Index = RECKConstants.relationSubTypes.indexOf(type + "." + subtype);
                    int rel_LDCLexical_Index = RECKConstants.relationLDCLexicalConditions.indexOf(relation.lexical_condition);

                    Statistics.nbr_relations++;
                    Statistics.nbr_relationsPerType[rel_Type_Index]++;
                    if (rel_SubType_Index != -1)
                        Statistics.nbr_relationsPerSubType[rel_SubType_Index]++;
                    if (rel_LDCLexical_Index != -1)
                        Statistics.nbr_relationLDCLexicalConditions[rel_LDCLexical_Index]++;
                    }

                }
            }
            
        }
    } // construction from document

    /** Construction from Collection (which must be an AnnotationSet) */
    public RelationSetImpl(Collection c) throws ClassCastException {
        this( ( (RelationSet) c).getDocument());
        if (c instanceof RelationSetImpl) {
            RelationSetImpl theC = (RelationSetImpl) c;
            relationsById = (HashMap) theC.relationsById.clone();
            if (theC.relationsByType != null)
                relationsByType = (HashMap) theC.relationsByType.clone();
        }
        else
            addAll(c);
    } // construction from collection
    
    /** This inner class serves as the return value from the iterator()
     * method.
     */
    class RelationSetIterator implements Iterator {
        private Iterator iter;
        protected Relation lastNext = null;

        RelationSetIterator() {
            iter = relationsById.values().iterator();
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public Object next() {
            return (lastNext = (Relation) iter.next());
        }

        public void remove() {
            // this takes care of the ID index
            iter.remove();

            // remove from type index
            removeFromTypeIndex(lastNext);
        } // remove()
    }; // AnnotationSetIterator
    
    /** The size of this set */
    public int size() {
        return relationsById.size();
    }
    
    /** Returns true if this set contains no elements */
    public boolean isEmpty() {
        return relationsById.isEmpty();
    }
    
    /** Returns true if this set contains the specified element */
    public boolean contains(Object o){
        return relationsById.containsValue(o);
    }
    
    /** Get an iterator for this set */
    public Iterator iterator() {
        return new RelationSetIterator();
    }
    
    /** Returns an array containing all of the elements in this set */
    //public Object[] toArray();
    
    //public Object[] toArray(Object[] a);
    
    /** Add an existing relation. Returns true when the set is modified. */
    public void add(String id, String type, String subtype, 
            Entity entity1, Entity entity2, ArrayList relationTree, 
            Mention mention1, Mention mention2) {
        Relation r = new RelationImpl(id, type, subtype,  
                entity1, entity2, relationTree, mention1, mention2);
        add(r);
    }
    
    /** Add an existing annotation. Returns true when the set is modified. */
    public boolean add(Object o) throws ClassCastException {
        Relation e = (Relation) o;
        Object oldValue = relationsById.put(e.getMentionId(), e);
        if (relationsByType != null)
            addToTypeIndex(e);
        return oldValue != e;
    } // add(o)
    
    /** Add an existing relation. Returns true when the set is modified. */
    public boolean remove(String id) {
        return remove(relationsById.get(id));
    }
    
    /** Remove an element from this set. */
    /** Remove an element from this set. */
    public boolean remove(Object o) throws ClassCastException {
        Relation e = (Relation) o;
        boolean wasPresent = removeFromIdIndex(e);
        if (wasPresent) {
            removeFromTypeIndex(e);
        }

        return wasPresent;
    } // remove(o)
    
    /** 
     * Returns true if this set contains all of the elements of the 
     *  specified collection
     */
    // public boolean containsAll(Collection c);
    
    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation)
     */
    public boolean addAll(Collection c) {
        Iterator relationIter = c.iterator();
        boolean changed = false;
        while (relationIter.hasNext()) {
            Relation r = (Relation) relationIter.next();
            
            add(r.getId(), r.getType(), r.getSubtype(), 
                    r.getEntity(0), r.getEntity(1), r.getRelationTree(), 
                    r.getMention(0), r.getMention(1) );
            
            changed = true;
        }
        return changed;
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
    
    /** Get all relations */
    public RelationSet get() {
        RelationSetImpl resultSet = new RelationSetImpl(doc);
        resultSet.addAllKeepIDs(relationsById.values());
        if (resultSet.isEmpty())
            return null;
        return resultSet;
    } // get()
  
   /** Get an entity by its Id */
    public Relation getRelationById(String id) {
        return (Relation) relationsById.get(id);
    }
    
    public Relation getRelationByMentions(Mention mention1, Mention mention2) {
        Relation relation = null;
        
        Iterator idIter = relationsById.keySet().iterator();
        while (idIter.hasNext()) {
            String id = (String)idIter.next();
            Relation rel = getRelationById(id);
            if ( (rel.getMention(0).getId().equals(mention1.getId()) && rel.getMention(1).getId().equals(mention2.getId()))
                    || (rel.getMention(1).getId().equals(mention1.getId()) && rel.getMention(0).getId().equals(mention2.getId()))) {
                relation = rel;
                break;
            }
        }
        
        return relation;
    }

    /** Select relations by type */
    public RelationSet get(String type) {
        if (relationsByType == null)
            indexByType();
        // the aliasing that happens when returning a set directly from the
        // types index can cause concurrent access problems; but the fix below
        // breaks the tests....
        //AnnotationSet newSet =
        //  new AnnotationSetImpl((Collection) annotsByType.get(type));
        //return newSet;
        return (RelationSet) relationsByType.get(type);
    } // get(type)

    /** Select relations by a set of types. Expects a Set of String. */
  /** Select annotations by a set of types. Expects a Set of String. */
    public RelationSet get(Set types) throws ClassCastException {
        if (relationsByType == null)
            indexByType();
        Iterator iter = types.iterator();
        RelationSetImpl resultSet = new RelationSetImpl(doc);
        while (iter.hasNext()) {
            String type = (String) iter.next();
            RelationSet as = (RelationSet) relationsByType.get(type);
            if (as != null)
            resultSet.addAllKeepIDs(as);
            // need an addAllOfOneType method
        } // while
        if (resultSet.isEmpty())
            return null;
        return resultSet;
    } // get(types)

    /** Get the document this set is attached to. */
    public Document getDocument() {
        return doc;
    }

    protected boolean addAllKeepIDs(Collection c) {
        Iterator relationIter = c.iterator();
        boolean changed = false;
        while (relationIter.hasNext()) {
            Relation e = (Relation) relationIter.next();
            changed |= add(e);
        }
        return changed;
    }
    
    /** Construct the positional index. */
    protected void indexByType() {
        if (relationsByType != null)
            return;
        relationsByType = new HashMap(RECKConstants.HASH_STH_SIZE);
        Iterator relationIter = relationsById.values().iterator();
        while (relationIter.hasNext())
            addToTypeIndex( (Relation) relationIter.next());
    } // indexByType()

    /** Add an annotation to the type index. Does nothing if the index
     * doesn't exist.
     */
    protected void addToTypeIndex(Relation e) {
        if (relationsByType == null)
            return;
        String type = e.getType();
        RelationSet sameType = (RelationSet) relationsByType.get(type);
        if (sameType == null) {
            sameType = new RelationSetImpl(doc);
            relationsByType.put(type, sameType);
        }
        sameType.add(e);
    } // addToTypeIndex(a)
    
    /** Remove from the ID index. */
    protected boolean removeFromIdIndex(Relation e) {
        if (relationsById.remove(e.getId()) == null)
            return false;
        return true;
    } // removeFromIdIndex(e)

    /** Remove from the type index. */
    protected void removeFromTypeIndex(Relation e) {
        if (relationsByType != null) {
            RelationSet sameType = (RelationSet) relationsByType.get(e.getType());
            if (sameType != null)
                sameType.remove(e);
            if (sameType.isEmpty()) // none left of this type
                relationsByType.remove(e.getType());
        }
    } // removeFromTypeIndex(a)

    /** The document this set belongs to */
    DocumentImpl doc = null;

    /** Maps annotation ids (Integers) to Annotations */
    protected HashMap relationsById = new HashMap();

    /** Maps annotation types (Strings) to AnnotationSets */
    protected HashMap relationsByType = null;
}
