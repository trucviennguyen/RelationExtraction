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

import java.io.Serializable;
import java.util.ArrayList;
import reck.annotation.EntityImpl;
import reck.annotation.MentionImpl;
import reck.trees.RECKParseTreeImpl;

/**
 * An interface that act as a relation.
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Relation {

    public String getId();
    public void setId(String id);
    public String getMentionId();
    public void setMentionId(String mention_id);
    public String getType();
    public void setType(String type);
    public String getSubtype();
    public void setSubtype(String subtype);
    public String getTypeSubtype();
    public Entity getEntity(int entityIndex);
    public void setEntity(int entityIndex, EntityImpl e);
    public Mention getMention(int mentionIndex);
    public void setMention(int mentionIndex, MentionImpl mention);
    public RelationMentionTime getMentionTime();
    public void setMentionTime(RelationMentionTime mentionTime);
 
    public ArrayList getRelationTree();
    public void setRelationTree(ArrayList RelationTree);
    
    public RECKParseTreeImpl getRECKParseTree();
    public void setRECKParseTree(RECKParseTreeImpl rpTree);
    
    public class RelationMentionTime implements Cloneable, Serializable{

        public RelationMentionTime(String type, String val, String mod, String dir) {
            this.type = type;
            this.val = val;
            this.mod = mod;
            this.dir = dir;
        }

        String type;
        String val;
        String mod;
        String dir;
    }
}
