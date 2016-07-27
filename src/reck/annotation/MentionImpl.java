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
import reck.Entity;
import reck.util.Charseq;
import reck.Mention;
import reck.trees.RECKDPTreeNodeImpl;

/**
 * A <code>MentionImpl</code> object acts as an Entity Mention
 * that contains type/subtype/class, a set of entity mentions/
 * attributes corresponding to ACE 2004 definition.
 * 
 * @author Truc-Vien T. Nguyen
 */
public class MentionImpl implements Mention, Cloneable, Serializable {

    public MentionImpl(Entity entity) {
        this.entity = entity;
    }
    
    public MentionImpl(Entity entity, String id, String headword, String type, String ldcType, 
            String role, String reference, Charseq extent, Charseq head, Charseq hwPosition) {
        this(entity);
        this.id = id;
        this.headword = headword;
        this.type = type;
        this.ldcType = ldcType;
        this.role = role;
        this.reference = reference;
        this.extent = extent;
        this.head = head;
        this.hwPosition = hwPosition;
    }

    public MentionImpl(Entity entity, String id, String headword, String type, String ldcType, 
            Charseq extent, Charseq head, Charseq hwPosition) {
        this(entity);
        this.id = id;
        this.headword = headword;
        this.ldcType = ldcType;
        this.type = type;
        this.extent = extent;
        this.head = head;
        this.hwPosition = hwPosition;
    }
    
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getHeadword() {
        return headword;
    }

    public void setHeadword(String headword) {
        this.headword = headword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getLDCType() {
        return ldcType;
    }

    public void setLDCType(String ldcType) {
        this.ldcType = ldcType;
    }

    public String getRole() {
        return id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Charseq getExtent() {
        return extent;
    }

    public void setExtent(Charseq extent) {
        this.extent = extent;
    }

    public Charseq getHead() {
        return head;
    }

    public void setHead(Charseq head) {
        this.head = head;
    }
    
    public Charseq getHwPosition() {
        return hwPosition;
    }

    public void setHwPosition(Charseq hwPosition) {
        this.hwPosition = hwPosition;
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedRange(Long start, Long end) {
        return ( (head.getStart().longValue() == start.longValue()
                && head.getEnd().longValue() == end.longValue()) 
                || (extent.getStart().longValue() == start.longValue()
                && extent.getEnd().longValue() == end.longValue()) );
/*        return ( (extent.getStart().longValue() == start.longValue()
                && extent.getEnd().longValue() == end.longValue()) );*/
    }
    
    /**
     * Says if the entity mention is matched exactly range start & end
     * @return
     */
    public boolean matchedNode(RECKDPTreeNodeImpl node) {
/*        return ( ( (head.getStart().longValue() == node.getPosition().getStart().longValue())
                && (head.getEnd().longValue() == node.getPosition().getEnd().longValue() - 1) ) 
                || ( (extent.getStart().longValue() == node.getPosition().getStart().longValue())
                && (extent.getEnd().longValue() == node.getPosition().getEnd().longValue() - 1) ) );*/
        return ( ( (head.getStart().longValue() == node.getPosition().getStart().longValue())
                && (head.getEnd().longValue() == node.getPosition().getEnd().longValue() - 1) ) );
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedHeadFromNode(RECKDPTreeNodeImpl node) {
        return ( head.getStart().longValue() == node.getPosition().getStart().longValue() );
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedHeadToNode(RECKDPTreeNodeImpl node) {
        return ( head.getEnd().longValue() == node.getPosition().getEnd().longValue() - 2)
                || ( head.getEnd().longValue() == node.getPosition().getEnd().longValue() - 1)
                || ( head.getEnd().longValue() == node.getPosition().getEnd().longValue());
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedExtentFromNode(RECKDPTreeNodeImpl node) {
        return ( extent.getStart().longValue() == node.getPosition().getStart().longValue() );
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedExtentToNode(RECKDPTreeNodeImpl node) {
        return ( extent.getEnd().longValue() == node.getPosition().getEnd().longValue() - 2)
                || ( extent.getEnd().longValue() == node.getPosition().getEnd().longValue() - 1)
                || ( extent.getEnd().longValue() == node.getPosition().getEnd().longValue());
    }

    Entity entity = null;
    String id = null;
    String headword = null;
    String type = null;
    String ldcType = null;
    String role = null;
    String reference = null;
    Charseq extent = new Charseq();
    Charseq head = new Charseq();
    Charseq hwPosition = new Charseq();
}
