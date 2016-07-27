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

import java.util.List;
import java.util.Set;
import reck.util.Charseq;

/**
 * An interface that act as an entity
 * containing a list of mentions.
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Entity {

    public String getId();
    
    public void setId(String id);
    
    public String getType();
    
    public void setType(String type);
    
    public String getSubType();
    
    public void setSubType(String subtype);
    
    public String getClas();
    
    public void setClas(String clas);
    
    /**
     * Returns an array of mentions for the current entity.
     */
    public Mention[] mentions();
    
    /**
     * Returns a List of mentions for the current entity.  If there are no
     * mentions, then a (non-null) <code>List&lt;Mention&gt;</code> of size 0 will
     * be returned.  The list has new list structure but pointers to,
     * not copies of the mentions.  That is, the returned list is mutable,
     * and simply adding to or deleting items from it is safe, but beware
     * changing the contents of the mentions.
     */
    public List<Mention> getMentionsAsList();
    public Set<Mention> getMentionsAsSet();

    /**
     * Sets the children of this <code>Entity</code>.  If given
     * <code>null</code>, this method prints a warning and sets the
     * Entity's mentions to the canonical zero-length Mention[] array.
     */
    public void setMentions(Mention[] mentions);
    
    /**
     * Set the mentions of this entity to the given list.  This
     * method is implemented in the <code>Entity</code> class by
     * converting the <code>List</code> into an entity array and calling
     * the array-based method.  Subclasses which use a
     * <code>List</code>-based representation of mentions should
     * override this method.  This implementation allows the case
     * that the <code>List</code> is <code>null</code>: it yields an
     * entity with no mentions (represented by a canonical zero-length
     * Mention() array).
     */
    public void setMentions(List<Mention> mentionsList);
    
    /**
     * Says how many children a tree node has in its local tree.
     * Can be used on an arbitrary <code>Tree</code>.  Being a leaf is defined
     * as having no children.
     */
    public int numMentions();
    
    /**
     * Returns the position of an Mention in the mention list, if present, or
     * -1 if it is not present.  Mentions are checked for presence with
     * <code>equals()</code>.
     */
    public int indexOfMention(Mention mention);
    
    /**
     * Returns an array of mentions for the current entity.
     */
    public Charseq[] attributes();
    
    /**
     * Returns a List of attributes for the current entity.  If there are no
     * mentions, then a (non-null) <code>List&lt;Charseq&gt;</code> of size 0 will
     * be returned.  The list has new list structure but pointers to,
     * not copies of the attributes.  That is, the returned list is mutable,
     * and simply adding to or deleting items from it is safe, but beware
     * changing the contents of the attributes.
     */
    public List<Charseq> getAttributesAsList();

    /**
     * Sets the children of this <code>Entity</code>.  If given
     * <code>null</code>, this method prints a warning and sets the
     * Entity's mentions to the canonical zero-length Mention[] array.
     */
    public void setAttributes(Charseq[] attributes);
    
    /**
     * Set the attributes of this entity to the given list.  This
     * method is implemented in the <code>Entity</code> class by
     * converting the <code>List</code> into an entity array and calling
     * the array-based method.  Subclasses which use a
     * <code>List</code>-based representation of attributes should
     * override this method.  This implementation allows the case
     * that the <code>List</code> is <code>null</code>: it yields an
     * entity with no attributes (represented by a canonical zero-length
     * Charseq() array).
     */
    public void setAttributes(List<Charseq> attributesList);
    
    /**
     * Says how many attributes an entity has.
     * Can be used on an arbitrary <code>Charseq</code>.  
     */
    public int numAttributes();
    
    /**
     * Returns the position of an attribute in the mention list, if present, or
     * -1 if it is not present.  Attributes are checked for presence with
     * <code>equals()</code>.
     */
    public int indexOfAttribute(Charseq attribute);
}
