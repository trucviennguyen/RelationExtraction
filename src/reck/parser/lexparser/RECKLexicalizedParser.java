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

package reck.parser.lexparser;

import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.AbstractEval;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Lexicon;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.Test;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Function;
import edu.stanford.nlp.process.WhitespaceTokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.Timing;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reck.trees.RECKCTTreeNodeImpl;
import reck.trees.RECKParseTreeImpl;
import reck.trees.RECKDPTreeNodeImpl;
import reck.trees.RECKTreePrint;
import reck.util.Charseq;

/**
 * A class extending the Stanford <code>LexicalizedParser</code>
 * to deal with ACE 2004 and decorate the parse trees.
 *
 * @author Truc-Vien T. Nguyen
 */
public class RECKLexicalizedParser extends LexicalizedParser {
    
    ArrayList TDs = null;
    
    private boolean fallbackToPCFG = false;
    
    Options op = super.getOp();
    
    private static boolean parseSucceeded = false;
    
    private int maxDistanceBetweenLeaves = 9;

    String[] oldStrings = {"*", "/","_"};
    String[] newStrings = {"\\*", "\\/","-"};
    
    private static final String[] converters 
            = new String[]{"anaesthetic", "analogue", "analogues", "analyse", "analysed", "analysing", /* not analyses NNS */
            "armoured", "cancelled", "cancelling", "candour", "capitalise", "capitalised", "capitalisation", "centre", "chimaeric", "clamour", "coloured", "colouring", "defence", "detour", /* "dialogue", "dialogues", */ "discolour", "discolours", "discoloured", "discolouring", "encyclopaedia", "endeavour", "endeavours", "endeavoured", "endeavouring", "fervour", "favour", "favours", "favoured", "favouring", "favourite", "favourites", "fibre", "fibres", "finalise", "finalised", "finalising", "flavour", "flavours", "flavoured", "flavouring", "glamour", "grey", "harbour", "harbours", "homologue", "homologues", "honour", "honours", "honoured", "honouring", "honourable", "humour", "humours", "humoured", "humouring", "kerb", "labelled", "labelling", "labour", "labours", "laboured", "labouring", "leant", "learnt", "localise", "localised", "manoeuvre", "manoeuvres", "maximise", "maximised", "maximising", "meagre", "minimise", "minimised", "minimising", "modernise", "modernised", "modernising", "misdemeanour", "misdemeanours", "neighbour", "neighbours", "neighbourhood", "neighbourhoods", "oestrogen", "oestrogens", "organisation", "organisations", "penalise", "penalised", "popularise", "popularised", "popularises", "popularising", "practise", "practised", "pressurise", "pressurised", "pressurises", "pressurising", "realise", "realised", "realising", "realises", "recognise", "recognised", "recognising", "recognises", "rumoured", "rumouring", "savour", "savours", "savoured", "savouring", "splendour", "splendours", "theatre", "theatres", "titre", "titres", "travelled", "travelling" };

    private static final String[] converted 
            = new String[]{"anesthetic", "analog", "analogs", "analyze", "analyzed", "analyzing", 
            "armored", "canceled", "canceling", "candor", "capitalize", "capitalized", "capitalization", "center", "chimeric", "clamor", "colored", "coloring", "defense", "detour", /* "dialog", "dialogs", */ "discolor", "discolors", "discolored", "discoloring", "encyclopedia", "endeavor", "endeavors", "endeavored", "endeavoring", "fervor", "favor", "favors", "favored", "favoring", "favorite", "favorites", "fiber", "fibers", "finalize", "finalized", "finalizing", "flavor", "flavors", "flavored", "flavoring", "glamour", "gray", "harbor", "harbors", "homolog", "homologs", "honor", "honors", "honored", "honoring", "honorable", "humor", "humors", "humored", "humoring", "curb", "labeled", "labeling", "labor", "labors", "labored", "laboring", "leaned", "learned", "localize", "localized", "maneuver", "maneuvers", "maximize", "maximized", "maximizing", "meager", "minimize", "minimized", "minimizing", "modernize", "modernized", "modernizing", "misdemeanor", "misdemeanors", "neighbor", "neighbors", "neighborhood", "neighborhoods", "estrogen", "estrogens", "organization", "organizations", "penalize", "penalized", "popularize", "popularized", "popularizes", "popularizing", "practice", "practiced", "pressurize", "pressurized", "pressurizes", "pressurizing", "realize", "realized", "realizing", "realizes", "recognize", "recognized", "recognizing", "recognizes", "rumored", "rumoring", "savor", "savors", "savored", "savoring", "splendor", "splendors", "theater", "theaters", "titer", "titers", "traveled", "traveling" };

    private static final String[] timexConverters 
            = new String[]{"january", "february", /* not "march" ! */
            "april", /* Not "may"! */ "june", "july", "august", "september", "october", "november", "december", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    private static final String[] timexConverted 
            = new String[]{"January", "February", /* not "march" ! */
            "April", /* Not "may"! */ "June", "July", "August", "September", "October", "November", "December", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
    private static Pattern[] excepts = { null,
                                         null,
                                         null,
                                         null,
                                         Pattern.compile("glamour|de[tv]our") };

    private static Pattern[] pats = { Pattern.compile("hem(at)?o"),
                                      Pattern.compile("emia$"),
                                      Pattern.compile("([lL]euk)em"),
                                      Pattern.compile("program(s?)$"),
                                      Pattern.compile("^([a-z]{3,})or(s?)$") };

    private static String[] reps = {"haem$1o", "aemia", "$1aem", "programme$1", "$1our$2" };
  
  /**
     * Construct a new LexicalizedParser object from a previously serialized
     * grammar read from a property
     * <code>edu.stanford.nlp.SerializedLexicalizedParser</code>,
     * or a default file location.
     */
    public RECKLexicalizedParser() {
        super(new Options());
    }


    /**
     * Construct a new LexicalizedParser object from a previously serialized
     * grammar read from a System property
     * <code>edu.stanford.nlp.SerializedLexicalizedParser</code>,
     * or a default file location
     * (<code>/u/nlp/data/lexparser/englishPCFG.ser.gz</code>).
     *
     * @param op Options to the parser.    These get overwritten by the
     *           Options read from the serialized parser; I think the only
     *           thing determined by them is the encoding of the grammar
     *           iff it is a text grammar
     */
    public RECKLexicalizedParser(Options op) {
        super(op);
    }


    /**
    * Construct a new LexicalizedParser.  This loads a grammar that
    * was previously assembled and stored.
    * @param parserFileOrUrl Filename/URL to load parser from
    * @param op Options for this parser. These will normally be overwritten
    *     by options stored in the file
    * @throws IllegalArgumentException If parser data cannot be loaded
    */
    public RECKLexicalizedParser(String parserFileOrUrl, Options op) {
        super(parserFileOrUrl, op);
    }

    public RECKLexicalizedParser(String parserFileOrUrl) {
        super(parserFileOrUrl);
    }

    /**
     * Construct a new LexicalizedParser.  This loads a grammar that
     * was previously assembled and stored.
     *
     * @throws IllegalArgumentException If parser data cannot be loaded
     */
    public RECKLexicalizedParser(String parserFileOrUrl, boolean isTextGrammar, Options op) {
        super(parserFileOrUrl, isTextGrammar, op);
    }
    
  /** Return a TreePrint for formatting parsed output trees.
   *  @return A TreePrint for formatting parsed output trees.
   */
    public static RECKTreePrint getRECKTreePrint(Options op) {
        TreebankLangParserParams tlpParams = op.tlpParams;
        TreebankLanguagePack tlp = tlpParams.treebankLanguagePack();
        /**
         * Determines format of output trees: choose among penn, oneline
         */
        String outputFormat = "typedDependencies";
        String outputFormatOptions = "";
        return new RECKTreePrint(outputFormat, outputFormatOptions, tlp, tlpParams.headFinder());
    }
    
    private static void printOutOfMemory(PrintWriter pw) {
        pw.println();
        pw.println("*******************************************************");
        pw.println("***  WARNING!! OUT OF MEMORY! THERE WAS NOT ENOUGH  ***");
        pw.println("***  MEMORY TO RUN ALL PARSERS.  EITHER GIVE THE    ***");
        pw.println("***  JVM MORE MEMORY, SET THE MAXIMUM SENTENCE      ***");
        pw.println("***  LENGTH WITH -maxLength, OR PERHAPS YOU ARE     ***");
        pw.println("***  HAPPY TO HAVE THE PARSER FALL BACK TO USING    ***");
        pw.println("***  A SIMPLER PARSER FOR VERY LONG SENTENCES.      ***");
        pw.println("*******************************************************");
        pw.println();
    }

  /** Adds a sentence final punctuation mark to sentences that lack one.
   *  This method adds a period (the first sentence final punctuation word
   *  in a parser language pack) to sentences that don't have one within
   *  the last 3 words (to allow for close parentheses, etc.).  It checks
   *  tags for punctuation, if available, otherwise words.
   *  @param sentence The sentence to check
   *  @param length The length of the sentence (just to avoid recomputation)
   */
  private void addSentenceFinalPunctIfNeeded(List<HasWord> sentence, int length) {
    int start = length - 3;
    if (start < 0) start = 0;
    TreebankLanguagePack tlp = op.tlpParams.treebankLanguagePack();
    for (int i = length - 1; i >= start; i--) {
      Object item = sentence.get(i);
      // An object (e.g., MapLabel) can implement HasTag but not actually store
      // a tag so we need to check that there is something there for this case.
      // If there is, use only it, since word tokens can be ambiguous.
      String tag = null;
      if (item instanceof HasTag) {
        tag = ((HasTag) item).tag();
      }
      if (tag != null && ! "".equals(tag)) {
        if (tlp.isSentenceFinalPunctuationTag(tag)) {
          return;
        }
      } else if (item instanceof HasWord) {
        String str = ((HasWord) item).word();
        if (tlp.isPunctuationWord(str)) {
          return;
        }
      } else {
        String str = item.toString();
        if (tlp.isPunctuationWord(str)) {
          return;
        }
      }
    }
    // none found so add one.
    if (Test.verbose) {
      System.err.println("Adding missing final punctuation to sentence.");
    }
    String[] sfpWords = tlp.sentenceFinalPunctuationWords();
    if (sfpWords.length > 0) {
      sentence.add(new Word(sfpWords[0]));
    }
  }    
    
  /**
   * Parse a sentence represented as a List of tokens.
   * The text must already have been tokenized and
   * normalized into tokens that are appropriate to the treebank
   * which was used to train the parser.  The tokens can be of
   * multiple types, and the list items need not be homogeneous as to type
   * (in particular, only some words might be given tags):
   * <ul>
   * <li>If a token implements HasWord, then the word to be parsed is
   * given by its word() value.</li>
   * <li>If a token implements HasTag and the tag() value is not
   * null or the empty String, then the parser is strongly advised to assign
   * a part of speech tag that <i>begins</i> with this String.</li>
   * <li>Otherwise toString() is called on the token, and the returned
   * value is used as the word to be parsed.  In particular, if the
   * token is already a String, this means that the String is used as
   * the word to be parsed.</li>
   * </ul>
   *
   * @param sentence The sentence to parse
   * @return true Iff the sentence was accepted by the grammar
   * @throws UnsupportedOperationException If the Sentence is too long or
   *                                       of zero length or the parse
   *                                       otherwise fails for resource reasons
   */
    @Override
  public boolean parse(List<? extends HasWord> sentence) {
    int length = sentence.size();
    if (length == 0) {
      throw new UnsupportedOperationException("Can't parse a zero-length sentence!");
    }
    List<HasWord> sentenceB = new ArrayList<HasWord>(sentence);
    if (Test.addMissingFinalPunctuation) {
      addSentenceFinalPunctIfNeeded(sentenceB, length);
    }
    if (length > Test.maxLength) {
      throw new UnsupportedOperationException("Sentence too long: length " + length);
    }
    TreePrint treePrint = getTreePrint();
    PrintWriter pwOut = op.tlpParams.pw();
    parseSucceeded = false;
    sentenceB.add(new Word(Lexicon.BOUNDARY));
    if (op.doPCFG) {
      if (!pparser.parse(sentenceB)) {
        return parseSucceeded;
      }
      if (Test.verbose) {
        System.out.println("PParser output");
        // pwOut.println(debinarizer.transformTree(pparser.getBestParse())); // with scores on nodes
        treePrint.printTree(debinarizer.transformTree(pparser.getBestParse()), pwOut);
      }
    }
    if (op.doDep && ! Test.useFastFactored) {
      if ( ! dparser.parse(sentenceB)) {
        return parseSucceeded;
      }
      // cdm nov 2006: should move these printing bits to the main printing section,
      // so don't calculate the best parse twice!
      if (Test.verbose) {
        System.out.println("DParser output");
        treePrint.printTree(dparser.getBestParse(), pwOut);
      }
    }
    if (op.doPCFG && op.doDep) {
      if ( ! bparser.parse(sentenceB)) {
        return parseSucceeded;
      } else {
        parseSucceeded = true;
      }
    }
    return true;
  }

    /** Parse the files with names given in the String array args elements from
     *  index argIndex on.
     */
    public ArrayList parseFile(String filename, String content, 
            int startSentence, boolean tokenized, TokenizerFactory tokenizerFactory, 
            List<List<? extends HasWord>> document,
            DocumentPreprocessor documentPreprocessor,
            Function<List<HasWord>, List<HasWord>> escaper, int tagDelimiter) {
        ArrayList treeList = new ArrayList();        
        PrintWriter pwOut = op.tlpParams.pw();
        PrintWriter pwErr = op.tlpParams.pw(System.err);
        RECKTreePrint treePrint = getRECKTreePrint(op);
        int numWords = 0;
        int numSents = 0;
        int numUnparsable = 0;
        int numNoMemory = 0;
        int numFallback = 0;
        int numSkipped = 0;
        Timing timer = new Timing();
        TreebankLanguagePack tlp = op.tlpParams.treebankLanguagePack();
        // set the tokenizer
        if (tokenized) {
            tokenizerFactory = WhitespaceTokenizer.factory();
        }
        if (tokenizerFactory == null) {
            tokenizerFactory = tlp.getTokenizerFactory();
        }
        if (Test.verbose) {
            System.err.println("parseFiles: Tokenizer factory is: " + tokenizerFactory);
            System.err.println("Sentence final words are: " + Arrays.asList(tlp.sentenceFinalPunctuationWords()));
            System.err.println("File encoding is: " + op.tlpParams.getInputEncoding());
        }
        documentPreprocessor.setTokenizerFactory(tokenizerFactory);
        documentPreprocessor.setSentenceFinalPuncWords(tlp.sentenceFinalPunctuationWords());
        documentPreprocessor.setEncoding(op.tlpParams.getInputEncoding());
        boolean saidMemMessage = false;

        // evaluation setup
        boolean runningAverages = Boolean.parseBoolean(Test.evals.getProperty("runningAverages"));
        boolean summary = Boolean.parseBoolean(Test.evals.getProperty("summary"));
        AbstractEval.ScoreEval pcfgLL = null;
        if (Boolean.parseBoolean(Test.evals.getProperty("pcfgLL"))) {
            pcfgLL = new AbstractEval.ScoreEval("pcfgLL", runningAverages);
        }
        AbstractEval.ScoreEval depLL = null;
        if (Boolean.parseBoolean(Test.evals.getProperty("depLL"))) {
            depLL = new AbstractEval.ScoreEval("depLL", runningAverages);
        }
        AbstractEval.ScoreEval factLL = null;
        if (Boolean.parseBoolean(Test.evals.getProperty("factLL"))) {
          factLL = new AbstractEval.ScoreEval("factLL", runningAverages);
        }

        /** Hide for performance
        timer.start();

        System.out.println("Parsing file: " + filename + " with " + document.size() + " sentences.");*/
        PrintWriter pwo = pwOut;

        int num = 0, docIndex = startSentence;
        for (List sentence : document) {
            // System.out.println(sentence.toString());
            num++;
            numSents++;
            int len = sentence.size();
            numWords += len;

            Tree ansTree = null;
            try {
                if ( ! parse(sentence)) {
                    pwErr.print("Sentence couldn't be parsed by grammar.");
                    if (pparser != null && pparser.hasParse() && fallbackToPCFG) {
                        pwErr.println("... falling back to PCFG parse.");
                        ansTree = getBestPCFGParse();
                        numFallback++;
                    } 
                    else {
                        pwErr.println();
                        numUnparsable++;
                    }
                } 
                else {
                    // System.out.println("Score: " + lp.pparser.bestScore);
                    ansTree = getBestParse();
                }
                if (pcfgLL != null && pparser != null) {
                    pcfgLL.recordScore(pparser, pwErr);
                }
                if (depLL != null && dparser != null) {
                    depLL.recordScore(dparser, pwErr);
                }
                if (factLL != null && bparser != null) {
                    factLL.recordScore(bparser, pwErr);
                }
            } 
            catch (OutOfMemoryError e) {
                if (Test.maxLength != -0xDEADBEEF) {
                    // this means they explicitly asked for a length they cannot handle. Throw exception.
                    pwErr.println("NOT ENOUGH MEMORY TO PARSE SENTENCES OF LENGTH " + Test.maxLength);
                    pwo.println("NOT ENOUGH MEMORY TO PARSE SENTENCES OF LENGTH " + Test.maxLength);
                    throw e;
                } 
                else {
                    if ( ! saidMemMessage) {
                        printOutOfMemory(pwErr);
                        saidMemMessage = true;
                    }
                    if (pparser.hasParse() && fallbackToPCFG) {
                        try {
                            String what = "dependency";
                            if (dparser.hasParse()) {
                                what = "factored";
                            }
                            pwErr.println("Sentence too long for " + what + " parser.  Falling back to PCFG parse...");
                            ansTree = getBestPCFGParse();
                            numFallback++;
                        } 
                        catch (OutOfMemoryError oome) {
                            oome.printStackTrace();
                            numNoMemory++;
                            pwErr.println("No memory to gather PCFG parse. Skipping...");
                            pwo.println("Sentence skipped:  no PCFG fallback.");
                            pparser.nudgeDownArraySize();
                        }
                    } 
                    else {
                        pwErr.println("Sentence has no parse using PCFG grammar (or no PCFG fallback).  Skipping...");
                        pwo.println("Sentence skipped: no PCFG fallback.");
                        numSkipped++;
                    }
                }
            } 
            catch (UnsupportedOperationException uEx) {
                pwErr.println("Sentence too long (or zero words).");
                pwo.println("Sentence skipped: too long (or zero words).");
                numWords -= len;
                numSkipped++;
            }

            if (ansTree != null) {
                computePosition(docIndex, (Sentence)sentence, content);
                TDs = treePrint.getDependencies(ansTree, reckTreeList, sentencePosition);
                if (TDs.size() > 0)
                    TDs = treePrint.orderDependencies(TDs, ansTree.getLeaves().size());
                RECKDPTreeNodeImpl DPTree = treePrint.convertToDependencyTree(ansTree, reckTreeList, sentencePosition);
                DPTree = this.splitHyphen_Dependency(DPTree);
                DPTree = this.splitPoint_Dependency(DPTree);
                RECKCTTreeNodeImpl CTTree = convertToRECKTree(ansTree, docIndex, content);
                CTTree = this.splitHyphen_Constituent(CTTree);
                CTTree = this.splitPoint_Constituent(CTTree);
                RECKParseTreeImpl rpTree = new RECKParseTreeImpl(sentence, TDs, sentencePosition, DPTree, CTTree);
                treeList.add(rpTree);
            }
            // crude addition of k-best tree printing
            if (Test.printPCFGkBest > 0 && pparser.hasParse()) {
                if (ansTree != null) {
                    computePosition(docIndex, (Sentence)sentence, content);
                    TDs = treePrint.getDependencies(ansTree, reckTreeList, sentencePosition);
                    if (TDs.size() > 0)
                        TDs = treePrint.orderDependencies(TDs, ansTree.getLeaves().size());
                    RECKDPTreeNodeImpl DPTree = treePrint.convertToDependencyTree(ansTree, reckTreeList, sentencePosition);
                    DPTree = this.splitHyphen_Dependency(DPTree);
                    DPTree = this.splitPoint_Dependency(DPTree);
                    RECKCTTreeNodeImpl CTTree = convertToRECKTree(ansTree, docIndex, content);
                    CTTree = this.splitHyphen_Constituent(CTTree);
                    CTTree = this.splitPoint_Constituent(CTTree);
                    RECKParseTreeImpl rpTree = new RECKParseTreeImpl(sentence, TDs, sentencePosition, DPTree, CTTree);
                    treeList.add(rpTree);
                }

            } 
            else if (Test.printFactoredKGood > 0 && bparser.hasParse()) {
                // DZ: debug n best trees
                if (ansTree != null) {
                    computePosition(docIndex, (Sentence)sentence, content);
                    TDs = treePrint.getDependencies(ansTree, reckTreeList, sentencePosition);
                    if (TDs.size() > 0)
                        TDs = treePrint.orderDependencies(TDs, ansTree.getLeaves().size());
                    RECKDPTreeNodeImpl DPTree = treePrint.convertToDependencyTree(ansTree, reckTreeList, sentencePosition);
                    DPTree = this.splitHyphen_Dependency(DPTree);
                    DPTree = this.splitPoint_Dependency(DPTree);
                    RECKCTTreeNodeImpl CTTree = convertToRECKTree(ansTree, docIndex, content);
                    CTTree = this.splitHyphen_Constituent(CTTree);
                    CTTree = this.splitPoint_Constituent(CTTree);
                    RECKParseTreeImpl rpTree = new RECKParseTreeImpl(sentence, TDs, sentencePosition, DPTree, CTTree);
                    treeList.add(rpTree);
                }
            }

            docIndex = sentencePosition.getEnd().intValue();

        } // for sentence : document

        if (Test.writeOutputFiles) {
            pwo.close();
        }
        System.out.println("Parsed file: " + filename + " [" + num + " sentences].");

        /** Hide for performance
        long millis = timer.stop();

        if (summary) {
            if (pcfgLL != null) pcfgLL.display(false, pwErr);
            if (depLL != null) depLL.display(false, pwErr);
            if (factLL != null) factLL.display(false, pwErr);
        }*/

        if ( saidMemMessage) {
            printOutOfMemory(pwErr);
        }
        /** Hide for performance
        double wordspersec = numWords / (((double) millis) / 1000);
        double sentspersec = numSents / (((double) millis) / 1000);
        NumberFormat nf = new DecimalFormat("0.00"); // easier way!

        System.out.println("Parsed " + numWords + " words in " + numSents +
                " sentences (" + nf.format(wordspersec) + " wds/sec; " +
                nf.format(sentspersec) + " sents/sec).");
         */ 
        if (numFallback > 0) {
            pwErr.println("  " + numFallback + " sentences were parsed by fallback to PCFG.");
        }
        if (numUnparsable > 0 || numNoMemory > 0 || numSkipped > 0) {
            pwErr.println("  " + (numUnparsable + numNoMemory + numSkipped) + " sentences were not parsed:");
            if (numUnparsable > 0) {
                pwErr.println("    " + numUnparsable + " were not parsable with non-zero probability.");
            }
            if (numNoMemory > 0) {
                pwErr.println("    " + numNoMemory + " were skipped because of insufficient memory.");
            }
            if (numSkipped > 0) {
                pwErr.println("    " + numSkipped + " were skipped as length 0 or greater than " + Test.maxLength);
            }
        }
        
        return treeList;
    } // end parseFile
    
    public void computePosition(int start, Sentence sentence, String content) {
        
        int docIndex = start;
        String st = null;

        reckTreeList = new ArrayList();
        
        for (int i = 0; i < sentence.size(); i++) {
            st = ((Word)sentence.get(i)).toString();
            int index = content.indexOf(st, docIndex);
            if (index == -1 || index - docIndex > maxDistanceBetweenLeaves) {
                if (st.indexOf("&") != -1) {
                    String tmp = st.replaceAll("&", "&amp;");
                    index = content.indexOf(tmp, docIndex);
                    if (index == -1 || index - docIndex > maxDistanceBetweenLeaves) {
                        tmp = st.replaceAll("&", "&AMP;");
                        index = content.indexOf(tmp, docIndex);
                    }
                }
                if (index != -1 && index - docIndex <= maxDistanceBetweenLeaves) {
                    docIndex = index + st.length() + 4;
                }
                else {
                    st = reConvert(st);
                    index = content.indexOf(st, docIndex);
                    if (index == -1 || index - docIndex > maxDistanceBetweenLeaves) {
                        if (st.equals("-LRB-") || st.equals("-LCB-")) {
                            int i1 = content.indexOf("(", docIndex);
                            int i2 = content.indexOf("[", docIndex);
                            int i3 = content.indexOf("{", docIndex);
                            if (i1 == -1) i1 = content.length();
                            if (i2 == -1) i2 = content.length();
                            if (i3 == -1) i3 = content.length();

                            if ( (i1 == i2) && (i1 == i3) )
                                System.out.println("Come here !");
                            else
                                if (i1 < i2) {
                                    if (i3 < i1) {
                                        // st = "{";
                                        index = i3;
                                    }
                                    else {
                                        // st = "(";
                                        index = i1;
                                    }
                                }
                                else {
                                    if (i3 < i2) {
                                        // st = "{";
                                        index = i3;
                                    }
                                    else {
                                        // st = "[";
                                        index = i2;
                                    }
                                }
                            docIndex = index + 1;
                        }

                        else if (st.equals("-RRB-") || st.equals("-RCB-")) {
                            int i1 = content.indexOf(")", docIndex);
                            int i2 = content.indexOf("]", docIndex);
                            int i3 = content.indexOf("}", docIndex);
                            if (i1 == -1) i1 = content.length();
                            if (i2 == -1) i2 = content.length();
                            if (i3 == -1) i3 = content.length();

                            if ( (i1 == i2) && (i1 == i3) )
                                System.out.println("Come here !");
                            else
                                if (i1 < i2) {
                                    if (i3 < i1) {
                                        // st = "}";
                                        index = i3;
                                    }
                                    else {
                                        // st = ")";
                                        index = i1;
                                    }
                                }
                                else {
                                    if (i3 < i2) {
                                        // st = "}";
                                        index = i3;
                                    }
                                    else {
                                        // st = "]";
                                        index = i2;
                                    }
                                }
                            docIndex = index + 1;
                        }

                        else {

                            for (int k = 0; k < newStrings.length; k++) {
                                st = st.replace(newStrings[k], oldStrings[k]);
                            }

                            String oldSubSt1 = new String(new char[] {(char)39, (char)39});
                            String oldSubSt2 = new String(new char[] {(char)96, (char)96});
                            String newSubSt = new String(new char[] {(char)34});
                            if (st.indexOf(oldSubSt1) != -1 && content.substring(docIndex).indexOf(newSubSt) != -1)
                                st = st.replace(oldSubSt1, newSubSt);
                            else if (st.indexOf(oldSubSt2)!= -1  && content.substring(docIndex).indexOf(newSubSt) != -1)
                                st = st.replace(oldSubSt2, newSubSt);

                            int i39 = content.indexOf(39, docIndex);
                            int i96 = content.indexOf(96, docIndex);

                            if ( (st.indexOf(39) != -1) && (i96 != -1 && i96 - docIndex <= maxDistanceBetweenLeaves) )
                                st = st.replace((char)39, (char)96);
                            else if ( (st.indexOf(96) != -1) && (i39 != -1 && i39 - docIndex <= maxDistanceBetweenLeaves) )
                                st = st.replace((char)96, (char)39);

                            index = content.indexOf(st, docIndex);
                            if (index == -1 || index - docIndex > maxDistanceBetweenLeaves)
                                System.out.println("Come here !");
                            else
                                docIndex = index + st.length();
                        }
                    }
                    else
                        docIndex = index + st.length();
                }
            }
            else
                docIndex = index + st.length();
            
            // Test if next node is a sentence splitter, means "."
            if (st.endsWith(".") && i < sentence.size() - 1) {
                String nextLabel = ((Word)sentence.get(i + 1)).toString();
                int nextIndex = content.indexOf(nextLabel, docIndex);
                
                if ( nextLabel.equals(".") && (nextIndex == -1 || nextIndex - docIndex > maxDistanceBetweenLeaves) ) {
                    docIndex--;
                    st = st.substring(0, st.length() - 2);
                }
            }
            // ((Word)sentence.get(i)).setWord(st);
            
            RECKDPTreeNodeImpl reckNode = new RECKDPTreeNodeImpl(new StringLabel(st), new Charseq(index, docIndex));
            reckTreeList.add(reckNode);
            
        }
        
        sentencePosition = new Charseq(start, docIndex);
        
    } // computePosition
    
    public String reConvert(String st) {

        for (int i = 0; i < converted.length; i++) 
            if (converted[i].equals(st))
                return converters[i];
        for (int i = 0; i < timexConverted.length; i++) 
            if (timexConverted[i].equals(st))
                return timexConverters[i];
        
        for (int i = 0; i < pats.length; i++) {
            Pattern ex = excepts[i];
            if (ex != null) {
                Matcher me = ex.matcher(st);
                if (me.find()) {
                    continue;
                }
            }
            Matcher m = pats[i].matcher(st);
            if (m.find()) {
                // System.err.println("Replacing " + word + " with " +
                //             pats[i].matcher(word).replaceAll(reps[i]));
                return m.replaceAll(reps[i]);
            }
        }

        return st;
    }

    public RECKCTTreeNodeImpl convertToRECKTree(Tree root, int startSentence, String content) {
        
        RECKCTTreeNodeImpl newRoot = null;
        
        Charseq pos = null;

        List nodeList = root.getLeaves();
        HashSet parentSet = new HashSet();
        int docIndex = startSentence;
        String st = null;
        
        // compute leaves' positions
        for (int i = 0; i < nodeList.size(); i++) {
            Tree oldNode = (Tree)nodeList.get(i);
            st = oldNode.toString();
            
            int start = content.indexOf(st, docIndex);
            if (start == -1 || start - docIndex > maxDistanceBetweenLeaves) {
                if (st.indexOf("&") != -1) {
                    String tmp = st.replaceAll("&", "&amp;");
                    start = content.indexOf(tmp, docIndex);
                    if (start == -1 || start - docIndex > maxDistanceBetweenLeaves) {
                        tmp = st.replaceAll("&", "&AMP;");
                        start = content.indexOf(tmp, docIndex);
                    }
                }
                if (start != -1 && start - docIndex <= maxDistanceBetweenLeaves) {
                    docIndex = start + st.length() + 4;
                }
                else {                
                    st = reConvert(st);
                    start = content.indexOf(st, docIndex);
                    if (start == -1 || start - docIndex > maxDistanceBetweenLeaves) {
                        if (st.equals("-LRB-") || st.equals("-LCB-")) {
                            int i1 = content.indexOf("(", docIndex);
                            int i2 = content.indexOf("[", docIndex);
                            int i3 = content.indexOf("{", docIndex);
                            if (i1 == -1) i1 = content.length();
                            if (i2 == -1) i2 = content.length();
                            if (i3 == -1) i3 = content.length();

                            if ( (i1 == i2) && (i1 == i3) )
                                System.out.println("Come here !");
                            else
                                if (i1 < i2) {
                                    if (i3 < i1) {
                                        // st = "{";
                                        start = i3;
                                    }
                                    else {
                                        // st = "(";
                                        start = i1;
                                    }
                                }
                                else {
                                    if (i3 < i2) {
                                        // st = "{";
                                        start = i3;
                                    }
                                    else {
                                        // st = "[";
                                        start = i2;
                                    }
                                }
                            docIndex = start + 1;
                        }

                        else if (st.equals("-RRB-") || st.equals("-RCB-")) {
                            int i1 = content.indexOf(")", docIndex);
                            int i2 = content.indexOf("]", docIndex);
                            int i3 = content.indexOf("}", docIndex);
                            if (i1 == -1) i1 = content.length();
                            if (i2 == -1) i2 = content.length();
                            if (i3 == -1) i3 = content.length();

                            if ( (i1 == i2) && (i1 == i3) )
                                System.out.println("Come here !");
                            else
                                if (i1 < i2) {
                                    if (i3 < i1) {
                                        // st = "}";
                                        start = i3;
                                    }
                                    else {
                                        // st = ")";
                                        start = i1;
                                    }
                                }
                                else {
                                    if (i3 < i2) {
                                        // st = "}";
                                        start = i3;
                                    }
                                    else {
                                        // st = "]";
                                        start = i2;
                                    }
                                }
                            docIndex = start + 1;
                        }

                        else {

                            for (int k = 0; k < newStrings.length; k++) {
                                st = st.replace(newStrings[k], oldStrings[k]);
                            }

                            String oldSubSt1 = new String(new char[] {(char)39, (char)39});
                            String oldSubSt2 = new String(new char[] {(char)96, (char)96});
                            String newSubSt = new String(new char[] {(char)34});
                            if (st.indexOf(oldSubSt1) != -1 && content.substring(docIndex).indexOf(newSubSt) != -1)
                                st = st.replace(oldSubSt1, newSubSt);
                            else if (st.indexOf(oldSubSt2)!= -1  && content.substring(docIndex).indexOf(newSubSt) != -1)
                                st = st.replace(oldSubSt2, newSubSt);

                            int i39 = content.indexOf(39, docIndex);
                            int i96 = content.indexOf(96, docIndex);

                            if ( (st.indexOf(39) != -1) && (i96 != -1 && i96 - docIndex <= maxDistanceBetweenLeaves) )
                                st = st.replace((char)39, (char)96);
                            else if ( (st.indexOf(96) != -1) && (i39 != -1 && i39 - docIndex <= maxDistanceBetweenLeaves) )
                                st = st.replace((char)96, (char)39);

                            start = content.indexOf(st, docIndex);
                            if (start == -1 || start - docIndex > maxDistanceBetweenLeaves)
                                System.out.println("Come here !");
                            else
                                docIndex = start + st.length();
                        }
                    }
                    else
                        docIndex = start + st.length();
                }
            }
            else
                docIndex = start + st.length();

            // Test if next node is a sentence splitter, means "."
            if (st.endsWith(".") && i < nodeList.size() - 1) {
                Tree nextNode = (Tree)nodeList.get(i+1);
                String nextLabel = nextNode.label().value();
                int nextStart = content.indexOf(nextLabel, docIndex);
                
                if ( nextLabel.equals(".") && (nextStart == -1 || nextStart - docIndex > maxDistanceBetweenLeaves) ) {
                    docIndex--;
                    oldNode.setLabel(new StringLabel(st.substring(0, st.length() - 1)));
                }
            }
            
            pos = new Charseq(start, docIndex);
            RECKCTTreeNodeImpl newNode = new RECKCTTreeNodeImpl(new StringLabel(st), 
                    (List)oldNode.getChildrenAsList(), pos);
            Tree parent = oldNode.parent(root);
            parent.setChild(parent.indexOf(oldNode), newNode);
            parentSet.add(parent);
        }
        
        nodeList.clear();
        nodeList.addAll(parentSet);
        
        // compute upper nodes' positions
        while (!nodeList.isEmpty()) {
            parentSet = new HashSet();
            for (int i = 0; i < nodeList.size(); i++) {
                Tree oldNode = (Tree)nodeList.get(i);
                Iterator nodeIter = oldNode.getChildrenAsList().iterator();
                Tree node = (Tree)nodeIter.next();
                while (node instanceof RECKCTTreeNodeImpl && nodeIter.hasNext()) {
                    node = (Tree)nodeIter.next();
                }
                if (node instanceof RECKCTTreeNodeImpl) {
                    Long start = ((RECKCTTreeNodeImpl)oldNode.firstChild()).getPosition().getStart();
                    Long end = ((RECKCTTreeNodeImpl)oldNode.lastChild()).getPosition().getEnd();
                    pos = new Charseq(start, end);
                    RECKCTTreeNodeImpl newNode = new RECKCTTreeNodeImpl(oldNode.label(), 
                            (List)oldNode.getChildrenAsList(), pos);
                    Tree parent = oldNode.parent(root);
                    parent.setChild(parent.indexOf(oldNode), newNode);
                    parentSet.add(parent);
                    
                    // if oldNode is in parentSet, remove it
                    if (parentSet.contains(oldNode)) {
                        parentSet.remove(oldNode);
                    }
                }
                else {
                    parentSet.add(oldNode);
                }
            }
            
            nodeList.clear();
            if (parentSet.size() == 1 && parentSet.contains(root)) {
                Long start = ((RECKCTTreeNodeImpl)root.firstChild()).getPosition().getStart();
                Long end = ((RECKCTTreeNodeImpl)root.lastChild()).getPosition().getEnd();
                pos = new Charseq(start, end);
                newRoot = new RECKCTTreeNodeImpl(root.label(), (List)root.getChildrenAsList(), pos);
            }
            else {                
                nodeList.addAll(parentSet);
            }
        }
        
        return newRoot;
        
    }
    
    public RECKCTTreeNodeImpl[] splitHyphenSt_Constituent(RECKCTTreeNodeImpl node, RECKCTTreeNodeImpl parent) {
        String label = node.label().value();
        String subSt[] = label.split("-");
        int n = subSt.length;
        long index = node.getPosition().getStart();
        RECKCTTreeNodeImpl preTerminalNode[] = new RECKCTTreeNodeImpl[2*n - 1];

        for (int i = 0; i < n; i++) {
            StringLabel  leafLb = new StringLabel(subSt[i]);
            Charseq leafPos = new Charseq(index, index + subSt[i].length());
            RECKCTTreeNodeImpl leafNode = new RECKCTTreeNodeImpl(leafLb, leafPos);
            preTerminalNode[2*i] = new RECKCTTreeNodeImpl(new StringLabel(parent.label().value()), 
                    new RECKCTTreeNodeImpl[] {leafNode}, leafPos);
            index += subSt[i].length();
            
            if (i < n - 1) {
                StringLabel hyphenLb = new StringLabel("-");
                Charseq hyphenPos = new Charseq(index, index + 1);
                RECKCTTreeNodeImpl hyphenNode = new RECKCTTreeNodeImpl(hyphenLb, hyphenPos);
                preTerminalNode[2*i + 1] = new RECKCTTreeNodeImpl(new StringLabel(parent.label().value()), 
                        new RECKCTTreeNodeImpl[] {hyphenNode}, hyphenPos);
                index++;
            }

        }        
        
        return preTerminalNode;
    }
    
    public RECKCTTreeNodeImpl splitHyphen_Constituent(RECKCTTreeNodeImpl root) {
        RECKCTTreeNodeImpl rtree = root;
        List leaves = rtree.getLeaves();
        int i = 0;
        
        while (i < leaves.size()) {
            RECKCTTreeNodeImpl node = (RECKCTTreeNodeImpl)leaves.get(i);
            String label = node.label().value();
            int indexHyphen = label.indexOf("-");
            if (indexHyphen > 0 && indexHyphen < label.length() - 1 && label.indexOf("--") == -1) {
                RECKCTTreeNodeImpl parent = node.parent(rtree);
                RECKCTTreeNodeImpl[] newChildren = splitHyphenSt_Constituent(node, parent);
                parent.setChildren(newChildren);
                
                Object o = TDs.get(i);
                for (int j = 0; j < newChildren.length - 1; j++) 
                    TDs.add(i, o);

                i += newChildren.length - 1;
            }
            i++;
        }

        return rtree;
    }
    
    public RECKCTTreeNodeImpl[] splitPointSt_Constituent(RECKCTTreeNodeImpl node, RECKCTTreeNodeImpl parent) {
        String label = node.label().value();
        int startNode = node.getPosition().getStart().intValue();
        int endNode = node.getPosition().getEnd().intValue();
        int lenNode = label.length();
        RECKCTTreeNodeImpl preTerminalNode[] = new RECKCTTreeNodeImpl[2];

        StringLabel leafLb = new StringLabel(label.substring(0, lenNode - 1));
        Charseq leafPos = new Charseq(startNode, endNode - 1);
        RECKCTTreeNodeImpl leafNode = new RECKCTTreeNodeImpl(leafLb, leafPos);
        preTerminalNode[0] = new RECKCTTreeNodeImpl(new StringLabel(parent.label().value()), 
                new RECKCTTreeNodeImpl[] {leafNode}, leafPos);
        
        StringLabel pointLb = new StringLabel(".");
        Charseq pointPos = new Charseq(endNode - 1, endNode);
        RECKCTTreeNodeImpl pointNode = new RECKCTTreeNodeImpl(pointLb, pointPos);
        preTerminalNode[1] = new RECKCTTreeNodeImpl(new StringLabel(parent.label().value()), 
                new RECKCTTreeNodeImpl[] {pointNode}, pointPos);
        
        return preTerminalNode;
    }
    
    public RECKCTTreeNodeImpl splitPoint_Constituent(RECKCTTreeNodeImpl root) {
        RECKCTTreeNodeImpl rtree = root;
        List leaves = rtree.getLeaves();
        int i = 0;
        
        while (i < leaves.size()) {
            RECKCTTreeNodeImpl node = (RECKCTTreeNodeImpl)leaves.get(i);
            String label = node.label().value();
            if (label.endsWith(".")) {
                RECKCTTreeNodeImpl parent = node.parent(rtree);
                RECKCTTreeNodeImpl[] newChildren = splitPointSt_Constituent(node, parent);
                parent.setChildren(newChildren);

                if (TDs.size() > 0) {
                    Object o = TDs.get(i);
                    for (int j = 0; j < newChildren.length - 1; j++) 
                        TDs.add(i, o);
                }
                i += newChildren.length - 1;
            }
            i++;
        }

        return rtree;
    }
    
    public RECKDPTreeNodeImpl splitHyphenSt_Dependency(RECKDPTreeNodeImpl node, RECKDPTreeNodeImpl parent) {
        String label = node.label().value();
        String subSt[] = label.split("-");
        int n = subSt.length;
        long index = node.getPosition().getEnd();
        RECKDPTreeNodeImpl current = null, next = null;
        int indexInTreeList = reckTreeList.indexOf(node);

        StringLabel lb = new StringLabel(subSt[n-1]);
        Charseq pos = new Charseq(index - subSt[n-1].length(), index);
        int idx = node.index();
        current = new RECKDPTreeNodeImpl(lb, idx, node.role(), 
                node.constituent(), node.children(), pos);
        reckTreeList.set(indexInTreeList, current);
        index -= subSt[n-1].length();

        for (int i = n - 2; i >= 0; i--) {
            index--;                            // hyphen at this point
            lb = new StringLabel(subSt[i]);
            pos = new Charseq(index - subSt[i].length(), index);
            next = new RECKDPTreeNodeImpl(lb, ++idx, node.role(), 
                    node.constituent(), new RECKDPTreeNodeImpl[] {current}, pos);
            index -= subSt[i].length();            
            current = next;
            reckTreeList.add(indexInTreeList + n - 1 - i, current);
        }
        
        return current;
    }
    
    public RECKDPTreeNodeImpl splitHyphen_Dependency(RECKDPTreeNodeImpl root) {
        RECKDPTreeNodeImpl rtree = root;
        List leaves = rtree.subTreeList();
        
        for (int i = 0; i < leaves.size(); i++) {
            RECKDPTreeNodeImpl node = (RECKDPTreeNodeImpl)leaves.get(i);
            String label = node.label().value();
            int indexHyphen = label.indexOf("-");
            if (indexHyphen > 0 && indexHyphen < label.length() - 1 && label.indexOf("--") == -1) {
                RECKDPTreeNodeImpl parent = node.parent(rtree);
                int index = parent.indexOf(node);
                RECKDPTreeNodeImpl newChild = splitHyphenSt_Dependency(node, parent);
                parent.setChild(index, newChild);
                
                for (int j = i + 1; j < leaves.size(); j++) {
                    node = (RECKDPTreeNodeImpl)leaves.get(j);
                    node.setIndex(node.index() + newChild.subTreeList().size() - 1);
                }
            }
            i++;
        }

        return rtree;
    }

    public RECKDPTreeNodeImpl splitPoint_Dependency(RECKDPTreeNodeImpl root) {
        RECKDPTreeNodeImpl rtree = root;
        List leaves = rtree.subTreeList();
        
        for (int i = 0; i < leaves.size(); i++) {
            RECKDPTreeNodeImpl node = (RECKDPTreeNodeImpl)leaves.get(i);
            String label = node.label().value();
            if (label.endsWith("."))
                node.setLabel(new StringLabel(label.substring(0, label.length() - 1)));
        }

        return rtree;
    }
    
    Charseq sentencePosition = null;
    ArrayList reckTreeList = null;
    
}
