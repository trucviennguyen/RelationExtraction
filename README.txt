Relation Extraction with Composite Kernel -- RECK
v1.0.0 - 2011-06-01
------------------------------------------------

Available at http://sourceforge.net/projects/reck/

Copyright (c) 2011
Truc-Vien T. Nguyen. All Rights Reserved.

Original core RECK code by Truc-Vien T. Nguyen.

This release prepared by Truc-Vien T. Nguyen.

This package contains a relation extractor: a high-accuracy relation 
extractor, based on kernel methods, with the use of structured and
flat features.

This software is written for the ACE 2004 corpus. However, it can be modified
to deal with other RE corpora such as the Roth-Yih or Wikipedia datasets.


REQUIREMENTS

	1. Java 
	This software requires Java 5 (JDK 1.5.0+).  (You must have installed it
	separately. Check that the command "java -version" works and gives 1.5+.)

	2. Stanford Parser
	This software makes use of the Stanford Parser
	Go to http://nlp.stanford.edu/software/lex-parser.shtml and download the parser.
	(recommended version: stanford-parser-2007-08-19)
	Put the stanford-parser.jar file in the RECK/lib folder.
	and the englishPCFG.ser.gz file in the RECK folder

	3. Tree Kernel Toolkit
	Go to http://disi.unitn.it/moschitti/Tree-Kernel.htm and download the SVM-light-TK.
	(that will be used to train and test the binary relation classifiers)


QUICKSTART

UNIX/WINDOWS COMMAND-LINE USAGE

On a Unix/Windows system you should be able to train the Relation Extractor with
the following commands.

	1. java -Xmx1024M input output
	This reads documents (ACE 2004 format) from folder input, 
	processes them and exports to serialized files in folder output.

	[Notes: it takes a few seconds to load the parser data. Do use the 
	lexicalized parser englishPCFG.ser.gz
	It takes more than 1 hour to parse/process 348 documents in the newswire/
	broadcast news domain of ACE 2004.]

	2. java -Xmx300M -jar RECK.jar -s 1 output
	This reads serialized files from folder output and print out statistics.

	3. java -Xmx300M -jar RECK.jar -s 2 output .
	This reads serialized files from folder output and exports file containing 
	all the entity headwords, which will be used later to generate headword 
	features in learning files.

	3. java -Xmx1024M -jar RECK.jar -s 3 output tree -r 1 -n 5
	This reads serialized files from folder output, generate potential relations,
	splits data into training/test parts with n-fold cross-validation, and 
	exports learning files to folder tree.
	[Notes: Each fold is in the folder tree/i and contains 8 pairs of training/
	test files.]

	4. java -Xmx300M -jar RECK.jar -s 4 tree tree/out.data
	This reads SVM output scores from folder tree, transform from
	binary to multi-class classification using one vs. rest strategy, 
	and exports to file tree/out.data.

	[Notes: SVM output scores are in the form i.out where i is the ith relation.]

	5. java -Xmx300M -jar RECK.jar -s 5 tree/gold.data tree/out.data
	This compares between gold data and test outcome and
	output results in form of Precision/Recall/F-measure
	for i. each relation type and ii. overall evaluation


JAVA PROGRAM PARAMETERS

	-s	mode of execution
	-r	features to be written to learning files
		-r 0	there are two kinds of features
				PET, the path-enlosed tree of the constituent parse tree (to be encoded with the subset tree kernel)
				entity features: entity type/subtype, mention type/LDCtype/role/reference, and mention headword
		-r 1	there are nine kinds of features
				PET, the path-enlosed tree of the constituent parse tree (to be encoded with the subset tree kernel)
				the dependency parse tree (to be encoded with the partial tree kernel)
				six sequences (to be encoded with the word sequence kernel)
				entity features: entity type/subtype, mention type/LDCtype/role/reference, and mention headword

	-n	n-fold cross-validation


LEARNING PARAMETERS

COMPILING THE TREE KERNEL TOOLKIT WITH CUSTOMIZED KERNEL

	There are two kernel settings (please refer to our EMNLP'09 paper for more details)
	1. The CK1 kernel (corresponding to option -r 0)
	2. The CK1_SSK kernel (corresponding to option -r 1)

	to use the first kernel, copy the file learning/CK1/kernel.h into the SVM-light-TK folder and recompile, using make.
	to use the second kernel, copy the file learning/CK1_SSK/kernel.h into the SVM-light-TK folder and recompile, using make.
	This second setting often lead to 2-3% improvement in the overall performance, with a bit slower due to the use of dependency parses and a complex kernel combination.


TRAINING THE BINARY RELATION CLASSIFIERS WITH CUSTOMIZED KERNEL SETTING

	If using the program option -r 1 and the CK1_SSK kernel setting, copy the file learning/tree_kernels.param to the same folder with svm_learn/svm_classify
	in order to encode the PET with the subset tree kernel
	the dependency parses with the partial tree kernel
	and the sequential structures with the word sequence kernel.

	then use these options for training:
	svm_learn -t 4 -U 1 


LICENSE

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


CITING THE RELATION EXTRACTOR

The main technical ideas behind how the REs works appear in this paper.
Feel free to cite the following paper.

		Convolution Kernels on Constituent, Dependency and Sequential Structures for Relation Extraction
		Truc-Vien T. Nguyen, Alessandro Moschitti, and Giuseppe Riccardi.
		Proceedings of the Conference on Empirical Methods in Natural Language Processing (EMNLP-09), Singapore, August 2009.
		http://www.aclweb.org/anthology/D/D09/D09-1143.pdf


CHANGES

This section summarizes changes between released versions of RECK.

Version 1.0.0  2011-06-01
    Initial release
