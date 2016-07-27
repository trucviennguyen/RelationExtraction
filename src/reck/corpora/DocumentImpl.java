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

package reck.corpora;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Function;
import edu.stanford.nlp.trees.Tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.jdom.JDOMException;

import reck.MentionList;
import reck.EntitySet;
import reck.Mention;
import reck.RelationSet;
import reck.annotation.EntitySetImpl;
import reck.annotation.MentionImpl;
import reck.annotation.MentionListImpl;
import reck.annotation.RelationSetImpl;
import reck.trees.RECKCTTreeNodeImpl;
import reck.trees.RECKParseTreeImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.util.Charseq;
import reck.util.ParserConstants;
import reck.util.RECKConstants;

/**
 * A document consistent with ACE 2004 corpus.
 *
 * @author Truc-Vien T. Nguyen
 */
public class DocumentImpl implements reck.Document, Cloneable, Serializable {

    public DocumentImpl(String taggedContent, String URI, String source, String type, 
            String version, String author, String encoding, String docId, 
            EntitySet entities, RelationSet relations) {
        this.taggedContent = taggedContent;
        this.URI = URI;
        this.source = source;
        this.type = type;
        this.version = version;
        this.author = author;
        this.encoding = encoding;
        this.docId = docId;
        this.defaultEntities = entities;
        this.defaultMentions = new MentionListImpl(this);
        this.defaultTreeList = getParseTreeList();
        this.findAllHeadWord();
        this.defaultRelations = relations;
    }   

    public DocumentImpl(URL u) {
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document doc = builder.build(u.openStream());
            Element source_file = doc.getRootElement();
            Element docElement = (org.jdom.Element) source_file.getChild("document");
            
            this.URI = source_file.getAttributeValue("URI");
            this.source = source_file.getAttributeValue("SOURCE");
            this.type = source_file.getAttributeValue("TYPE");
            this.version = source_file.getAttributeValue("VERSION");
            this.author = source_file.getAttributeValue("AUTHOR");
            this.encoding = source_file.getAttributeValue("ENCODING");
            this.docId = docElement.getAttributeValue("DOCID");
            
            String st = u.toString();
            st = st.substring(0, st.lastIndexOf("/") + 1) + source_file.getAttributeValue("URI");
            URI sourceURI = new URI(st);
            this.taggedContent = readTaggedContentFromFile(sourceURI.toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultTreeList = getParseTreeList();
            this.findAllHeadWord();
            this.defaultRelations = new RelationSetImpl(this, docElement);            
        }
        catch (JDOMException jdomEx) {
            jdomEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        catch (java.net.URISyntaxException uriEx) {
            uriEx.printStackTrace();
        }
    }
    
    public DocumentImpl(File fs) {
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document doc = builder.build(fs);
            Element source_file = doc.getRootElement();
            Element docElement = (org.jdom.Element) source_file.getChild("document");
            
            this.URI = source_file.getAttributeValue("URI");
            this.source = source_file.getAttributeValue("SOURCE");
            this.type = source_file.getAttributeValue("TYPE");
            this.version = source_file.getAttributeValue("VERSION");
            this.author = source_file.getAttributeValue("AUTHOR");
            this.encoding = source_file.getAttributeValue("ENCODING");
            this.docId = docElement.getAttributeValue("DOCID");
            
            String st = fs.getCanonicalPath();
            st = st.substring(0, st.lastIndexOf("\\") + 1);
            
            URI = URI.replace('.', '_').replaceAll("_sgm", ".SGM");
            
            String sourceFilename = st + URI;
            this.taggedContent = readTaggedContentFromFile(new File(sourceFilename).toURI().toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            this.textFilename = 
                    sourceFilename.substring(0, sourceFilename.lastIndexOf(".")) 
                    + ".txt";
            this.exportContentToFile(textFilename, rawContent, encoding);
            
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultTreeList = getParseTreeList();
            this.findAllHeadWord();
            this.defaultRelations = new RelationSetImpl(this, docElement);
        }
        catch (JDOMException jdomEx) {
            jdomEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
    
    public DocumentImpl(Element source_file) {
        Element docElement = (org.jdom.Element) source_file.getChild("document");
        this.URI = source_file.getAttributeValue("URI");
        this.source = source_file.getAttributeValue("SOURCE");
        this.type = source_file.getAttributeValue("TYPE");
        this.version = source_file.getAttributeValue("VERSION");
        this.author = source_file.getAttributeValue("AUTHOR");
        this.encoding = source_file.getAttributeValue("ENCODING");
        this.docId = docElement.getAttributeValue("DOCID");

        File fs = new File(URI);
        try {
            this.taggedContent = readTaggedContentFromFile(fs.toURI().toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultTreeList = getParseTreeList();
            this.findAllHeadWord();
            this.defaultRelations = new RelationSetImpl(this, docElement);
        }
        catch (MalformedURLException urlEx) {
            urlEx.printStackTrace();
        }
    }
    
    /** Get the content of the document. */
    public String getTaggedContent() {
        return taggedContent;
    } // getContent()
    
    /** Get the content of the document. */
    public String getNoTaggedContent() {
        return noTaggedContent;
    } // getContent()
    
    /** Get the content of the document. */
    public String getRawContent() {
        return rawContent;
    } // getContent()
    
    /** Set the content of the document. */
    public void setTaggedContent(String taggedContent) {
        this.taggedContent = taggedContent;
    } // setContent()
    
    /** Get the text filename of the document. */
    public String getTextFilename() {
        return textFilename;
    } // getTextFilename()
    
    /** Get the URI of the document. */
    public String getURI() {
        return URI;
    } // getURI()
    
    /** Set the URI of the document. */
    public void setURI(String URI) {
        this.URI = URI;
    } // setURI()
    
    /** Get the source of the document. */
    public String getSource() {
        return source;
    } // getSource()
    
    /** Set the content of the document. */
    public void setSource(String source) {
        this.source = source;
    } // setSource()
    
    /** Get the type of the document. */
    public String getType() {
        return type;
    } // getType()
    
    /** Set the type of the document. */
    public void setType(String type) {
        this.type = type;
    } // setType()
    
    /** Get the version of the document. */
    public String getVersion() {
        return version;
    } // getVersion()
    
    /** Set the version of the document. */
    public void setVersion(String version) {
        this.version = version;
    } // setVersion()
    
    /** Get the author of the document. */
    public String getAuthor() {
        return author;
    } // getAuthor()
    
    /** Set the author of the document content source */
    public void setAuthor(String author) {
        this.author = author;
    } // setAuthor()
    
    /** Get the encoding of the document. */
    public String getEncoding() {
        return encoding;
    } // getEncoding()
    
    /** Set the encoding of the document content source */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    } // setEncoding()
    
    /** Get the docId of the document. */
    public String getDocId() {
        return docId;
    } // getDocId()
    
    /** Set the docId of the document content source */
    public void setDocId(String docId) {
        this.docId = docId;
    } // setDocId()
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public EntitySet getEntities() {
        if( defaultEntities == null){
            defaultEntities = new EntitySetImpl(this);
        }//if
        return defaultEntities;
    } // getEntities()
    
    /**
     * Set the default set of entities
     */
    public void setDefaultEntities(EntitySetImpl defaultEntities) {
        this.defaultEntities = defaultEntities;
    }
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public MentionList getMentions() {
        if( defaultMentions == null){
            defaultMentions = new MentionListImpl(this);
        }//if
        return defaultMentions;
    } // getEntities()
    
    /**
     * Set the default set of entities
     */
    public void setDefaultMentions(MentionListImpl defaultMentions) {
        this.defaultMentions = defaultMentions;
    }
    
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public ArrayList getTreeList() {
        return defaultTreeList;
    }

    /**
     * Set the default set of entities
     */
    public void setDefaultTreeList(ArrayList defaultTreeList) {
        this.defaultTreeList = defaultTreeList;
    }
    
    /** Get the default set of relations. The set is created if it
     * doesn't exist yet.
     */
    public RelationSet getRelations() {
        if( defaultRelations == null){
            defaultRelations = new RelationSetImpl(this);
        }//if
        return defaultRelations;
    } // getEntities()
    
    /**
     * Set the default set of relations
     */
    public void setDefaultRelations(RelationSetImpl defaultRelations) {
        this.defaultRelations = defaultRelations;
    }
    
    /** Clear all the data members of the object. */
    public void cleanup() {
        defaultEntities = null;
        defaultRelations = null;
    } // cleanup()
    
    public String readTaggedContentFromFile(URL u, String encoding) {
        String docContent = "";

        try {
            int readLength = 0;
            char[] readBuffer = new char[RECKConstants.INTERNAL_BUFFER_SIZE];

            BufferedReader uReader = null;
            StringBuffer buf = new StringBuffer();
            char c;
            long toRead = Long.MAX_VALUE;
            
            if(encoding != null && !encoding.equalsIgnoreCase("")) {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream(), encoding), RECKConstants.INTERNAL_BUFFER_SIZE);
            } 
            else {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream()), RECKConstants.INTERNAL_BUFFER_SIZE);
            }
            
            // read gtom source into buffer
            while (toRead > 0 && (readLength = uReader.read(readBuffer, 0, RECKConstants.INTERNAL_BUFFER_SIZE)) != -1) {
                if (toRead <  readLength) {
                    //well, if toRead(long) is less than readLenght(int)
                    //then there can be no overflow, so the cast is safe
                    readLength = (int)toRead;
                }

                buf.append(readBuffer, 0, readLength);
                toRead -= readLength;
            }

            // 4.close reader
            uReader.close();

            docContent = new String(buf);
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();            
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return docContent;
    }
    
    public static String trimUnrealReturns(String st) {
        String ret = st, text = st, pre = "";
        while (text.contains("\n")) {
            int i = text.indexOf("\n");
            
            if ( (i < text.length() - 1) && (text.charAt(i + 1) == '\n') ) {
                pre = pre + text.substring(0, i + 2);
                text = text.substring(i + 2);
            }
            else {

                // char before return is a letter
                if (pre.length() + i >= startSentence) {
                    text = text.substring(0, i) + " " + text.substring(i + 1);
                    ret = pre + text;
                }
                else {
                    pre = pre + text.substring(0, i + 1);
                    text = text.substring(i + 1);
                }

            }

        }
        
        return ret;
    }

    public String toNoTaggedContent(String taggedContent) {
        String noTaggedCT = "";
        int startRawText = -1, endRawText = -1;
        
        int count = 0;
        while (count < taggedContent.length()) {
            int tagIndex = count;
            while (tagIndex < taggedContent.length() && taggedContent.charAt(tagIndex) != '<') tagIndex++;
            if (tagIndex < taggedContent.length()) {
                noTaggedCT = noTaggedCT + taggedContent.substring(count, tagIndex);
                while (tagIndex < taggedContent.length() && taggedContent.charAt(tagIndex) != '>') tagIndex++;
                count = tagIndex + 1;
                if (taggedContent.substring(0, count).endsWith("<BODY>")) {
                    startRawText = noTaggedCT.length();
                }
                if (taggedContent.substring(0, count).endsWith("</BODY>")) {
                    endRawText = noTaggedCT.length();
                } 
            }
            else {
                noTaggedCT = noTaggedCT + taggedContent.substring(count);
                count = tagIndex;
            }
        }
        
        noTaggedCT = trimUnrealReturns(noTaggedCT);
        
        if (startRawText > 0) {
            this.rawContent = noTaggedCT.substring(startRawText, endRawText);
            DocumentImpl.startSentence = startRawText;
        }
        
        return noTaggedCT;
    }
    
    public void exportContentToFile(String outputFilename, String fileContent, String encoding) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename)), encoding));
            writer.write(fileContent);
            writer.close();
        }
        catch (FileNotFoundException fileEx) {
            fileEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        
    }
    
    public boolean isNoun(RECKDPTreeNodeImpl node) {

        if (!node.label().value().startsWith("N"))
            return false;
        
        List children = node.getChildrenAsList();
        for  (int i = 0; i < children.size(); i++ ) {
            RECKDPTreeNodeImpl child = ((RECKDPTreeNodeImpl)children.get(i));
            if (!child.isLeaf() && !(child.label().value().startsWith("N")) )
                return false;
        }
            
        return true;
    }
    
    public RECKParseTreeImpl findDPHeadWord(RECKParseTreeImpl parseTree, MentionImpl mention) {
        
        /*if (mention.getId().equals("54-86") || mention.getId().equals("52-85"))
            System.out.println();*/
        
        RECKDPTreeNodeImpl DPTreeNode = parseTree.getDPParseTree();
        int start = mention.getHead().getStart().intValue();
        int end = mention.getHead().getEnd().intValue();
        
        ArrayList leaves = parseTree.getDPTreeList();

        int n = leaves.size(), i = 0, j = n - 1, k;

        int leftIndex = ((RECKDPTreeNodeImpl)leaves.get(i)).getPosition().getStart().intValue();
        int rightIndex = ((RECKDPTreeNodeImpl)leaves.get(j)).getPosition().getEnd().intValue();
        int leftID = leftIndex, rightID = rightIndex;
        RECKDPTreeNodeImpl leftNode = (RECKDPTreeNodeImpl)leaves.get(i), rightNode = (RECKDPTreeNodeImpl)leaves.get(j);

        while (i < n - 1 && leftIndex < start) {
            i++;
            leftID = leftIndex;
            leftNode = (RECKDPTreeNodeImpl)leaves.get(i);
            leftIndex = leftNode.getPosition().getStart().intValue();
        }
        while (j > 0 && end < rightIndex) {
            j--;
            rightID = rightIndex;
            rightNode = (RECKDPTreeNodeImpl)leaves.get(j);
            rightIndex = rightNode.getPosition().getEnd().intValue();
        }

        if ( (leftIndex > start) && (leftID == start - 1) ) {
            i--;
            leftNode = (RECKDPTreeNodeImpl)leaves.get(i);
            leftIndex = leftNode.getPosition().getStart().intValue();
        }
        if ( (end > rightIndex) && (rightID == end + 1) ) {
            j++;
            rightNode = (RECKDPTreeNodeImpl)leaves.get(j);
            rightIndex = rightNode.getPosition().getEnd().intValue();
        }
        
        leftID = i; rightID = j;
        
        RECKDPTreeNodeImpl terminal = null;
        
        if (leftID < rightID) {
            for (k = rightID; k >= leftID; k--) {
                terminal = (RECKDPTreeNodeImpl)leaves.get(k);

                // re-define the head word of the mention when a preposition exists
                if ( terminal.role().equals("prep") && (k > leftID) ) {
                    k--;
                    break;
                }
            }
        }
        else {
            k = leftID;
        }
        
        
        RECKDPTreeNodeImpl origin = (k >= leftID)?(RECKDPTreeNodeImpl)leaves.get(k):(RECKDPTreeNodeImpl)leaves.get(rightID);
        if (k >= leftID) {
            origin = (RECKDPTreeNodeImpl)leaves.get(k);            
            rightIndex = origin.getPosition().getEnd().intValue();
            rightID = k;
        }
        else {
            origin = (RECKDPTreeNodeImpl)leaves.get(rightID);
        }

        StringLabel newLabel = new StringLabel(mention.getEntity().getType());
        Charseq newPosition = null;
        RECKDPTreeNodeImpl newNode = null;
        
        newPosition = origin.getPosition().clone(); 
        mention.setHeadword( RECKConstants.trimReturn(noTaggedContent.substring(origin.getPosition().getStart().intValue(),
                origin.getPosition().getEnd().intValue())) );
        mention.setHwPosition(new Charseq(origin.getPosition().getStart().intValue(),
                origin.getPosition().getEnd().intValue() - 1) );

        RECKDPTreeNodeImpl upper = origin.parent(DPTreeNode);
        Tree[] newChildren = {origin};
        int index = upper.indexOf(origin);
        newNode = new RECKDPTreeNodeImpl(newLabel, newChildren, newPosition) ;
        upper.setChild(index, newNode);
        
        ArrayList nodeList = (ArrayList)parseTree.getDPEntityTrees().get(mention);
        if (nodeList == null) {
            nodeList = new ArrayList();
            nodeList.add(newNode);
            parseTree.getDPEntityTrees().put(mention, nodeList);
        }
        
        return parseTree;
    }
    
    public RECKParseTreeImpl findCTHeadWord(RECKParseTreeImpl parseTree, MentionImpl mention) {
        
        RECKCTTreeNodeImpl CTTreeNode = parseTree.getCTParseTree();
        int start = mention.getHead().getStart().intValue();
        int end = mention.getHead().getEnd().intValue();
        
        ArrayList leaves = new ArrayList(CTTreeNode.getLeaves());

        int n = leaves.size(), i = 0, j = n - 1, k;

        int leftIndex = ((RECKCTTreeNodeImpl)leaves.get(i)).getPosition().getStart().intValue();
        int rightIndex = ((RECKCTTreeNodeImpl)leaves.get(j)).getPosition().getEnd().intValue();
        int leftID = leftIndex, rightID = rightIndex;
        RECKCTTreeNodeImpl leftNode = (RECKCTTreeNodeImpl)leaves.get(i), rightNode = (RECKCTTreeNodeImpl)leaves.get(j);

        while (i < n - 1 && leftIndex < start) {
            i++;
            leftID = leftIndex;
            leftNode = (RECKCTTreeNodeImpl)leaves.get(i);
            leftIndex = leftNode.getPosition().getStart().intValue();
        }
        while (j > 0 && end < rightIndex) {
            j--;
            rightID = rightIndex;
            rightNode = (RECKCTTreeNodeImpl)leaves.get(j);
            rightIndex = rightNode.getPosition().getEnd().intValue();
        }

        if ( (leftIndex > start) && (leftID == start - 1) ) {
            i--;
            leftNode = (RECKCTTreeNodeImpl)leaves.get(i);
            leftIndex = leftNode.getPosition().getStart().intValue();
        }
        if ( (end > rightIndex) && (rightID == end + 1) ) {
            j++;
            rightNode = (RECKCTTreeNodeImpl)leaves.get(j);
            rightIndex = rightNode.getPosition().getEnd().intValue();
        }
        
        leftID = i; rightID = j;
        
        RECKCTTreeNodeImpl terminal = null;
        RECKCTTreeNodeImpl preTerminal = null;
        RECKCTTreeNodeImpl prePreTerminal = null;
        
        if (leftID < rightID) {
            for (k = rightID; k >= leftID; k--) {
                terminal = (RECKCTTreeNodeImpl)leaves.get(k);
                preTerminal = terminal.parent(CTTreeNode);
                prePreTerminal = preTerminal.parent(CTTreeNode);

                // re-define the head word of the mention when a preposition exists
                if ( (preTerminal.label().value().equals("IN"))
                        && (prePreTerminal.label().value().equals("PP")) 
                        && k > leftID) {
                    k--;
                    break;
                }
            }
        }
        else {
            k = leftID;
        }
        
        
        RECKCTTreeNodeImpl origin = (k >= leftID)?(RECKCTTreeNodeImpl)leaves.get(k):(RECKCTTreeNodeImpl)leaves.get(rightID);
        if (k >= leftID) {
            origin = (RECKCTTreeNodeImpl)leaves.get(k);            
            rightIndex = origin.getPosition().getEnd().intValue();
            rightID = k;
        }
        else {
            origin = (RECKCTTreeNodeImpl)leaves.get(rightID);
        }        
        RECKCTTreeNodeImpl upper = origin.parent(CTTreeNode);
        while ( (upper.getPosition().getStart().intValue() >= leftIndex)
                && (upper.getPosition().getEnd().intValue() == rightIndex)) {
            origin = upper;
            upper = upper.parent(CTTreeNode);
        }


        StringLabel newLabel = new StringLabel(mention.getEntity().getType());
        Charseq newPosition = null;
        RECKCTTreeNodeImpl newNode = null;
        
        /** The case where upper covers more than mention head 
         */
        if ( (upper.getPosition().getStart().intValue() < leftIndex)
                && (origin.getPosition().getStart().intValue() > leftIndex) ) {
            RECKCTTreeNodeImpl child = origin;
            int r = upper.indexOf(child), l = r;
            while (child.getPosition().getStart().intValue() > leftIndex) {
                l--;
                child = (RECKCTTreeNodeImpl)upper.getChild(l);
            }
            if (child.getPosition().getStart().intValue() == leftIndex) {
                RECKCTTreeNodeImpl leftChild = (RECKCTTreeNodeImpl)upper.getChild(l);
                RECKCTTreeNodeImpl rightChild = (RECKCTTreeNodeImpl)upper.getChild(r);
                leftIndex = leftChild.getPosition().getStart().intValue();
                rightIndex = rightChild.getPosition().getEnd().intValue();
                newPosition = new Charseq(leftIndex, rightIndex);
                mention.setHeadword( RECKConstants.trimReturn(
                        noTaggedContent.substring(leftIndex, rightIndex)) );
                mention.setHwPosition(new Charseq(leftIndex, rightIndex - 1));                

                Tree[] children = new Tree[r - l + 1];

                for (int m = l; m < r + 1; m++)
                    children[m - l] = upper.getChild(m);

                newNode = new RECKCTTreeNodeImpl(newLabel, children, newPosition);
                Tree[] newChildren = new Tree[upper.numChildren() - newNode.numChildren() + 1];

                for (int m = 0; m < l; m++) {
                    newChildren[m] = upper.getChild(m);
                }

                newChildren[l] = newNode;

                for (int m = r + 1; m < upper.numChildren(); m++) {
                    newChildren[m - r + l] = upper.getChild(m);
                }
                upper.setChildren(newChildren);
                
            }
        }
        
        if (newNode == null) {      
            newPosition = origin.getPosition().clone(); 
            mention.setHeadword( RECKConstants.trimReturn(noTaggedContent.substring(origin.getPosition().getStart().intValue(),
                    origin.getPosition().getEnd().intValue())) );
            mention.setHwPosition(new Charseq(origin.getPosition().getStart().intValue(),
                    origin.getPosition().getEnd().intValue() - 1) );

            if (origin.isPreTerminal()) {
                Tree[] newChildren = {origin};
                int index = upper.indexOf(origin);
                newNode = new RECKCTTreeNodeImpl(newLabel, newChildren, newPosition) ;
                upper.setChild(index, newNode);
            }
            else {            
                newNode = new RECKCTTreeNodeImpl(newLabel, origin.children(), newPosition) ;
                Tree[] newChildren = {newNode};
                origin.setChildren(newChildren);
            }            
        }
        
        ArrayList nodeList = (ArrayList)parseTree.getCTEntityTrees().get(mention);
        if (nodeList == null) {
            nodeList = new ArrayList();
            nodeList.add(newNode);
            parseTree.getCTEntityTrees().put(mention, nodeList);
        }
        
        return parseTree;
    }
    
    public void findAllHeadWord() {
        int i, j = 0;
        RECKParseTreeImpl parseTree = null;
        MentionImpl mention = null;
        
        for (i = 0; i < defaultTreeList.size(); i++) {
            
            parseTree = (RECKParseTreeImpl)defaultTreeList.get(i);
            
            if (mention != null) {

                while ( (j < defaultMentions.size() - 1)
                        && (mention.getExtent().getEnd().intValue() <= parseTree.getPosition().getStart().intValue()) ) {
                    j++;
                    mention = (MentionImpl)defaultMentions.get(j);
                }
            }

            

            while (j < defaultMentions.size()) {
                mention = (MentionImpl)defaultMentions.get(j);
                int startExtent = mention.getExtent().getStart().intValue();
                int endExtent = mention.getExtent().getEnd().intValue() + 1;
                if ( (startExtent >= parseTree.getPosition().getStart().intValue() - 1)
                        && (endExtent <= parseTree.getPosition().getEnd().intValue() + 1) ) {

                    if (parseTree.getDPTreeList().size() > 0)
                        parseTree = findDPHeadWord(parseTree, mention);
                    parseTree = findCTHeadWord(parseTree, mention);

                    j++;
                }
                else
                    break;
            }

            defaultTreeList.set(i, parseTree);
        }
    }
    
    public ArrayList getParseTreeList() {
        
        // variables needed to process the files to be parsed
        TokenizerFactory tokenizerFactory = null;
        DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor();
        boolean tokenized = false; // whether or not the input file has already been tokenized
        Function<List<HasWord>, List<HasWord>> escaper = null;
        int tagDelimiter = -1;
        String sentenceDelimiter = null;
        String elementDelimiter = null;
        Options op = ParserConstants.lp.getOp();
        PrintWriter pwErr = op.tlpParams.pw(System.err);        
        
        try {
            if (elementDelimiter != null) {
                document = documentPreprocessor.getSentencesFromXML(textFilename, escaper, elementDelimiter, sentenceDelimiter);
            }
            else {
                document = documentPreprocessor.getSentencesFromText(textFilename, escaper, sentenceDelimiter, tagDelimiter);
            }
        }
        catch (IOException e) {
            pwErr.println("ERROR: Couldn't open file: " + textFilename);
        }
        
        ArrayList treeList = ParserConstants.lp.parseFile(textFilename, 
                noTaggedContent, startSentence, tokenized, tokenizerFactory, 
                document,
                documentPreprocessor, 
                escaper, tagDelimiter);
        
        return treeList;
    }
    
    public RECKParseTreeImpl getParseTreeByEntity(Mention m) {
        RECKParseTreeImpl rpTree = null;
        
        for (int i = 0; i < defaultTreeList.size(); i++) {
            rpTree = (RECKParseTreeImpl)defaultTreeList.get(i);
            if (rpTree.getDPTreeByEntity(m) != null)
                break;
        }
        
        return rpTree;
    }
    
    public RECKDPTreeNodeImpl getTreeNodeByEntity(Mention m) {
        RECKParseTreeImpl rpTree = null;
        RECKDPTreeNodeImpl rtree = null;
        
        for (int i = 0; i < defaultTreeList.size(); i++) {
            rpTree = (RECKParseTreeImpl)defaultTreeList.get(i);
            rtree = rpTree.getDPTreeByEntity(m);
            if (rtree != null)
                break;
        }
        
        return rtree;
        
    }
    
    public void writeToFile(String outputPath) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(outputPath + "\\" + docId + ".document"));
        out.writeObject(this);
        out.close(); // Also flushes output    
    }
    
    public void writeToXML() {
        
    }
    
    public void writeKernelToXML() {
        
    }
  
    String taggedContent = null;
    String noTaggedContent = null;
    String rawContent = null;
    static int startSentence = -1;
    String URI = null;
    String source = null;
    String type = null;
    String version = null;
    String author = null;
    String encoding = null;
    String docId = null;
    
    
    /**
     * Text file containing the content of the source file
     */
    String textFilename = null;
    
    /** The default entity set */
    protected EntitySet defaultEntities = null;
    
    /** The default entity set */
    protected MentionList defaultMentions = null;
    
    /** The default relation set */
    protected RelationSet defaultRelations = null;  
    
    /** list of trees (parse tree combined with matched entities) 
     * produced for sentences in the document 
     */
    protected ArrayList defaultTreeList;   
    List<List<? extends HasWord>> document = null; // initialized in getParseTreeList

}
