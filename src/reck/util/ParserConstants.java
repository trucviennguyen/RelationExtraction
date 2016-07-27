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

import edu.stanford.nlp.parser.lexparser.Options;
import reck.parser.lexparser.RECKLexicalizedParser;

/**
 * A set of parameters to use with the Stanford Parser.
 * 
 * @author Truc-Vien T. Nguyen
 */
public final class ParserConstants {
    public static final Options op = new Options();
    public static final RECKLexicalizedParser lp = new RECKLexicalizedParser("englishPCFG.ser.gz", op);
}
