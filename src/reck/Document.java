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

import java.net.URL;
import java.util.ArrayList;
import reck.annotation.EntitySetImpl;
import reck.annotation.MentionListImpl;
import reck.annotation.RelationSetImpl;
import reck.trees.RECKParseTreeImpl;
import reck.trees.RECKDPTreeNodeImpl;

/**
 * An interface that act as a document
 * containing a list of entities, relations
 * and parse trees.
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Document {
    
    /** Get the content of the document. */
    public String getNoTaggedContent();
    
    /** Get the content of the document. */
    public String getRawContent();
    
    /** Get the content of the document. */
    public String getTaggedContent();
    
    /** Set the content of the document. */
    public void setTaggedContent(String content);
    
    /** Get the text filename of the document. */
    public String getTextFilename();
    
    /** Get the URI of the document. */
    public String getURI();
    
    /** Set the URI of the document. */
    public void setURI(String URI);
    
    /** Get the source of the document. */
    public String getSource();
    
    /** Set the content of the document. */
    public void setSource(String source);
    
    /** Get the type of the document. */
    public String getType();
    
    /** Set the type of the document. */
    public void setType(String type);
    
    /** Get the version of the document. */
    public String getVersion();
    
    /** Set the version of the document. */
    public void setVersion(String version);
    
    /** Get the author of the document. */
    public String getAuthor();
    
    /** Set the author of the document content source */
    public void setAuthor(String author);
    
    /** Get the encoding of the document. */
    public String getEncoding();
    
    /** Set the encoding of the document content source */
    public void setEncoding(String encoding);
    
    /** Get the docId of the document. */
    public String getDocId();
    
    /** Set the docId of the document content source */
    public void setDocId(String docId);
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public EntitySet getEntities();    

    /**
     * Set the default set of entities
     */
    public void setDefaultEntities(EntitySetImpl defaultEntities);
    
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public MentionList getMentions();  

    /**
     * Set the default set of entities
     */
    public void setDefaultMentions(MentionListImpl defaultMentions);
    
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public ArrayList getTreeList();

    /**
     * Set the default set of entities
     */
    public void setDefaultTreeList(ArrayList defaultTreeList);
    
    /** Get the default set of relations. The set is created if it
     * doesn't exist yet.
     */
    public RelationSet getRelations();

    /**
     * Set the default set of relations
     */
    public void setDefaultRelations(RelationSetImpl defaultRelations);
    
    /** Clear all the data members of the object. */
    public void cleanup();
    
    public String readTaggedContentFromFile(URL u, String encoding);
    
    public String toNoTaggedContent(String taggedContent);
    
    public void exportContentToFile(String outputFilename, String fileContent, String encoding);

    public ArrayList getParseTreeList();
    
    public RECKParseTreeImpl getParseTreeByEntity(Mention m);
    
    public RECKDPTreeNodeImpl getTreeNodeByEntity(Mention m);
    
    public void writeToXML();
    
    public void writeKernelToXML();
    
}
