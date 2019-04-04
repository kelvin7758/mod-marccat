package org.folio.marccat.business.descriptor;


import org.folio.marccat.model.Subfield;
import org.folio.marccat.util.StringText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.folio.marccat.config.log.Global.EMPTY_STRING;

/**
 * Utilities to help in the creation of sortforms. This class was introduced to
 * support "in assembly" production of sortforms (as opposed to invoking the db
 * stored procedure.
 * <p>
 * Both methods are supported and this class holds the static flag isUseDatabase
 * to control which method is used.
 * <p>
 * Note that this does not affect the behaviour of the existing
 * DescriptorDAO.CalculateSortForm(Descriptor d) which continues to be used by
 * the application for such things as checking for duplicate headings, etc.
 * <p>
 * Also note, that the actual sortform columns in the db are not generated by
 * the application, but are done in triggers in the DB.
 */
public class SortformUtils {

  private static final List<Character> punctuationMark1List = new ArrayList<>(
    Arrays.asList(
      '\u0021',
      '\u002c',
      '\u002d',
      '\u002f',
      '\u003a',
      '\u003b',
      '\u003d',
      '\u003f',
      '\u0040',
      '\u005f'
    ));
  private static SortformUtils instance;
  private static List<Character> punctuationMark2List = new ArrayList<>(
    Arrays.asList(
      '\u0021',
      '\u0022',
      '\u0026',
      '\u002a',
      '\u002b',
      '\u002c',
      '\u002d',
      '\u002e',
      '\u002f',
      '\u003a',
      '\u003b',
      '\u003d',
      '\u003f',
      '\u005f'
    ));
  private static List<Character> deweyPunctuationMarkList = new ArrayList<>(
    Arrays.asList(
      '\u0021',
      '\u0022',
      '\u0023',
      '\u0024',
      '\u0025',
      '\u0026',
      '\'',
      '\u0028',
      '\u0029',
      '\u002a',
      '\u002b',
      '\u002c',
      '\u002d',
      '\u002e',
      '\u002f',
      '\u003a',
      '\u003b',
      '\u003d',
      '\u003f',
      '\u0040',
      '\u005f'
    ));
  private final TreeSet arabicWordExceptions = new TreeSet(
    Arrays
      .asList(
        "\u0622\u0644",
        "\u0622\u0644\u0622\u0645",
        "\u0622\u0644\u0627",
        "\u0622\u0644\u0627\u0621",
        "\u0622\u0644\u0627\u062A",
        "\u0622\u0644\u0627\u0641",
        "\u0622\u0644\u0627\u0645",
        "\u0622\u0644\u0629",
        "\u0622\u0644\u0647",
        "\u0622\u0644\u0647\u0629",
        "\u0622\u0644\u0647\u0647",
        "\u0622\u0644\u064A\u0629",
        "\u0622\u0644\u064A\u0647",
        "\u0623\u0644",
        "\u0623\u0644\u0622\u0644\u0627\u062A\u064A",
        "\u0623\u0644\u0622\u0644\u0627\u062C\u0649",
        "\u0623\u0644\u0622\u0644\u0627\u062C\u0649",
        "\u0623\u0644\u0622\u0646",
        "\u0623\u0644\u0623\u0644\u0628\u064A\u0631\u0649",
        "\u0623\u0644\u0623\u0644\u0628\u064A\u0631\u064A",
        "\u0623\u0644\u0623\u0644\u062A\u064A\u0643",
        "\u0623\u0644\u0623\u0644\u0632\u0627\u0633",
        "\u0623\u0644\u0623\u0644\u0645\u0627\u0646\u064A\u062A",
        "\u0623\u0644\u0623\u0644\u064A\u0627\u0646\u0629",
        "\u0623\u0644\u0623\u0644\u064A\u0627\u0646\u0647",
        "\u0623\u0644\u0627",
        "\u0623\u0644\u0627\u0628\u0627\u0645\u0627",
        "\u0623\u0644\u0627\u062A",
        "\u0623\u0644\u0627\u0633\u0643\u0627",
        "\u0623\u0644\u0627\u0639\u064A\u0628",
        "\u0623\u0644\u0627\u0644\u0627\u062A\u0649",
        "\u0623\u0644\u0627\u0644\u062A\u064A\u0643",
        "\u0623\u0644\u0627\u0644\u0632\u0627\u0633",
        "\u0623\u0644\u0627\u0644\u0645\u0627\u0646\u064A\u062A",
        "\u0623\u0644\u0627\u0646",
        "\u0623\u0644\u0628\u0627\u0628",
        "\u0623\u0644\u0628\u0627\u0646",
        "\u0623\u0644\u0628\u0627\u0646\u0649",
        "\u0623\u0644\u0628\u0627\u0646\u064A",
        "\u0623\u0644\u0628\u0627\u0646\u064A\u0627",
        "\u0623\u0644\u0628\u0631\u062A",
        "\u0623\u0644\u0628\u0631\u062A\u0627",
        "\u0623\u0644\u0628\u0631\u062A\u0648",
        "\u0623\u0644\u0628\u0631\u062A\u0648\u0633",
        "\u0623\u0644\u0628\u0633\u0629",
        "\u0623\u0644\u0628\u0633\u0647",
        "\u0623\u0644\u0628\u0648\u0645",
        "\u0623\u0644\u0628\u064A\u0631",
        "\u0623\u0644\u062A\u0631\u0627\u0641\u064A\u0634",
        "\u0623\u0644\u062A\u0631\u0627\u0645\u064A\u0643\u0631\u0648\u0633\u0643\u0648\u0628",
        "\u0623\u0644\u062A\u0648\u0646",
        "\u0623\u0644\u062C\u0645\u0629",
        "\u0623\u0644\u062C\u0645\u0647",
        "\u0623\u0644\u062C\u0648\u0644",
        "\u0623\u0644\u062D",
        "\u0623\u0644\u062D\u0627\u0646",
        "\u0623\u0644\u062D\u0642",
        "\u0623\u0644\u062E",
        "\u0623\u0644\u062F",
        "\u0623\u0644\u062F\u0631\u0633\u0648\u0646",
        "\u0623\u0644\u0633\u0646",
        "\u0623\u0644\u0633\u0646\u0629",
        "\u0623\u0644\u0633\u0646\u0647",
        "\u0623\u0644\u0633\u0646\u0649",
        "\u0623\u0644\u0633\u0646\u064A",
        "\u0623\u0644\u0633\u0646\u064A\u0629",
        "\u0623\u0644\u0633\u0646\u064A\u0647",
        "\u0623\u0644\u0635\u0642",
        "\u0623\u0644\u0637\u0627\u0641",
        "\u0623\u0644\u0637\u0648",
        "\u0623\u0644\u0638\u0646\u0628\u063A\u0627\u0621",
        "\u0623\u0644\u0639\u0627\u0628",
        "\u0623\u0644\u0639\u0648\u0628\u0629",
        "\u0623\u0644\u0639\u0648\u0628\u0647",
        "\u0623\u0644\u063A\u0627\u0621",
        "\u0623\u0644\u063A\u0627\u0632",
        "\u0623\u0644\u063A\u0627\u0645",
        "\u0623\u0644\u063A\u0649",
        "\u0623\u0644\u0641",
        "\u0623\u0644\u0641\u0627",
        "\u0623\u0644\u0641\u0627\u0631",
        "\u0623\u0644\u0641\u0627\u0631\u0648",
        "\u0623\u0644\u0641\u0627\u0638",
        "\u0623\u0644\u0641\u0628\u0627\u0626\u0649",
        "\u0623\u0644\u0641\u0628\u0627\u0626\u064A",
        "\u0623\u0644\u0641\u0629",
        "\u0623\u0644\u0641\u0631\u062F",
        "\u0623\u0644\u0641\u0631\u064A\u062F",
        "\u0623\u0644\u0641\u0647",
        "\u0623\u0644\u0641\u0648\u0646\u0633\u0648",
        "\u0623\u0644\u0641\u0649",
        "\u0623\u0644\u0641\u064A",
        "\u0623\u0644\u0641\u064A\u0629",
        "\u0623\u0644\u0641\u064A\u0633",
        "\u0623\u0644\u0641\u064A\u0647",
        "\u0623\u0644\u0642\u0627\u0628",
        "\u0623\u0644\u0642\u0649",
        "\u0623\u0644\u0643\u062A\u0631\u0648\u062F",
        "\u0623\u0644\u0643\u062A\u0631\u0648\u0646",
        "\u0623\u0644\u0643\u062A\u0631\u0648\u0646\u064A\u0627\u062A",
        "\u0623\u0644\u0643\u0633\u0627\u0646",
        "\u0623\u0644\u0643\u0633\u0646\u062F\u0631",
        "\u0623\u0644\u0643\u0633\u0646\u062F\u0631\u0627",
        "\u0623\u0644\u0643\u0633\u0649",
        "\u0623\u0644\u0643\u0633\u064A",
        "\u0623\u0644\u0643\u0633\u064A\u0633",
        "\u0623\u0644\u0643\u0639",
        "\u0623\u0644\u0643\u0646\u062A\u0631\u0627",
        "\u0623\u0644\u0643\u064A\u0627",
        "\u0623\u0644\u0644\u0622\u062A",
        "\u0623\u0644\u0644\u0627\u062A",
        "\u0623\u0644\u0644\u0646\u0628\u0649",
        "\u0623\u0644\u0644\u0646\u0628\u064A",
        "\u0623\u0644\u0645",
        "\u0623\u0644\u0645\u0627\u0633",
        "\u0623\u0644\u0645\u0627\u0646",
        "\u0623\u0644\u0645\u0627\u0646\u064A\u0627",
        "\u0623\u0644\u0645\u0639\u0649",
        "\u0623\u0644\u0645\u0639\u064A",
        "\u0623\u0644\u0645\u0639\u064A\u0629",
        "\u0623\u0644\u0645\u0639\u064A\u0647",
        "\u0623\u0644\u0645\u0646\u064A\u0648\u0645",
        "\u0623\u0644\u0645\u0648\u0646\u064A\u0648\u0645",
        "\u0623\u0644\u0646",
        "\u0623\u0644\u0646\u0628\u0649",
        "\u0623\u0644\u0646\u0628\u064A",
        "\u0623\u0644\u0647\u0628",
        "\u0623\u0644\u0647\u0645",
        "\u0623\u0644\u0648",
        "\u0623\u0644\u0648\u0627\u062D",
        "\u0623\u0644\u0648\u0627\u062D\u0649",
        "\u0623\u0644\u0648\u0627\u062D\u064A",
        "\u0623\u0644\u0648\u0627\u0646",
        "\u0623\u0644\u0648\u0633\u0649",
        "\u0623\u0644\u0648\u0633\u064A",
        "\u0623\u0644\u0648\u0641",
        "\u0623\u0644\u0648\u0647\u064A\u0629",
        "\u0623\u0644\u0648\u0647\u064A\u0647",
        "\u0623\u0644\u0648\u064A\u0629",
        "\u0623\u0644\u0648\u064A\u0633",
        "\u0623\u0644\u0648\u064A\u0647",
        "\u0623\u0644\u064A\u0627\u0641",
        "\u0623\u0644\u064A\u0633",
        "\u0623\u0644\u064A\u0633\u0627",
        "\u0623\u0644\u064A\u0633\u0648\u0646",
        "\u0623\u0644\u064A\u0634\u0627\u0646",
        "\u0623\u0644\u064A\u0641",
        "\u0623\u0644\u064A\u0645",
        "\u0623\u0644\u064A\u0646\u0648\u0649",
        "\u0623\u0644\u064A\u0646\u0648\u064A",
        "\u0625\u0644",
        "\u0625\u0644\u0627",
        "\u0625\u0644\u0629",
        "\u0625\u0644\u062A\u0623\u0645",
        "\u0625\u0644\u062A\u0626\u0627\u0645",
        "\u0625\u0644\u062A\u0628\u0627\u0633",
        "\u0625\u0644\u062A\u0628\u0633",
        "\u0625\u0644\u062A\u062C\u0623",
        "\u0625\u0644\u062A\u062C\u0627",
        "\u0625\u0644\u062A\u062C\u0627\u0621",
        "\u0625\u0644\u062A\u062D\u0627\u0641",
        "\u0625\u0644\u062A\u062D\u0627\u0645",
        "\u0625\u0644\u062A\u062D\u0641",
        "\u0625\u0644\u062A\u062D\u0642",
        "\u0625\u0644\u062A\u062D\u0645",
        "\u0625\u0644\u062A\u0632\u0627\u0645",
        "\u0625\u0644\u062A\u0635\u0627\u0642",
        "\u0625\u0644\u062A\u0635\u0642",
        "\u0625\u0644\u062A\u0641",
        "\u0625\u0644\u062A\u0641\u0627\u0641",
        "\u0625\u0644\u062A\u0642\u0627\u0621",
        "\u0625\u0644\u062A\u0642\u0627\u0637",
        "\u0625\u0644\u062A\u0642\u0637",
        "\u0625\u0644\u062A\u0642\u0649",
        "\u0625\u0644\u062A\u0642\u064A",
        "\u0625\u0644\u062A\u0645\u0627\u0633",
        "\u0625\u0644\u062A\u0645\u0633",
        "\u0625\u0644\u062A\u0647\u0627\u0621",
        "\u0625\u0644\u062A\u0647\u0627\u0628",
        "\u0625\u0644\u062A\u0647\u0628",
        "\u0625\u0644\u062A\u0648\u0627\u0621",
        "\u0625\u0644\u062C\u0627\u0645",
        "\u0625\u0644\u062D\u0627\u062D",
        "\u0625\u0644\u062D\u0627\u062F",
        "\u0625\u0644\u062D\u0627\u062F\u064A\u0629",
        "\u0625\u0644\u062D\u0627\u062F\u064A\u0647",
        "\u0625\u0644\u062D\u0627\u0642",
        "\u0625\u0644\u0632\u0627\u0645",
        "\u0625\u0644\u0635\u0627\u0642",
        "\u0625\u0644\u063A\u0627\u0621",
        "\u0625\u0644\u0642\u0627\u0621",
        "\u0625\u0644\u0645\u0627\u0645",
        "\u0625\u0644\u0647",
        "\u0625\u0644\u0647\u0627\u0645",
        "\u0625\u0644\u0647\u0649",
        "\u0625\u0644\u0647\u064A",
        "\u0625\u0644\u0647\u064A\u0627\u062A",
        "\u0625\u0644\u0647\u064A\u0629",
        "\u0625\u0644\u0647\u064A\u0647",
        "\u0625\u0644\u0649",
        "\u0625\u0644\u064A\u0627\u0630\u0629",
        "\u0625\u0644\u064A\u0627\u0630\u0647",
        "\u0625\u0644\u064A\u0627\u0633",
        "\u0625\u0644\u064A\u0646\u0648\u0649",
        "\u0625\u0644\u064A\u0646\u0648\u064A",
        "\u0627\u0644",
        "\u0627\u0644\u0622\u0621",
        "\u0627\u0644\u0622\u0644\u0627\u062A\u064A",
        "\u0627\u0644\u0622\u0644\u0627\u062C\u064A",
        "\u0627\u0644\u0622\u0647\u064A\u0627\u062A",
        "\u0627\u0644\u0623\u0628\u0627\u0645\u0627",
        "\u0627\u0644\u0623\u0644\u0628\u064A\u0631\u0649",
        "\u0627\u0644\u0623\u0644\u0628\u064A\u0631\u064A",
        "\u0627\u0644\u0623\u0644\u062A\u064A\u0643",
        "\u0627\u0644\u0623\u0644\u0632\u0627\u0633",
        "\u0627\u0644\u0623\u0644\u0645\u0627\u0646\u064A\u062A",
        "\u0627\u0644\u0623\u0644\u064A\u0627\u0646\u0629",
        "\u0627\u0644\u0623\u0644\u064A\u0627\u0646\u0647",
        "\u0627\u0644\u0623\u0646",
        "\u0627\u0644\u0627",
        "\u0627\u0644\u0627\u0621",
        "\u0627\u0644\u0627\u0628\u0627\u0645\u0627",
        "\u0627\u0644\u0627\u062A",
        "\u0627\u0644\u0627\u0633\u0643\u0627",
        "\u0627\u0644\u0627\u0639\u064A\u0628",
        "\u0627\u0644\u0627\u0641",
        "\u0627\u0644\u0627\u0644\u0627\u062A\u0649",
        "\u0627\u0644\u0627\u0644\u0627\u062C\u064A",
        "\u0627\u0644\u0627\u0644\u0628\u064A\u0631\u0649",
        "\u0627\u0644\u0627\u0644\u0628\u064A\u0631\u064A",
        "\u0627\u0644\u0627\u0644\u062A\u064A\u0643",
        "\u0627\u0644\u0627\u0644\u0632\u0627\u0633",
        "\u0627\u0644\u0627\u0644\u0645\u0627\u0646\u064A\u062A",
        "\u0627\u0644\u0627\u0644\u064A\u0627\u0646\u0629",
        "\u0627\u0644\u0627\u0644\u064A\u0627\u0646\u0647",
        "\u0627\u0644\u0627\u0646",
        "\u0627\u0644\u0627\u0647\u064A\u0627\u062A",
        "\u0627\u0644\u0628\u0627\u0646\u064A\u0627",
        "\u0627\u0644\u0628\u0631\u062A",
        "\u0627\u0644\u0628\u0631\u062A\u0627",
        "\u0627\u0644\u0628\u0631\u062A\u0648",
        "\u0627\u0644\u0628\u0631\u062A\u0648\u0633",
        "\u0627\u0644\u0628\u0633\u0629",
        "\u0627\u0644\u0628\u0633\u0647",
        "\u0627\u0644\u0629",
        "\u0627\u0644\u062A\u0623\u0645",
        "\u0627\u0644\u062A\u0626\u0627\u0645",
        "\u0627\u0644\u062A\u0628\u0627\u0633",
        "\u0627\u0644\u062A\u0628\u0633",
        "\u0627\u0644\u062A\u062C\u0623",
        "\u0627\u0644\u062A\u062C\u0627",
        "\u0627\u0644\u062A\u062C\u0627\u0621",
        "\u0627\u0644\u062A\u062D\u0627\u0641",
        "\u0627\u0644\u062A\u062D\u0627\u0645",
        "\u0627\u0644\u062A\u062D\u0642",
        "\u0627\u0644\u062A\u062D\u0645",
        "\u0627\u0644\u062A\u0631\u0627\u0641\u064A\u0634",
        "\u0627\u0644\u062A\u0631\u0627\u0645\u064A\u0643\u0631\u0648\u0633\u0643\u0648\u0628",
        "\u0627\u0644\u062A\u0632\u0627\u0645",
        "\u0627\u0644\u062A\u0635\u0627\u0642",
        "\u0627\u0644\u062A\u0635\u0642",
        "\u0627\u0644\u062A\u0641",
        "\u0627\u0644\u062A\u0641\u0627\u0641",
        "\u0627\u0644\u062A\u0642\u0627\u0621",
        "\u0627\u0644\u062A\u0642\u0627\u0637",
        "\u0627\u0644\u062A\u0642\u0637",
        "\u0627\u0644\u062A\u0645\u0633",
        "\u0627\u0644\u062A\u0647\u0627\u0621",
        "\u0627\u0644\u062A\u0647\u0627\u0628",
        "\u0627\u0644\u062A\u0647\u0628",
        "\u0627\u0644\u062A\u0648\u0627\u0621",
        "\u0627\u0644\u062A\u0648\u0646",
        "\u0627\u0644\u062A\u0649",
        "\u0627\u0644\u062A\u064A",
        "\u0627\u0644\u062D",
        "\u0627\u0644\u062D\u0627\u062D",
        "\u0627\u0644\u062D\u0627\u0646",
        "\u0627\u0644\u062E",
        "\u0627\u0644\u062F",
        "\u0627\u0644\u062F\u0631\u0633\u0648\u0646",
        "\u0627\u0644\u0630\u0649",
        "\u0627\u0644\u0630\u064A",
        "\u0627\u0644\u0632\u0627\u0645",
        "\u0627\u0644\u0635\u0627\u0642",
        "\u0627\u0644\u0635\u0642",
        "\u0627\u0644\u0637\u0648",
        "\u0627\u0644\u0638\u0646\u0628\u063A\u0627\u0621",
        "\u0627\u0644\u0639\u0627\u0628",
        "\u0627\u0644\u0639\u0648\u0628\u0629",
        "\u0627\u0644\u0639\u0648\u0628\u0647",
        "\u0627\u0644\u063A\u0627\u0621",
        "\u0627\u0644\u063A\u0627\u0645",
        "\u0627\u0644\u0641",
        "\u0627\u0644\u0641\u0627",
        "\u0627\u0644\u0641\u0627\u0631\u0648",
        "\u0627\u0644\u0641\u0627\u0638",
        "\u0627\u0644\u0641\u0628\u0627\u0626\u0649",
        "\u0627\u0644\u0641\u0628\u0627\u0626\u064A",
        "\u0627\u0644\u0641\u0629",
        "\u0627\u0644\u0641\u0647",
        "\u0627\u0644\u0641\u0648\u0646\u0633\u0648",
        "\u0627\u0644\u0641\u064A\u0629",
        "\u0627\u0644\u0641\u064A\u0633",
        "\u0627\u0644\u0641\u064A\u0647",
        "\u0627\u0644\u0642\u0627\u0621",
        "\u0627\u0644\u0642\u0627\u0628",
        "\u0627\u0644\u0643\u062A\u0631\u0648\u062F",
        "\u0627\u0644\u0643\u062A\u0631\u0648\u0646",
        "\u0627\u0644\u0643\u062A\u0631\u0648\u0646\u064A\u0627\u062A",
        "\u0627\u0644\u0643\u0633\u0627\u0646",
        "\u0627\u0644\u0643\u0633\u0646\u062F\u0631",
        "\u0627\u0644\u0643\u0633\u0646\u062F\u0631\u0627",
        "\u0627\u0644\u0643\u0633\u0649",
        "\u0627\u0644\u0643\u0633\u064A",
        "\u0627\u0644\u0643\u0633\u064A\u0633",
        "\u0627\u0644\u0643\u0639",
        "\u0627\u0644\u0643\u0646\u062A\u0631\u0627",
        "\u0627\u0644\u0643\u064A\u0627",
        "\u0627\u0644\u0644\u0622\u062A",
        "\u0627\u0644\u0644\u0627\u062A",
        "\u0627\u0644\u0644\u0646\u0628\u0649",
        "\u0627\u0644\u0644\u0646\u0628\u064A",
        "\u0627\u0644\u0644\u0647", "\u0627\u0644\u0645",
        "\u0627\u0644\u0645\u0627\u0645",
        "\u0627\u0644\u0645\u0627\u0646",
        "\u0627\u0644\u0645\u0627\u0646\u064A\u0627",
        "\u0627\u0644\u0645\u0639\u0649",
        "\u0627\u0644\u0645\u0639\u064A",
        "\u0627\u0644\u0645\u0646\u064A\u0648\u0645",
        "\u0627\u0644\u0645\u0648\u0646\u064A\u0648\u0645",
        "\u0627\u0644\u0646", "\u0627\u0644\u0647",
        "\u0627\u0644\u0647\u0628",
        "\u0627\u0644\u0647\u0629",
        "\u0627\u0644\u0647\u0647",
        "\u0627\u0644\u0647\u0649",
        "\u0627\u0644\u0647\u064A",
        "\u0627\u0644\u0647\u064A\u0627\u062A",
        "\u0627\u0644\u0647\u064A\u0629",
        "\u0627\u0644\u0647\u064A\u0647",
        "\u0627\u0644\u0648",
        "\u0627\u0644\u0648\u0627\u062D",
        "\u0627\u0644\u0648\u0627\u062D\u0649",
        "\u0627\u0644\u0648\u0627\u062D\u064A",
        "\u0627\u0644\u0648\u0627\u0646",
        "\u0627\u0644\u0648\u0633\u0649",
        "\u0627\u0644\u0648\u0633\u064A",
        "\u0627\u0644\u0648\u0641",
        "\u0627\u0644\u0648\u0647\u064A\u0629",
        "\u0627\u0644\u0648\u0647\u064A\u0647",
        "\u0627\u0644\u0648\u064A\u0629",
        "\u0627\u0644\u0648\u064A\u0633",
        "\u0627\u0644\u0648\u064A\u0647",
        "\u0627\u0644\u0649",
        "\u0627\u0644\u064A\u0627\u0630\u0629",
        "\u0627\u0644\u064A\u0627\u0630\u0647",
        "\u0627\u0644\u064A\u0627\u0641",
        "\u0627\u0644\u064A\u0629",
        "\u0627\u0644\u064A\u0633",
        "\u0627\u0644\u064A\u0633\u0627",
        "\u0627\u0644\u064A\u0633\u0648\u0646",
        "\u0627\u0644\u064A\u0634\u0627\u0646",
        "\u0627\u0644\u064A\u0641",
        "\u0627\u0644\u064A\u0646\u0648\u0649",
        "\u0627\u0644\u064A\u0646\u0648\u064A"));
  private SortformUtils() {
  }

  public static SortformUtils get() {
    if (instance == null) {
      synchronized (SortformUtils.class) {
        instance = new SortformUtils();
      }
    }
    return instance;
  }

  public String defaultSortform(String stringText) {
    String result = new StringText(stringText).toDisplayString().toUpperCase();
    result = stripAccents(result);
    result = deleteAlfalam(result);
    result = transformALA(result);
    result = stripPunctuation(result);
    result = stripMultipleBlanks(result);
    return result;
  }

  public String stripMultipleBlanks(String s) {
    return s.replaceAll("\\s+", " ");
  }

  private String transformALA(String s) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\u0110': /* Latin capital letter D with stroke */
        case '\u00D0': /* Latin capital letter eth */
        case '\u0189': /* Latin capital letter african D */
          result.append('\u0044');
          break;
        case '\u00D8': /* Latin capital letter O with stroke */
          result.append('\u004F');
          break;
        case '\u0141': /* Latin capital letter L with stroke */
          result.append('\u004C');
          break;
        case '\u0142': /* Latin small letter l with stroke */
          result.append('\u006C');
          break;
        case '\u0111': /* Latin small letter d with stroke */
        case '\u00F0': /* Latin small letter eth */
          result.append('\u0064');
          break;
        case '\u00F8': /* Latin small letter o with stroke */
          result.append('\u006F');
          break;
        case '\u0629': /* Arabic letter teh marbute */
          result.append('\u0647'); /* arabic letter heh */
          break;
        case '\u0649': /* Arabic letter alef maksura */
          result.append('\u064A'); /* arabic letter yeh */
          break;
        case '\u0621': /* Arabic letter hamza */
          result.append('\u0627'); /* arabic letter alef */
          break;
        case '\u00C6': /* Latin capital letter AE */
          result.append("\u0041\u0045");
          break;
        case '\u00E6': /* Latin small letter ae */
          result.append("\u0061\u0065");
          break;
        case '\u0152': /* Latin capital ligature OE */
          result.append("\u004F\u0045");
          break;
        case '\u0153': /* Latin small ligature oe */
          result.append("\u006F\u0065");
          break;
        case '\u002D':
        case '\u002F':
          result.append(" ");
          break;
        default:
          result.append(c);
          break;
      }
    }
    return result.toString();
  }

  
  private String stripPunctuation(String s) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isLetterOrDigit(c) || Character.isSpaceChar(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  public String replacePunctuationMark1(String input, String replacementString) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (punctuationMark1List.contains(c)) {
        result.append(replacementString);
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public String replacePunctuationMark2(String input, String replacementString) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (punctuationMark2List.contains(c)) {
        result.append(replacementString);
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public String replaceDeweyPunctuation(String input, String replacementString) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (deweyPunctuationMarkList.contains(c)) {
        result.append(replacementString);
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public String stripAccents(String s) {

    String normalized = Normalizer.normalize(
      s, java.text.Normalizer.Form.NFKD);
    return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", EMPTY_STRING);
  }

  public String deleteAlfalam(String s) {

    boolean haveArabic = false;
    for (int i = 0; i < s.length(); i++) {
      if (Character.UnicodeBlock.of(s.charAt(i)) == Character.UnicodeBlock.ARABIC) {
        haveArabic = true;
        break;
      }
    }
    if (haveArabic) {
      StringBuilder result = new StringBuilder();
      StringBuilder word = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (Character.isLetterOrDigit(c)) {
          word.append(c);
        } else {
          if (word.length() > 0) {
            if (word.toString().startsWith("\u0644\u0622")
              || "\u0623\u0625\0627".contains(word
              .subSequence(0, 1))) {
              if (!arabicWordExceptions.contains(word.toString())) {
                result.append(word.substring(2));
              } else {
                result.append(word.toString());
              }
              word = new StringBuilder();
            }
          }
          result.append(c);
        }
      }
      return result.toString();
    }  // no arabic
    return s;
  }

  public StringText stripSkipInFiling(String stringText, short skipInFiling) {
    StringText st = new StringText(stringText);
    if (skipInFiling > 0) {
      Subfield s = st.getSubfield(0);
      s.setContent(s.getContent().substring(skipInFiling));
      st.setSubfield(0, s);
    }
    return st;
  }
}
