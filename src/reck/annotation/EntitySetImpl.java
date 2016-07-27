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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import reck.*;
import reck.corpora.DocumentImpl;
import reck.util.RECKConstants;
import reck.util.Statistics;

/**
 * A set of entities.
 * 
 * @author Truc-Vien T. Nguyen
 */
public class EntitySetImpl extends AbstractSet implements EntitySet, Cloneable, Serializable {

    /** Construction from Document. */
    public EntitySetImpl(Document doc) {
        this.doc = (DocumentImpl) doc;
    } // construction from document

    /** Construction from Document. */
    public EntitySetImpl(Document doc, Element document) {
        this(doc);
        Iterator entityIter = document.getChildren("entity").iterator();

        while (entityIter.hasNext()) {
            EntityImpl entity = new EntityImpl((Element) entityIter.next(), doc.getNoTaggedContent());
            add(entity);
                    
            int ent_Type_Index = RECKConstants.entityTypes.indexOf(entity.type);
            int ent_SubType_Index = RECKConstants.entitySubTypes.indexOf(entity.type + "." + entity.subtype);
            int ent_Class_Index = RECKConstants.entityClasses.indexOf(entity.clas);

            Statistics.nbr_entities++;
            Statistics.nbr_entitiesPerType[ent_Type_Index]++;
            if (ent_SubType_Index != -1)
                Statistics.nbr_entitiesPerSubType[ent_SubType_Index]++;
            if (ent_Class_Index != -1)
                Statistics.nbr_entitiesPerClass[ent_Class_Index]++;
        }
    } // construction from document

    /** Construction from Collection (which must be an AnnotationSet) */
    public EntitySetImpl(Collection c) throws ClassCastException {
        this(((EntitySet) c).getDocument());
        if (c instanceof EntitySetImpl) {
            EntitySetImpl theC = (EntitySetImpl) c;
            entitiesById = (HashMap) theC.entitiesById.clone();
            if (theC.entitiesByType != null) {
                entitiesByType = (HashMap) theC.entitiesByType.clone();
            }
        } else {
            addAll(c);
        }
    } // construction from collection

    /** This inner class serves as the return value from the iterator()
     * method.
     */
    class EntitySetIterator implements Iterator {

        private Iterator iter;
        protected Entity lastNext = null;

        EntitySetIterator() {
            iter = entitiesById.values().iterator();
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public Object next() {
            return (lastNext = (Entity) iter.next());
        }

        public void remove() {
            // this takes care of the ID index
            iter.remove();

            // remove from type index
            removeFromTypeIndex(lastNext);
        } // remove()
    }; // EntitySetIterator

    /** The size of this set */
    public int size() {
        return entitiesById.size();
    }
    
    /** Returns true if this set contains no elements */
    public boolean isEmpty() {
        return entitiesById.isEmpty();
    }
    
    /** Returns true if this set contains the specified element */
    public boolean contains(Object o){
        return entitiesById.containsValue(o);
    }
    
    /** Get an iterator for this set */
    public Iterator iterator() {
        return new EntitySetIterator();
    }
    
    /** Returns an array containing all of the elements in this set */
    //public Object[] toArray();
    
    //public Object[] toArray(Object[] a);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public void add(String id, String type, String subtype, String Clas, Mention[] mentions, Charseq[] attributes, String content) {
        Entity e = new EntityImpl(id, type, subtype, Clas, mentions, attributes, content);
        add(e);
    }
    
    /** Add an existing entity. Returns true when the set is modified. */
    public void add(String id, String type, String subtype, String Clas, List<Mention> mentions, List<Charseq> attributes, String content) {
        Entity e = new EntityImpl(id, type, subtype, Clas, mentions, attributes, content);
        add(e);
    }
    
    /** Add an existing annotation. Returns true when the set is modified. */
    public boolean add(Object o) throws ClassCastException {
        Entity e = (Entity) o;
        Object oldValue = entitiesById.put(e.getId(), e);
        if (entitiesByType != null) {
            addToTypeIndex(e);
        }
        return oldValue != e;
    } // add(o)

    /** Add an existing entity. Returns true when the set is modified. */
    public boolean remove(String id) {
        return remove(entitiesById.get(id));
    }

    /** Remove an element from this set. */
    /** Remove an element from this set. */
    public boolean remove(Object o) throws ClassCastException {
        Entity e = (Entity) o;
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
    public boolean addAll(Collection c, String content) {
        Iterator entityIter = c.iterator();
        boolean changed = false;
        while (entityIter.hasNext()) {
            Entity e = (Entity) entityIter.next();

            add(e.getId(), e.getType(), e.getSubType(), e.getClas(), e.mentions(), e.attributes(), content);
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
    /** Get all entities */
    public EntitySet get() {
        EntitySetImpl resultSet = new EntitySetImpl(doc);
        resultSet.addAllKeepIDs(entitiesById.values());
        if (resultSet.isEmpty()) {
            return null;
        }
        return resultSet;
    } // get()

    /** Get an entity by its Id */
    public Entity getEntityById(String id) {
        return (Entity) entitiesById.get(id);
    }

    /** Select entities by type */
    public EntitySet get(String type) {
        if (entitiesByType == null) {
            indexByType();
        }
        // the aliasing that happens when returning a set directly from the
        // types index can cause concurrent access problems; but the fix below
        // breaks the tests....
        //AnnotationSet newSet =
        //  new AnnotationSetImpl((Collection) annotsByType.get(type));
        //return newSet;
        return (EntitySet) entitiesByType.get(type);
    } // get(type)

    /** Select entities by a set of types. Expects a Set of String. */
    /** Select annotations by a set of types. Expects a Set of String. */
    public EntitySet get(Set types) throws ClassCastException {
        if (entitiesByType == null) {
            indexByType();
        }
        Iterator iter = types.iterator();
        EntitySetImpl resultSet = new EntitySetImpl(doc);
        while (iter.hasNext()) {
            String type = (String) iter.next();
            EntitySet as = (EntitySet) entitiesByType.get(type);
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
    
    /** Get the document this set is attached to. */
    public Document getDocument() {
        return doc;
    }

    protected boolean addAllKeepIDs(Collection c) {
        Iterator entityIter = c.iterator();
        boolean changed = false;
        while (entityIter.hasNext()) {
            Entity e = (Entity) entityIter.next();
            changed |= add(e);
        }
        return changed;
    }

    /** Construct the positional index. */
    protected void indexByType() {
        if (entitiesByType != null) {
            return;
        }
        entitiesByType = new HashMap(RECKConstants.HASH_STH_SIZE);
        Iterator entityIter = entitiesById.values().iterator();
        while (entityIter.hasNext()) {
            addToTypeIndex((Entity) entityIter.next());
        }
    } // indexByType()

    /** Add an annotation to the type index. Does nothing if the index
     * doesn't exist.
     */
    void addToTypeIndex(Entity e) {
        if (entitiesByType == null) {
            return;
        }
        String type = e.getType();
        EntitySet sameType = (EntitySet) entitiesByType.get(type);
        if (sameType == null) {
            sameType = new EntitySetImpl(doc);
            entitiesByType.put(type, sameType);
        }
        sameType.add(e);
    } // addToTypeIndex(a)

    /** Remove from the ID index. */
    protected boolean removeFromIdIndex(Entity e) {
        if (entitiesById.remove(e.getId()) == null) {
            return false;
        }
        return true;
    } // removeFromIdIndex(e)

    /** Remove from the type index. */
    protected void removeFromTypeIndex(Entity e) {
        if (entitiesByType != null) {
            EntitySet sameType = (EntitySet) entitiesByType.get(e.getType());
            if (sameType != null) {
                sameType.remove(e);
            }
            if (sameType.isEmpty()) // none left of this type
            {
                entitiesByType.remove(e.getType());
            }
        }
    } // removeFromTypeIndex(a) 

    /** The document this set belongs to */
    DocumentImpl doc = null;
    /** Maps annotation ids (Integers) to Annotations */
    protected HashMap entitiesById = new HashMap();
    /** Maps annotation types (Strings) to AnnotationSets */
    protected HashMap entitiesByType = null;
}
