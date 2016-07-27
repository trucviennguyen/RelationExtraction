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

package reck.util;

import java.io.Serializable;

/**
 * A class represents a position in the document.
 * It contains a start and an end points.
 *
 * @author Truc-Vien T. Nguyen
 */
public class Charseq implements Cloneable, Serializable {
    
    public Charseq() {
        this.start = new Long(-1);
        this.end = new Long(-1);
    }
    
    public Charseq(Long start, Long end) {
        this.start = start;
        this.end = end;        
    }
    
    public Charseq(String start, String end) {
        this.start = new Long(start);
        this.end = new Long(end);
    }
    
    public Charseq(int start, int end) {
        this.start = new Long((long)start);
        this.end = new Long((long)end);
    }
    
    public Long getStart() {
        return start;
    }
    
    public Long getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = 97 * hash + (this.end != null ? this.end.hashCode() : 0);
        return hash;
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } 
        else if (!(o instanceof Charseq)) {
            return false;
        }
        
        Charseq pos = (Charseq) o;
        if (!(start.equals(pos.getStart()))) {
            return false;
        }
        
        if (!(end.equals(pos.getEnd()))) {
            return false;
        }
        
        return true;
    }
    
    public Charseq clone() {
        return new Charseq(start, end);
    }
    
    public int length() {
        return (int) (end - start + 1);
    }
    
    /**
     * start position
     *
     */
    Long start = null;
    
    /**
     * end position
     *
     */
    Long end = null;
}
