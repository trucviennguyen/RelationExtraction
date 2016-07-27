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

import java.io.File;
import java.io.FilenameFilter;

/**
 * A class that can list files in a folder in alphabetical order.
 * 
 * @author Truc-Vien T. Nguyen
 */
public class SortedFile extends File {

    public SortedFile(String pathname) {
        super(pathname);
    } // SortedFile
    
    @Override
    public File[] listFiles() {
        File[] lst = super.listFiles();
        File[] newList = new File[lst.length];
        for (int i = 0; i < lst.length; i++) {
            String filename = lst[i].getAbsolutePath();
            int j = 0;
            while ( (j < i) && (filename.compareTo(newList[j].getAbsolutePath())) > 0)
                j++;
            if (j == i)
                newList[i] = lst[i];
            else {
                for (int k = j; k < i; k++)
                    newList[k + 1] = newList[k];
                newList[j] = lst[i];
            }
        }
        return newList;
    }
    
    @Override
    public File[] listFiles(FilenameFilter filter) {
        File[] lst = super.listFiles(filter);
        File[] newList = new File[lst.length];
        for (int i = 0; i < lst.length; i++) {
            String filename = lst[i].getAbsolutePath();
            int j = 0;
            while ( (j < i) && (filename.compareTo(newList[j].getAbsolutePath())) > 0)
                j++;
            if (j == i)
                newList[i] = lst[i];
            else {
                for (int k = j; k < i; k++)
                    newList[k + 1] = newList[k];
                newList[j] = lst[i];
            }
        }
        return lst;
    }

} // SortedFile