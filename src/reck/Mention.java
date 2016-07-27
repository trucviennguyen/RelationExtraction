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

import reck.trees.RECKDPTreeNodeImpl;
import reck.util.Charseq;

/**
 * An interface that act as an entity mention.
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Mention {
    
    public Entity getEntity();

    public void setEntity(Entity entity);

    public String getId();

    public void setId(String id);
    
    public String getHeadword();

    public void setHeadword(String headword);

    public String getType();

    public void setType(String type);
    
    public String getLDCType();

    public void setLDCType(String ldcType);

    public String getRole();

    public void setRole(String role);

    public String getReference();

    public void setReference(String reference);

    public Charseq getExtent();

    public void setExtent(Charseq extent);

    public Charseq getHead();

    public void setHead(Charseq head);
    
    public Charseq getHwPosition();

    public void setHwPosition(Charseq hwPosition);
    
    /**
     * Says if the entity mention is matched exactly range start & end
     * @return
     */
    public boolean matchedRange(Long start, Long end);
    
    /**
     * Says if the entity mention is matched exactly range start & end
     * @return
     */
    public boolean matchedNode(RECKDPTreeNodeImpl node);
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedHeadFromNode(RECKDPTreeNodeImpl node);
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedHeadToNode(RECKDPTreeNodeImpl node);
    
        /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedExtentFromNode(RECKDPTreeNodeImpl node);
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedExtentToNode(RECKDPTreeNodeImpl node);
    
}
