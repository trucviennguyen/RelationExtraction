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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import reck.util.Charseq;

import java.util.Iterator;
import org.jdom.Element;

import java.util.List;
import java.util.Set;
import reck.Entity;
import reck.Mention;
import reck.util.RECKConstants;
import reck.util.Statistics;

/**
 * A <code>EntityImpl</code> object acts as a Entity that contains
 * type/subtype/class, a set of entity mentions/attributes
 * corresponding to ACE 2004 definition.
 *
 * @author Truc-Vien T. Nguyen
 * @version 2009/03/31
 */
public class EntityImpl implements Entity, Cloneable, Serializable {
    
    /**
     * A leaf node should have a zero-length array for its
     * children. For efficiency, subclasses can use this array as a
     * return value for children() for leaf nodes if desired. Should
     * this be public instead?
     */
    protected static final Mention[] ZEROMENTIONS = new Mention[0];
    protected static final Charseq[] ZEROATTRIBUTES = new Charseq[0];
    
    /**
     * Mentions of the entity.
     */
    private Mention[] mentions = ZEROMENTIONS;
    
    /**
     * Attributes of the entity.
     */
    private Charseq[] attributes = ZEROATTRIBUTES;
    
    public EntityImpl(String id, String type, String subtype, String clas, 
            Mention[] mentions, Charseq[] attributes, String content) {
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        this.clas = clas;
        this.mentions = mentions;
        this.attributes = attributes;
    }
    
    public EntityImpl(String id, String type, String subtype, String clas, 
            List<Mention> mentions, List<Charseq> attributes, String content) {
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        this.clas = clas;
        setMentions(mentions);
        setAttributes(attributes);
    }
    
    public EntityImpl(String id, String type, String subtype, String clas) {
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        this.clas = clas; 
    }
    
    public EntityImpl(Element entity, String content) {
        this.id = entity.getAttributeValue("ID");
        // this.id = longId.substring(longId.lastIndexOf("-") + 1);        
        
        this.type = entity.getAttributeValue("TYPE");
        this.subtype = entity.getAttributeValue("SUBTYPE");
        this.clas = entity.getAttributeValue("CLASS");
        
        ArrayList mentionsList = new ArrayList();

        List entity_mentions = entity.getChildren("entity_mention");
        if (entity_mentions != null) {
            Iterator mentionIter = entity_mentions.iterator();
            while (mentionIter.hasNext()) {
                Element entity_mention = (Element)mentionIter.next();
                String mentionId = entity_mention.getAttributeValue("ID");                
                String mentionType = entity_mention.getAttributeValue("TYPE");
                String mentionLDCType = entity_mention.getAttributeValue("LDCTYPE");
                String mentionRole = entity_mention.getAttributeValue("ROLE");
                String mentionRef = entity_mention.getAttributeValue("REFERENCE");
                Element extentSeq = entity_mention.getChild("extent").getChild("charseq");
                Element headSeq = entity_mention.getChild("head").getChild("charseq");                
                Charseq mentionExtent = new Charseq(extentSeq.getAttributeValue("START"), 
                        extentSeq.getAttributeValue("END"));
                Charseq mentionHead = new Charseq(headSeq.getAttributeValue("START"), 
                        headSeq.getAttributeValue("END"));
                if ( (mentionExtent.getStart() == null) && (mentionExtent.getEnd() == null) )
                    mentionExtent = mentionHead.clone();
                if ( (mentionHead.getStart() == null) && (mentionHead.getEnd() == null) )
                    mentionHead = mentionExtent.clone();
                Charseq hwPosition = new Charseq(mentionExtent.getStart().intValue(),
                        mentionHead.getEnd().intValue());
                String mentionHeadword = RECKConstants.trimReturn(content.substring(mentionExtent.getStart().intValue(),
                        mentionHead.getEnd().intValue() + 1));
                MentionImpl mention = new MentionImpl(this, mentionId, mentionHeadword, 
                        mentionType, mentionLDCType, mentionRole, mentionRef, mentionExtent, mentionHead, hwPosition);
                mentionsList.add((Mention)mention);
                    
                int ment_Type_Index = RECKConstants.mentionTypes.indexOf(mentionType);
                int ment_LDCType_Index = RECKConstants.mentionTypes.indexOf(mentionLDCType);
                int ment_Role_Index = RECKConstants.mentionTypes.indexOf(mentionRole);
                int ment_Reference_Index = RECKConstants.mentionTypes.indexOf(mentionRef);

                Statistics.nbr_mentions++;
                Statistics.nbr_mentionsPerType[ment_Type_Index]++;
                if (ment_LDCType_Index != -1)
                    Statistics.nbr_mentionsPerLDCType[ment_LDCType_Index]++;
                if (ment_Role_Index != -1)
                    Statistics.nbr_mentionsPerRole[ment_Role_Index]++;
                if (ment_Reference_Index != -1)
                    Statistics.nbr_mentionsPerReference[ment_Reference_Index]++;
                
            }
            this.setMentions(mentionsList);
        }
        
        ArrayList attributesList = new ArrayList();
        
        Element entity_attributes = entity.getChild("entity_attributes");
        if (entity_attributes != null) {
            Iterator nameIter = entity_attributes.getChildren("name").iterator();
            while (nameIter.hasNext()) {
                Element name = (Element)nameIter.next();
                Charseq namePos = new Charseq(name.getChild("charseq").getAttributeValue("START"),
                        name.getChild("charseq").getAttributeValue("END"));
                attributesList.add(namePos);
            }
            this.setAttributes(attributesList);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSubType() {
        return subtype;
    }

    public void setSubType(String subtype) {
        this.subtype = subtype;
    }

    public String getClas() {
        return clas;
    }

    public void setClas(String clas) {
        this.clas = clas;
    }

    /**
     * Returns an array of mentions for the current entity.
     */
    public Mention[] mentions() {
        return mentions;
    }
    
    /**
     * Returns a List of mentions for the current entity.  If there are no
     * mentions, then a (non-null) <code>List&lt;Mention&gt;</code> of size 0 will
     * be returned.  The list has new list structure but pointers to,
     * not copies of the mentions.  That is, the returned list is mutable,
     * and simply adding to or deleting items from it is safe, but beware
     * changing the contents of the mentions.
     *
     * @return The mentions of the entity
     */
    public List<Mention> getMentionsAsList() {
        Mention[] ments = mentions();
        int count = ments.length;
        ArrayList<Mention> mentsList = new ArrayList<Mention>(count);
        for (int i = 0; i < count; i++) {
            mentsList.add(ments[i]);
        }

        return mentsList;
    }
    
    public Set<Mention> getMentionsAsSet() {
        Mention[] ments = mentions();
        int length = ments.length;
        HashSet<Mention> mentsSet = new HashSet<Mention>(length);
        for (int i = 0; i < length; i++) {
            mentsSet.add(ments[i]);
        }
        return mentsSet;
    }

    /**
     * Sets the children of this <code>Entity</code>.  If given
     * <code>null</code>, this method prints a warning and sets the
     * Entity's mentions to the canonical zero-length Mention[] array.
     *
     * @param mentions An array of mentions
     */
    public void setMentions(Mention[] mentions) {
        if (mentions == null) {
            System.err.println("Warning -- you tried to set the mentions of an Entity to null.\nYou really should be using a zero-length array instead.");
            mentions = ZEROMENTIONS;
        } 
        else {
            int count = mentions.length;

            // Selection sort
            for (int i = count - 1; i > 0; i--) {
                int max = 0, current;
                for (current = 1; current <= i; current++)
                    if (mentions[max].getExtent().length() > mentions[current].getExtent().length())
                        max = current;

                Mention temp = mentions[max];
                mentions[max] = mentions[i];
                mentions[i] = temp;
            }

            this.mentions = mentions;
        }
    }
    
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
     *
     * @param mentionsList A list of mentions to become mentions of the node.
     *          This method does not retain the List that you pass it (copying
     *          is done), but it will retain the individual mentions (they are
     *          not copied).
     * @see #setMentions(Mention[])
     */
    public void setMentions(List<Mention> mentionsList) {
        if (mentionsList == null || mentionsList.size() == 0) {
            setMentions(ZEROMENTIONS);
        } 
        else {
            int leng = mentionsList.size();
            Mention[] mentions = new Mention[leng];
            mentionsList.toArray(mentions);
            setMentions(mentions);
        }
    }
    
    /**
     * Says how many children a tree node has in its local tree.
     * Can be used on an arbitrary <code>Tree</code>.  Being a leaf is defined
     * as having no children.
     *
     * @return The number of direct children of the tree node
     */
    public int numMentions() {
        return mentions().length;
    }
    
    /**
     * Returns the position of an Mention in the mention list, if present, or
     * -1 if it is not present.  Mentions are checked for presence with
     * <code>equals()</code>.
     *
     * @param tree The mention to look for in mentions list
     * @return Its index in the list or -1
     */
    public int indexOfMention(Mention mention) {
        Mention[] ments = mentions();
        for (int i = 0; i < ments.length; i++) {
            if (ments[i].equals(mention)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns an array of mentions for the current entity.
     */
    public Charseq[] attributes() {
        return attributes;
    }
    
    /**
     * Returns a List of attributes for the current entity.  If there are no
     * mentions, then a (non-null) <code>List&lt;Charseq&gt;</code> of size 0 will
     * be returned.  The list has new list structure but pointers to,
     * not copies of the attributes.  That is, the returned list is mutable,
     * and simply adding to or deleting items from it is safe, but beware
     * changing the contents of the attributes.
     *
     * @return The attributes of the entity
     */
    public List<Charseq> getAttributesAsList() {
        Charseq[] atts = attributes();
        int length = atts.length;
        ArrayList<Charseq> attsList = new ArrayList<Charseq>(length);
        for (int i = 0; i < length; i++) {
            attsList.add(atts[i]);
        }
        return attsList;
    }

    /**
     * Sets the children of this <code>Entity</code>.  If given
     * <code>null</code>, this method prints a warning and sets the
     * Entity's mentions to the canonical zero-length Mention[] array.
     *
     * @param mentions An array of mentions
     */
    public void setAttributes(Charseq[] attributes) {
        if (attributes == null) {
            System.err.println("Warning -- you tried to set the attributes of an Entity to null.\nYou really should be using a zero-length array instead.");
            attributes = ZEROATTRIBUTES;
        } 
        else {
            this.attributes = attributes;
        }
    }
    
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
     *
     * @param mentionsList A list of attributes to become attributes of the entity.
     *          This method does not retain the List that you pass it (copying
     *          is done), but it will retain the individual attributes (they are
     *          not copied).
     * @see #setAttributes(Charseq[])
     */
    public void setAttributes(List<Charseq> attributesList) {
        if (attributesList == null || attributesList.size() == 0) {
            setAttributes(ZEROATTRIBUTES);
        } 
        else {
            int leng = attributesList.size();
            Charseq[] atts = new Charseq[leng];
            attributesList.toArray(atts);
            setAttributes(atts);
        }
    }
    
    /**
     * Says how many attributes an entity has.
     * Can be used on an arbitrary <code>Charseq</code>.  
     *
     * @return The number of attributes of the entity
     */
    public int numAttributes() {
        return attributes().length;
    }
    
    /**
     * Returns the position of an attribute in the mention list, if present, or
     * -1 if it is not present.  Attributes are checked for presence with
     * <code>equals()</code>.
     *
     * @param tree The attribute to look for in children list
     * @return Its index in the list or -1
     */
    public int indexOfAttribute(Charseq attribute) {
        Charseq[] atts = attributes();
        for (int i = 0; i < atts.length; i++) {
            if (atts[i].equals(attribute)) {
                return i;
            }
        }
        return -1;
    }
    
    public void writeToFile() throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(id + ".entity"));
        out.writeObject(this);
        out.close(); // Also flushes output    
    }
   
    String id = null;
    String type = null;
    String subtype = null;
    String clas = null;

    /**
     * Returns an array of children for the current node.  If there
     * are no children (if the node is a leaf), this must return a
     * Tree[] array of length 0.  A null children() value for tree
     * leaves was previously supported, but no longer is.
     * A caller may assume that either <code>isLeaf()</code> returns
     * true, or this node has a nonzero number of children.
     *
     * @return The children of the node
     * @see #getChildrenAsList()
     */

}
