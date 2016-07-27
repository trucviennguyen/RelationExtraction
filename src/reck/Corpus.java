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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An interface that act as a corpus
 * containing a list of documents.
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Corpus extends List {

    public int size();

    public boolean isEmpty();

    public boolean contains(Object o);

    public Iterator iterator();

    public Object[] toArray();

    public Object[] toArray(Object[] a);

    public boolean add(Object o);

    public boolean remove(Object o);

    public boolean containsAll(Collection c);

    public boolean addAll(Collection c);

    public boolean addAll(int index, Collection c);

    public boolean removeAll(Collection c);

    public boolean retainAll(Collection c);

    public void clear();

    public boolean equals(Object o);

    public int hashCode();

    public Object get(int index);

    public Object set(int index, Object element);

    public void add(int index, Object element);

    public Object remove(int index);

    public int indexOf(Object o);

    public int lastIndexOf(Object o);

    public ListIterator listIterator();

    public ListIterator listIterator(int index);

    public Corpus subList(int fromIndex, int toIndex);
}
