package org.folio.marccat.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.lang.StringUtils;
import org.folio.marccat.business.cataloguing.bibliographic.*;
import org.folio.marccat.business.cataloguing.bibliographic.FixedField;
import org.folio.marccat.business.cataloguing.bibliographic.VariableField;
import org.folio.marccat.business.cataloguing.common.*;
import org.folio.marccat.business.codetable.Avp;
import org.folio.marccat.business.common.View;
import org.folio.marccat.business.descriptor.DescriptorFactory;
import org.folio.marccat.config.constants.Global;
import org.folio.marccat.config.log.Log;
import org.folio.marccat.config.log.Message;
import org.folio.marccat.dao.*;
import org.folio.marccat.dao.persistence.*;
import org.folio.marccat.enumaration.CodeListsType;
import org.folio.marccat.exception.*;
import org.folio.marccat.integration.record.BibliographicInputFile;
import org.folio.marccat.integration.record.RecordParser;
import org.folio.marccat.integration.search.Parser;
import org.folio.marccat.model.Subfield;
import org.folio.marccat.resources.domain.*;
import org.folio.marccat.resources.domain.Leader;
import org.folio.marccat.search.SearchResponse;
import org.folio.marccat.shared.CorrelationValues;
import org.folio.marccat.shared.GeneralInformation;
import org.folio.marccat.shared.MapHeading;
import org.folio.marccat.shared.Validation;
import org.folio.marccat.util.F;
import org.folio.marccat.util.StringText;
import org.springframework.web.multipart.MultipartFile;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import static java.lang.String.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.folio.marccat.config.constants.Global.BIBLIOGRAPHIC_INDICATOR_NOT_NUMERIC;
import static org.folio.marccat.config.constants.Global.EMPTY_STRING;
import static org.folio.marccat.config.constants.Global.EMPTY_VALUE;
import static org.folio.marccat.util.F.isNotNullOrEmpty;
import static org.folio.marccat.util.F.locale;


/**
 * Storage layer service.
 * This is the interface towards our storage service. Any R/W access to the persistence layer needs to pass through
 * this interface.
 *
 * @author cchiama
 * @author carment
 * @author nbianchini
 * @since 1.0
 */
public class StorageService implements Closeable {

  public static final String HDG_MAIN_LIBRARY_NUMBER = " and hdg.mainLibraryNumber = ";
  private static final Log logger = new Log(StorageService.class);
  private static final Map<Integer, Class> FIRST_CORRELATION_HEADING_CLASS_MAP = new HashMap<>();
  private static final Map<Integer, Class> SECOND_CORRELATION_CLASS_MAP = new HashMap<>();
  private static final Map<Integer, Class> THIRD_CORRELATION_HEADING_CLASS_MAP = new HashMap<>();

  static {
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(1, T_BIB_HDR.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(2, NameType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(17, NameType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(3, TitleFunction.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(22, TitleFunction.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(4, SubjectType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(18, SubjectType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(5, ControlNumberType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(19, ControlNumberType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(6, ClassificationType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(20, ClassificationType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(7, BibliographicNoteType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(7, PublisherManager.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(8, BibliographicRelationType.class);
    FIRST_CORRELATION_HEADING_CLASS_MAP.put(11, T_NME_TTL_FNCTN.class);
  }

  static {
    SECOND_CORRELATION_CLASS_MAP.put(2, NameSubType.class);
    SECOND_CORRELATION_CLASS_MAP.put(3, TitleSecondaryFunction.class);
    SECOND_CORRELATION_CLASS_MAP.put(4, SubjectFunction.class);
    SECOND_CORRELATION_CLASS_MAP.put(5, ControlNumberFunction.class);
    SECOND_CORRELATION_CLASS_MAP.put(6, ClassificationFunction.class);
    SECOND_CORRELATION_CLASS_MAP.put(11, NameType.class);
  }

  static {
    THIRD_CORRELATION_HEADING_CLASS_MAP.put(2, NameFunction.class);
    THIRD_CORRELATION_HEADING_CLASS_MAP.put(4, SubjectSource.class);
    THIRD_CORRELATION_HEADING_CLASS_MAP.put(11, NameSubType.class);
  }

  private  Session session;

  public void setSession(Session session) {
    this.session = session;
  }

  public Session getSession() {
    return session;
  }


  /**
   * Returns the skip in filing associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the skip in filing associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getSkipInFiling(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_SKP_IN_FLNG_CNT.class, locale(lang));
  }


  @Override
  public void close() throws IOException {
    try {
      session.close();
    } catch (final HibernateException exception) {
      throw new IOException(exception);
    }
  }


  /**
   * Returns the preferred view associated with the input data.
   *
   * @param itemNumber              the record identifier.
   * @param databasePreferenceOrder the database preference order (for choosing among views).
   * @return the preferred view associated with the input data.
   * @throws DataAccessException in case of data access failure.
   */
  public int getPreferredView(final int itemNumber, final int databasePreferenceOrder) throws DataAccessException {
    return new CacheDAO().getPreferredView(session, itemNumber, databasePreferenceOrder);
  }

  /**
   * Sorts a given {@link SearchResponse} instance.
   * The sort actually happens at docids level, if some record has been already fetched in the input response, it will
   * be removed.
   *
   * @param rs         the search response.
   * @param attributes the sort attributes.
   * @param directions the sort orders.
   * @return a search response wrapping a docid array ordered according with the given criteria.
   * @throws DataAccessException in case of data access failure.
   */
  public SearchResponse sortResults(final SearchResponse rs, final String[] attributes, final String[] directions) throws DataAccessException {
    new SortResultSetsDAO().sort(session, rs, attributes, directions);
    rs.clearRecords();
    return rs;
  }

  /**
   * Returns the content of a record associated with the given data.
   *
   * @param itemNumber    the record identifier.
   * @param searchingView the view.
   * @return the content of a record associated with the given data.
   * @throws RecordNotFoundException in case nothing is found.
   */
  public String getRecordData(final int itemNumber, final int searchingView) throws RecordNotFoundException {
    final FULL_CACHE cache = new FullCacheDAO().load(session, itemNumber, searchingView);
    return cache.getRecordData();
  }

  /**
   * Find the {@link CatalogItem} associated with the given data.
   *
   * @param itemNumber    the record identifier.
   * @param searchingView the search view.
   * @return the {@link CatalogItem} associated with the given data.
   */
  public CatalogItem getCatalogItemByKey(final int itemNumber, final int searchingView) {
      return new BibliographicCatalogDAO().getCatalogItemByKey(session, itemNumber, searchingView);
  }

  /**
   * Returns a list of {@link Avp} which represents a short version of the available bibliographic templates.
   *
   * @return a list of {@link Avp} which represents a short version of the available bibliographic templates.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<Integer>> getBibliographicRecordTemplates() throws DataAccessException {
    final BibliographicModelDAO dao = new BibliographicModelDAO();
    try {
      return dao.getBibliographicModelList(session);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }


  /**
   * Deletes a Bibliographic record template.
   *
   * @param id the record template id.
   * @throws DataAccessException in case of data access failure.
   */
  public void deleteBibliographicRecordTemplate(final String id) throws DataAccessException {
    try {
      final BibliographicModelDAO dao = new BibliographicModelDAO();
      final Model model = dao.load(Integer.valueOf(id), session);
      dao.delete(model, session);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }


  /**
   * Update the Bibliographic Record Template.
   *
   * @param template the record template.
   * @throws DataAccessException in case of data access failure.
   */
  public void updateBibliographicRecordTemplate(final RecordTemplate template) throws DataAccessException {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      final BibliographicModelDAO dao = new BibliographicModelDAO();
      final BibliographicModel model = new BibliographicModel();
      model.setId(template.getId());
      model.setLabel(template.getName());
      model.setFrbrFirstGroup(template.getGroup());
      model.setRecordFields(mapper.writeValueAsString(template));
      dao.update(model, session);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final JsonProcessingException exception) {
      logger.error(Message.MOD_MARCCAT_00013_IO_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }



  /**
   * Save the new Bibliographic Record Template.
   *
   * @param template the record template.
   * @throws DataAccessException in case of data access failure.
   */
  public void saveBibliographicRecordTemplate(final RecordTemplate template) throws DataAccessException {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      final BibliographicModelDAO dao = new BibliographicModelDAO();
      final BibliographicModel model = new BibliographicModel();
      model.setLabel(template.getName());
      model.setFrbrFirstGroup(template.getGroup());
      model.setRecordFields(mapper.writeValueAsString(template));
      dao.save(model, session);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final JsonProcessingException exception) {
      logger.error(Message.MOD_MARCCAT_00013_IO_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Return a Bibliographic Record Template by id.
   *
   * @param id the record template id.
   * @return the bibliographic record template associated with the given id.
   * @throws DataAccessException in case of data access failure.
   */
  public RecordTemplate getBibliographicRecordRecordTemplatesById(final Integer id) throws DataAccessException {
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(
        new BibliographicModelDAO().load(id, session).getRecordFields(),
        RecordTemplate.class);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final IOException exception) {
      logger.error(Message.MOD_MARCCAT_00013_IO_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Load records from files uploaded.
   *
   * @param file            -- the current file.
   * @param startRecord     -- the number start record.
   * @param numberOfRecords -- the number of records to load.
   * @param view            -- the cataloguing view associated.
   * @return map with loading result.
   */
  public Map<String, Object> loadRecords(final MultipartFile file, final int startRecord, final int numberOfRecords,
                                         final int view, final Map<String, String> configuration) {
    final Map<String, Object> result = new HashMap<>();
    List<Integer> ids = new ArrayList<>();
    try {
      if (!file.isEmpty()) {
        final InputStream input = file.getInputStream();
        final BibliographicInputFile bf = new BibliographicInputFile();
        bf.loadFile(input, file.getOriginalFilename(), view, startRecord, numberOfRecords, session, configuration);

        final CodeTableDAO dao = new CodeTableDAO();
        final LDG_STATS stats = dao.getStats(session, bf.getLoadingStatisticsNumber());
        if (stats.getRecordsAdded() > 0) {
          final List<LOADING_MARC_RECORDS> lmr = (dao.getResults(session, bf.getLoadingStatisticsNumber()));
          ids = lmr.stream().map(LOADING_MARC_RECORDS :: getBibItemNumber).collect(Collectors.toList());
        }
        result.put(Global.LOADING_FILE_FILENAME, file.getName());
        result.put(Global.LOADING_FILE_IDS, ids);
        result.put(Global.LOADING_FILE_REJECTED, stats.getRecordsRejected());
        result.put(Global.LOADING_FILE_ADDED, stats.getRecordsAdded());
        result.put(Global.LOADING_FILE_ERRORS, stats.getErrorCount());

      }
    } catch (IOException e) {
      throw new ModMarccatException(e);
    }

    return result;
  }


  /**
   * Generate a new keyNumber for keyFieldCodeValue specified.
   *
   * @param keyCodeValue -- the key code of field value.
   * @return nextNumber
   * @throws DataAccessException in case of data access exception.
   */
  public Integer generateNewKey(final String keyCodeValue) throws DataAccessException {
    try {
      SystemNextNumberDAO dao = new SystemNextNumberDAO();
      return dao.getNextNumber(keyCodeValue, session);
    } catch (HibernateException e) {
      throw new DataAccessException(e);
    }
  }

  /**
   * Updates the full record cache table with the given item.
   *
   * @param item the catalog item.
   * @param view the related view.
   */
  public void updateFullRecordCacheTable(final CatalogItem item, final int view) {
      try {
        new BibliographicCatalogDAO().updateFullRecordCacheTable(session, item);
      } catch (final HibernateException exception) {
        throw new DataAccessException(exception);
      }

  }

  /**
   * Executes a CCL query using the given data.
   *
   * @param cclQuery      the CCL query.
   * @param mainLibraryId the main library identifier.
   * @param firstRecord   the first record.
   * @param lastRecord    the last record.
   * @param locale        the current locale.
   * @param searchingView the target search view.
   * @return a list of docid matching the input query.
   */
  public List<Integer> executeQuery(final String cclQuery, final int mainLibraryId, final Locale locale, final int searchingView, final int firstRecord, final int lastRecord, final String[] attributes, String[] directions) {
    final Parser parser = new Parser(locale, mainLibraryId, searchingView, session);
    try (final Statement sql = stmt(connection());
         final ResultSet rs = executeQuery(sql, parser.parse(cclQuery, firstRecord, lastRecord, attributes, directions))) {
      final ArrayList<Integer> results = new ArrayList<>();
      while (rs.next()) {
        results.add(rs.getInt(1));
      }

      logger.info(Message.MOD_MARCCAT_00023_SE_REQRES, cclQuery, results.size());

      return results;
    } catch (final HibernateException | SQLException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      return emptyList();
    }
  }


  /**
   * Returns a valid database connection associated with this service.
   *
   * @return a valid database connection associated with this service.
   * @throws HibernateException in case of data access failure.
   */
  public Connection connection() throws HibernateException {
    return session.connection();
  }


  /**
   * Creates a valid statement from the given connection.
   *
   * @param connection the database connection.
   * @return a valid statement from the given connection.
   */
  private Statement stmt(final Connection connection) {
    try {
      return connection.createStatement();
    } catch (final Exception exception) {
      throw new ModMarccatException(exception);
    }
  }

  /**
   * Internal method fo executing a SQL query.
   *
   * @param stmt  the statement.
   * @param query the SQL command.
   * @return the result of the query execution.
   */
  private ResultSet executeQuery(final Statement stmt, final String query) {
    try {
      return stmt.executeQuery(query);
    } catch (final Exception exception) {
      throw new ModMarccatException(exception);
    }
  }


  /**
   * returns the number of bibliographic records linked to an authority record
   *
   * @param id   the authority number, used here as a filter criterion.
   * @param view the view used here as filter criterion
   * @return the count of bibliographic records
   * @throws HibernateException
   */
  public CountDocument getCountDocumentByAutNumber(final int id, final int view) throws HibernateException {
    final CountDocument countDocument = new CountDocument();
    final AutDAO dao = new AutDAO();
    try{
    final AUT aut = dao.load(session, id);
    final Class accessPoint = Global.BIBLIOGRAPHIC_ACCESS_POINT_CLASS_MAP.get(aut.getHeadingType());
    countDocument.setCountDocuments(dao.getDocCountByAutNumber(aut.getHeadingNumber(), accessPoint, view, session));
    countDocument.setQuery(Global.INDEX_AUTHORITY_TYPE_MAP.get(aut.getHeadingType()) + " " + aut.getHeadingNumber());
    } catch (final RecordNotFoundException | HibernateException exception) {
      countDocument.setCountDocuments(0);
      countDocument.setQuery("");
    }
    return countDocument;
  }

  /**
   * Return a list of headings for a specific a search query in the first browse
   *
   * @param query       the query used here as filter criterion
   * @param view        the view used here as filter criterion
   * @param mainLibrary the main library used here as filter criterion
   * @param pageSize    the page size used here as filter criterion
   * @param lang        the lang used here as filter criterion
   * @return a list of headings
   * @throws DataAccessException
   * @throws InvalidBrowseIndexException
   */
  public List<MapHeading> getFirstPage(final String query, final int view, final int mainLibrary, final int pageSize, final String lang) throws DataAccessException, InvalidBrowseIndexException {
    String key = null;
    try {
      String index = null;
      String browseTerm = null;
      final List<Descriptor> descriptorsList;
      final IndexListDAO daoIndex = new IndexListDAO();
      if (query != null) {
        index = query.substring(0, query.indexOf((" ")));
        index = F.fixedCharPadding(index, 9).toUpperCase();
        browseTerm = query.substring(query.indexOf((" "))).trim();
      }
      key = daoIndex.getIndexByAbreviation(index, session, locale(lang));
      final Class c = Global.DAO_CLASS_MAP.get(key);
      if (c == null) {
        logger.error(Message.MOD_MARCCAT_00119_DAO_CLASS_MAP_NOT_FOUND, key);
        return Collections.emptyList();
      }
      final DescriptorDAO dao = (DescriptorDAO) c.newInstance();
      String filter = Global.FILTER_MAP.get(key);
      if (dao instanceof ShelfListDAO) {
        filter += HDG_MAIN_LIBRARY_NUMBER + mainLibrary;
      }
      Descriptor descriptor = ((Descriptor)dao.getPersistentClass().newInstance());
      descriptor.setStringText(new StringBuilder().append(Global.SUBFIELD_DELIMITER).append("a").append(browseTerm).toString());
      descriptor.calculateAndSetSortForm();
      browseTerm = descriptor.getSortForm();

      descriptorsList = dao.getHeadingsBySortform("<", "desc", browseTerm, filter, view, 1, session);
      if (!(dao instanceof PublisherDescriptorDAO) && !descriptorsList.isEmpty()) {
        browseTerm = dao.getBrowsingSortForm(descriptorsList.get(0));
        descriptorsList.clear();
      }
      descriptorsList.addAll(dao.getHeadingsBySortform(">=", EMPTY_STRING, browseTerm, filter, view, pageSize, session));
      return getMapHeadings(view, descriptorsList, dao);

    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final IllegalAccessException | InstantiationException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new InvalidBrowseIndexException(key);
    }
  }

  /**
   * Return a list of headings for a specific a search query in the next browse
   *
   * @param query       the query used here as filter criterion
   * @param view        the view used here as filter criterion
   * @param mainLibrary the main library used here as filter criterion
   * @param pageSize    the page size used here as filter criterion
   * @param lang        the lang used here as filter criterion
   * @return a list of headings
   * @throws DataAccessException
   * @throws InvalidBrowseIndexException
   */
  public List<MapHeading> getNextPage(final String query, final int view, final int mainLibrary, final int pageSize, final String lang) {
    String key = null;
    try {
      String index = null;
      String browseTerm = null;
      final List<Descriptor> descriptorsList;
      final IndexListDAO daoIndex = new IndexListDAO();
      String operator = ">";
      if (query != null) {
        index = query.substring(0, query.indexOf((" ")));
        index = F.fixedCharPadding(index, 9).toUpperCase();
        browseTerm = query.substring(query.indexOf((" "))).trim();
      }

      key = daoIndex.getIndexByAbreviation(index, session, locale(lang));
      final Class c = Global.DAO_CLASS_MAP.get(key);
      if (c == null) {
        logger.error(Message.MOD_MARCCAT_00119_DAO_CLASS_MAP_NOT_FOUND, key);
        return Collections.emptyList();
      }
      final DescriptorDAO dao = (DescriptorDAO) c.newInstance();
      String filter = Global.FILTER_MAP.get(key);
      if (dao instanceof ShelfListDAO) {
        filter = filter + HDG_MAIN_LIBRARY_NUMBER + mainLibrary;
      }
      Descriptor descriptor = ((Descriptor)dao.getPersistentClass().newInstance());
      descriptor.setStringText(new StringBuilder().append(Global.SUBFIELD_DELIMITER).append("a").append(browseTerm).toString());
      descriptor.calculateAndSetSortForm();
      browseTerm = descriptor.getSortForm();

      if (dao instanceof PublisherDescriptorDAO || dao instanceof NameTitleNameDescriptorDAO)
        operator = ">=";
      descriptorsList = dao.getHeadingsBySortform(operator, EMPTY_STRING, browseTerm, filter, view, pageSize, session);
      return getMapHeadings(view, descriptorsList, dao);


    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final IllegalAccessException | InstantiationException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new InvalidBrowseIndexException(key);
    }
  }

  /**
   * Return a list of headings for a specific a search query in the previous browse
   *
   * @param query       the query used here as filter criterion
   * @param view        the view used here as filter criterion
   * @param mainLibrary the main library used here as filter criterion
   * @param pageSize    the page size used here as filter criterion
   * @param lang        the lang used here as filter criterion
   * @return a list of headings
   * @throws DataAccessException
   * @throws InvalidBrowseIndexException
   */
  public List<MapHeading> getPreviousPage(final String query, final int view, final int mainLibrary, final int pageSize, final String lang) {
    String key = null;
    try {
      String index = null;
      String browseTerm = null;
      final List<Descriptor> descriptorsList;
      final IndexListDAO daoIndex = new IndexListDAO();
      String operator = "<";
      if (query != null) {
        index = query.substring(0, query.indexOf((" ")));
        index = F.fixedCharPadding(index, 9).toUpperCase();
        browseTerm = query.substring(query.indexOf((" ")), query.length()).trim();
      }

      key = daoIndex.getIndexByAbreviation(index, session, locale(lang));
      final Class c = Global.DAO_CLASS_MAP.get(key);
      if (c == null) {
        logger.error(Message.MOD_MARCCAT_00119_DAO_CLASS_MAP_NOT_FOUND, key);
        return Collections.emptyList();
      }
      final DescriptorDAO dao = (DescriptorDAO) c.newInstance();
      String filter = Global.FILTER_MAP.get(key);
      if (dao instanceof ShelfListDAO) {
        filter = filter + HDG_MAIN_LIBRARY_NUMBER + mainLibrary;
      }
      Descriptor descriptor = ((Descriptor)dao.getPersistentClass().newInstance());
      descriptor.setStringText(new StringBuilder().append(Global.SUBFIELD_DELIMITER).append("a").append(browseTerm).toString());
      descriptor.calculateAndSetSortForm();
      browseTerm = descriptor.getSortForm();
      if (dao instanceof PublisherDescriptorDAO || dao instanceof NameTitleNameDescriptorDAO)
        operator = "<=";
      descriptorsList = dao.getHeadingsBySortform(operator, "desc", browseTerm, filter, view, pageSize, session);
      List<MapHeading> mapHeading = getMapHeadings(view, descriptorsList, dao);
      Collections.reverse(mapHeading);
      return mapHeading;

    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    } catch (final IllegalAccessException | InstantiationException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new InvalidBrowseIndexException(key);
    }
  }

  /**
   * Returns the codes list associated with the given language and key.
   *
   * @param lang         the language code, used here as a filter criterion.
   * @param codeListType the code list type key.
   * @return a list of code / description tuples representing the date type associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getCodesList(final String lang, final CodeListsType codeListType) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, Global.MAP_CODE_LISTS.get(codeListType.toString()), locale(lang));
  }

  /**
   * Return a complete heading map with the data of the heding number, the text to display, the authority count,
   * the count of documents, the count of name titles
   *
   * @param view
   * @param descriptorsList
   * @param dao
   * @return a map headings
   */

  private List<MapHeading> getMapHeadings(int view, List<Descriptor> descriptorsList, DescriptorDAO dao) throws DataAccessException {
    return descriptorsList.stream().map(heading -> {
      final MapHeading headingObject = new MapHeading();
      try {
        headingObject.setHeadingNumber(heading.getHeadingNumber());
        headingObject.setStringText(heading.getDisplayText());
        headingObject.setCountAuthorities(heading.getAuthorityCount());
        headingObject.setCountDocuments(dao.getDocCount(heading, view, session));
        headingObject.setCountTitleNameDocuments(dao.getDocCountNT(heading, view, session));
        final List<REF> crossReferences = dao.getCrossReferences(heading, view, session);
        headingObject.setCrossReferences(crossReferences.stream().map(crossReference -> {
          try {
            final Ref ref = new Ref();
            final Descriptor hdg = crossReference.getTargetDAO().load(crossReference.getTarget(), view, session);
            ref.setStringText(new StringText(hdg.getStringText()).toDisplayString());
            ref.setRefType(crossReference.getType());
            return ref;
          } catch (HibernateException e) {
            throw new DataAccessException(e);
          }
        }).collect(Collectors.toList()));

      } catch (HibernateException exception) {
        logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
        throw new DataAccessException(exception);
      }
      return headingObject;
    }).collect(Collectors.toList());
  }


  /**
   * Gets the material type information.
   * Used for 006 field.
   *
   * @param headerCode the header code used here as filter criterion.
   * @param code       the tag number code used here as filter criterion.
   * @return a string representing form of material.
   * @throws DataAccessException in case of data access failure.
   */
  public Map<String, Object> getMaterialTypeInfosByHeaderCode(final int headerCode, final String code) throws DataAccessException {

    final Map<String, Object> mapRecordTypeMaterial = new HashMap<>();
    final RecordTypeMaterialDAO dao = new RecordTypeMaterialDAO();
    try {
      return ofNullable(dao.getDefaultTypeByHeaderCode(session, headerCode, code))
        .map(rtm -> {
          mapRecordTypeMaterial.put(Global.FORM_OF_MATERIAL_LABEL, rtm.getAmicusMaterialTypeCode());
          mapRecordTypeMaterial.put(Global.MATERIAL_TYPE_CODE_LABEL, rtm.getRecordTypeCode());

          return mapRecordTypeMaterial;
        }).orElse(null);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Return a correlation values for a specific tag number.
   *
   * @param category   the category code used here as filter criterion.
   * @param indicator1 the first indicator used here as filter criterion.
   * @param indicator2 the second indicator used here as filter criterion.
   * @param code       the tag number code used here as filter criterion.
   * @return correlation values
   * @throws DataAccessException in case of data access failure.
   */
  public CorrelationValues getCorrelationVariableField(final Integer category,
                                                       final String indicator1,
                                                       final String indicator2,
                                                       final String code) throws DataAccessException {
    final BibliographicCorrelationDAO bibliographicCorrelationDAO = new BibliographicCorrelationDAO();
    try {
      return ofNullable(
        bibliographicCorrelationDAO.getBibliographicCorrelation(
          session, code, indicator1.charAt(0), indicator2.charAt(0), category))
        .map(BibliographicCorrelation::getValues).orElse(null);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Gets Validation for variable field.
   * Validation is related to sub-fields: valid, mandatory and default subfield
   *
   * @param marcCategory the marc category used here as filter criterion.
   * @param code1        the first correlation used here as filter criterion.
   * @param code2        the second correlation used here as filter criterion.
   * @param code3        the third correlation used here as filter criterion.
   * @return Validation object containing subfield list.
   */
  public Validation getSubfieldsByCorrelations(final int marcCategory,
                                               final int code1,
                                               final int code2,
                                               final int code3) throws DataAccessException {
    final BibliographicValidationDAO daoBibliographicValidation = new BibliographicValidationDAO();
    try {
      final CorrelationValues correlationValues = new CorrelationValues(code1, code2, code3);
      return daoBibliographicValidation.load(session, marcCategory, correlationValues);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Returns the record types associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the record type associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getRecordTypes(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_REC_TYP.class, locale(lang));
  }

  /**
   * Returns the encoding levels associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the encoding level associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getEncodingLevels(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_ENCDG_LVL.class, locale(lang));
  }

  /**
   * Gets the Material description information.
   * The values depend on mtrl_dsc and bib_itm data (leader).
   *
   * @param recordTypeCode     the record type code (leader 05) used here as filter criterion.
   * @param bibliographicLevel the bibliographic level (leader 06) used here as filter criterion.
   * @param code               the tag number code used here as filter criterion.
   * @return a map with RecordTypeMaterial info.
   * @throws DataAccessException in case of data access failure.
   */
  public Map<String, Object> getMaterialTypeInfosByLeaderValues(final char recordTypeCode, final char bibliographicLevel, final String code) throws DataAccessException {

    final RecordTypeMaterialDAO dao = new RecordTypeMaterialDAO();

    try {
      final Map<String, Object> mapRecordTypeMaterial = new HashMap<>();
      final RecordTypeMaterial rtm = dao.getMaterialHeaderCode(session, recordTypeCode, bibliographicLevel);

      mapRecordTypeMaterial.put(Global.HEADER_TYPE_LABEL, (code.equals(Global.MATERIAL_TAG_CODE) ? rtm.getBibHeader008() : rtm.getBibHeader006()));
      mapRecordTypeMaterial.put(Global.FORM_OF_MATERIAL_LABEL, rtm.getAmicusMaterialTypeCode());
      return mapRecordTypeMaterial;
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }


  /**
   * Gets the Material description information.
   *
   * @param recordTypeCode     the record type code (Tag 006//00) used here as filter criterion.
   * @return a map with RecordTypeMaterial info.
   * @throws DataAccessException in case of data access failure.
   */
  public Map<String, Object> getHeaderTypeByRecordTypeCode(final char recordTypeCode) {

    final RecordTypeMaterialDAO dao = new RecordTypeMaterialDAO();

    try {
      final Map<String, Object> mapRecordTypeMaterial = new HashMap<>();
      final RecordTypeMaterial rtm = dao.get006HeaderCode(session, recordTypeCode);

      mapRecordTypeMaterial.put(Global.HEADER_TYPE_LABEL, rtm.getBibHeader006());
      mapRecordTypeMaterial.put(Global.FORM_OF_MATERIAL_LABEL, rtm.getAmicusMaterialTypeCode());
      return mapRecordTypeMaterial;
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Returns the multipart resource level associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the multipart resource level associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getMultipartResourceLevels(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_LNK_REC.class, locale(lang));
  }

  /**
   * Returns the descriptive catalog forms associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the descriptive catalog forms associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getDescriptiveCatalogForms(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_DSCTV_CTLG.class, locale(lang));
  }

  /**
   * Returns the bibliographic levels associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the bibliographic level associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getBibliographicLevels(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_BIB_LVL.class, locale(lang));
  }

  /**
   * Returns the character encoding schemas associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the character encoding schema associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getCharacterEncodingSchemas(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_CCS.class, locale(lang));
  }

  /**
   * Returns the control types associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the control type associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getControlTypes(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_CNTL_TYP.class, locale(lang));
  }


  /**
   * Returns the record status types associated with the given language.
   *
   * @param lang the language code, used here as a filter criterion.
   * @return a list of code / description tuples representing the record status type associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getRecordStatusTypes(final String lang) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getList(session, T_ITM_REC_STUS.class, locale(lang));
  }

  /**
   * Returns the description for heading type entity.
   *
   * @param code     the heading marc category code, used here as a filter criterion.
   * @param lang     the language code, used here as a filter criterion.
   * @param category the category field.
   * @return the description for index code associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public String getHeadingTypeDescription(final int code, final String lang, final int category) throws DataAccessException {
    final CodeTableDAO dao = new CodeTableDAO();
    return dao.getLongText(session, code, FIRST_CORRELATION_HEADING_CLASS_MAP.get(category), locale(lang));
  }

  /**
   * Return the language independent (key) index value to be used when
   * browsing for entries of this type of Descriptor
   *
   * @param descriptor the descriptor, used here as a filter criterion.
   * @param session    the session of hibernate
   * @return the browse index
   * @throws HibernateException
   */
  public String getBrowseKey(final Descriptor descriptor, final Session session) throws HibernateException {
    final IndexListDAO dao = new IndexListDAO();
    final String result = dao.getIndexBySortFormType(descriptor.getSortFormParameters().getSortFormMainType(), descriptor.getCorrelationValues().getValue(1), session);
    return (result != null) ? result : descriptor.getBrowseKey();
  }

  /**
   * Return a list of headings for a specific a search through the stringText of the tag
   *
   * @param stringText  the string text of the tag used here as filter criterion
   * @param view        the view used here as filter criterion
   * @param mainLibrary the main library used here as filter criterion
   * @param pageSize    the page size used here as filter criterion
   * @return a list of headings
   * @throws DataAccessException
   * @throws InvalidBrowseIndexException
   */
  public List<MapHeading> getHeadingsByTag(final String tag, final String indicator1, final String indicator2, final String stringText, final int view, final int mainLibrary, final int pageSize) {
    try {
      String key;
      String browseTerm;
      String operator = ">";
      final List<Descriptor> descriptorsList;
      final BibliographicCatalog catalog = new BibliographicCatalog();
      final CatalogItem item = new BibliographicItem();
      final TagImpl impl = new BibliographicTagImpl();
      final Correlation corr = impl.getCorrelation(tag, indicator1.charAt(0), indicator2.charAt(0), 0, session);
      final Tag newTag = catalog.getNewTag(item, corr.getKey().getMarcTagCategoryCode(), corr.getValues());
      if (newTag != null) {
        final StringText st = new StringText(stringText);
        ((VariableField) newTag).setStringText(st);
        if (newTag instanceof Browsable) {
          ((Browsable) newTag).setDescriptorStringText(st);
          final Descriptor descriptor = ((Browsable) newTag).getDescriptor();
          key = getBrowseKey(descriptor, session);
          final DescriptorDAO dao = (DescriptorDAO) descriptor.getDAO();
          String filter = Global.FILTER_MAP.get(key);
          if (dao instanceof ShelfListDAO) {
            filter = filter + HDG_MAIN_LIBRARY_NUMBER + mainLibrary;
          }
          descriptor.calculateAndSetSortForm();
          browseTerm = descriptor.getSortForm();

          if (dao instanceof PublisherDescriptorDAO || dao instanceof NameTitleNameDescriptorDAO)
            operator = ">=";
          descriptorsList = dao.getHeadingsBySortform(operator, EMPTY_STRING, browseTerm, filter, view, pageSize, session);
          return getMapHeadings(view, descriptorsList, dao);
        }
      }
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
    return Collections.emptyList();
  }


  /**
   * Get the record associated with given data.
   *
   * @param itemNumber -- the record identifier.
   * @param view       -- the search view.
   * @return the {@link BibliographicRecord} associated with the given data.
   */
  public ContainerRecordTemplate getBibliographicRecordById(final int itemNumber, final int view) {

    final ContainerRecordTemplate container = new ContainerRecordTemplate();
    CatalogItem item;
    try {
      item = getCatalogItemByKey(itemNumber, view);
    } catch (RecordNotFoundException re) {
      return null;
    }

    final BibliographicRecord bibliographicRecord = new BibliographicRecord();
    bibliographicRecord.setId(item.getAmicusNumber());
    bibliographicRecord.setRecordView(item.getUserView());

    org.folio.marccat.resources.domain.Leader leader = new org.folio.marccat.resources.domain.Leader();
    leader.setCode("000");
    leader.setValue(((org.folio.marccat.dao.persistence.Leader) item.getTag(0)).getDisplayString());
    bibliographicRecord.setLeader(leader);
    final char canadianIndicator = ((BibliographicItem) item).getBibItmData().getCanadianContentIndicator();
    bibliographicRecord.setCanadianContentIndicator(valueOf(canadianIndicator));
    bibliographicRecord.setVerificationLevel(valueOf(item.getItemEntity().getVerificationLevel()));

    item.getTags().stream().skip(1).forEach((Tag aTag) -> {
      int keyNumber = 0;
      int sequenceNbr = 0;
      int skipInFiling = 0;

      if (aTag.isFixedField() && aTag instanceof MaterialDescription) {
        final MaterialDescription materialTag = (MaterialDescription) aTag;
        keyNumber = materialTag.getMaterialDescriptionKeyNumber();
        final String tagNbr = materialTag.getMaterialDescription008Indicator().equals("1") ? "008" : "006";
        final Map<String, Object> map ;
        if(tagNbr.equals("008"))
          map = getMaterialTypeInfosByLeaderValues(materialTag.getItemRecordTypeCode(), materialTag.getItemBibliographicLevelCode(), tagNbr);
        else
          map = getHeaderTypeByRecordTypeCode(materialTag.getMaterialTypeCode().charAt(0));
        materialTag.setHeaderType((int) map.get(Global.HEADER_TYPE_LABEL));
        materialTag.setMaterialTypeCode(tagNbr.equalsIgnoreCase("006") ? materialTag.getMaterialTypeCode() : null);
        materialTag.setFormOfMaterial((String) map.get(Global.FORM_OF_MATERIAL_LABEL));
      }

      if (aTag.isFixedField() && aTag instanceof PhysicalDescription) {
        final PhysicalDescription physicalTag = (PhysicalDescription) aTag;
        keyNumber = physicalTag.getKeyNumber();
      }

      if (!aTag.isFixedField() && aTag instanceof BibliographicAccessPoint) {
        keyNumber = ((BibliographicAccessPoint) aTag).getDescriptor().getKey().getHeadingNumber();
        try {
          sequenceNbr = ((BibliographicAccessPoint) aTag).getSequenceNumber();
        } catch (Exception e) {
          sequenceNbr = 0;
        }

        if (aTag instanceof TitleAccessPoint) {
          skipInFiling = ((TitleAccessPoint) aTag).getDescriptor().getSkipInFiling();
        }
      }

      if (!aTag.isFixedField() && aTag instanceof BibliographicNoteTag) {
        keyNumber = ((BibliographicNoteTag) aTag).getNoteNbr();
        try {
          sequenceNbr = ((BibliographicNoteTag) aTag).getSequenceNumber();
        } catch (Exception e) {
          sequenceNbr = 0;
        }
      }

      if (!aTag.isFixedField() && aTag instanceof PublisherManager && !((PublisherManager) aTag).getPublisherTagUnits().isEmpty()) {
        keyNumber = ((PublisherManager) aTag).getPublisherTagUnits().get(0).getPublisherHeadingNumber(); //add gestione multi publisher
      }

      final CorrelationKey correlation = aTag.getTagImpl().getMarcEncoding(aTag, session);


      String entry = aTag.isFixedField()
        ? (((FixedField) aTag).getDisplayString())
        : ((VariableField) aTag).getStringText().getMarcDisplayString(Subfield.SUBFIELD_DELIMITER);

      if(aTag instanceof PublisherManager) {
        try {
          entry = aTag.addPunctuation().getMarcDisplayString(Subfield.SUBFIELD_DELIMITER);
        } catch (Exception exception) {
          logger.error(Message.MOD_MARCCAT_00013_IO_FAILURE, exception);
        }
      }
      final org.folio.marccat.resources.domain.Field field = new org.folio.marccat.resources.domain.Field();
      org.folio.marccat.resources.domain.VariableField variableField;
      org.folio.marccat.resources.domain.FixedField fixedField;
      String tagNumber = correlation.getMarcTag();

      if (aTag.isFixedField()) {
        fixedField = new org.folio.marccat.resources.domain.FixedField();
        fixedField.setSequenceNumber(sequenceNbr);
        fixedField.setCode(tagNumber);
        fixedField.setDisplayValue(entry);
        fixedField.setHeaderTypeCode(aTag.getCorrelation(1));
        fixedField.setCategoryCode(aTag.getCategory());
        fixedField.setKeyNumber(keyNumber);
        field.setFixedField(fixedField);
      } else {
        variableField = new org.folio.marccat.resources.domain.VariableField();
        variableField.setSequenceNumber(sequenceNbr);
        variableField.setCode(correlation.getMarcTag());
        variableField.setInd1(EMPTY_STRING + correlation.getMarcFirstIndicator());
        variableField.setInd2(EMPTY_STRING + correlation.getMarcSecondIndicator());
        variableField.setHeadingTypeCode(Integer.toString(aTag.getCorrelation(1)));
        variableField.setItemTypeCode(Integer.toString(aTag.getCorrelation(2)));
        variableField.setFunctionCode(Integer.toString(aTag.getCorrelation(3)));
        variableField.setValue(entry);
        variableField.setCategoryCode(correlation.getMarcTagCategoryCode());
        variableField.setKeyNumber(keyNumber);
        variableField.setSkipInFiling(skipInFiling);
        if (variableField.getInd2().equals("S"))
          variableField.setInd2(EMPTY_STRING + skipInFiling);
        field.setVariableField(variableField);
      }

      field.setCode(tagNumber);

      bibliographicRecord.getFields().add(field);
    });

    container.setBibliographicRecord(bibliographicRecord);
    container.setRecordTemplate(ofNullable(item.getModelItem()).map(model -> {
      try {
        final ObjectMapper objectMapper = new ObjectMapper();
        final RecordTemplate template = objectMapper.readValue(model.getRecordFields(), RecordTemplate.class);
        template.setId(model.getModel().getId());
        return template;
      } catch (IOException exception) {
        logger.error(Message.MOD_MARCCAT_00013_IO_FAILURE, exception);
        return null;
      }
    }).orElse(null));

    return container;
  }

  /**
   * Gets category code using tag and indicators.
   *
   * @param tag             -- the tag number.
   * @param firstIndicator  -- the 1.st indicator.
   * @param secondIndicator -- the 2nd. indicator.
   * @param hasTitle        -- indicates if there is a title portion in tag value.
   * @return category code.
   * @throws DataAccessException -- in case of DataAccessException.
   */
  public int getTagCategory(final String tag,
                            final char firstIndicator,
                            final char secondIndicator,
                            final boolean hasTitle) throws DataAccessException {
    final BibliographicCorrelationDAO dao = new BibliographicCorrelationDAO();

    try {
      List<BibliographicCorrelation> correlations = dao.getCategoryCorrelation(session, tag, firstIndicator, secondIndicator);
      if (correlations.stream().anyMatch(Objects::nonNull) && correlations.size() == 1) {
        Optional<BibliographicCorrelation> firstElement = correlations.stream().findFirst();
        if(firstElement.isPresent())
          return firstElement
            .get()
            .getKey()
            .getMarcTagCategoryCode();
      } else {
        if (correlations.size() > 1) {
          if ((tag.endsWith("00") || tag.endsWith("10") || tag.endsWith("11")) && hasTitle) {
            return Global.NAME_TITLE_CATEGORY;
          } else if (correlations.stream().anyMatch(Objects::nonNull)) {
            Optional<BibliographicCorrelation> firstElement = correlations.stream().findFirst();
            if(firstElement.isPresent())
              return firstElement
                .get()
                .getKey()
                .getMarcTagCategoryCode();
          }
        }
      }

      return 0;

    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }


  /**
   * Gets category code using tag and indicators.
   *
   * @param tag             -- the tag number.
   * @param firstIndicator  -- the 1.st indicator.
   * @param secondIndicator -- the 2nd. indicator.
   * @return category code.
   * @throws DataAccessException -- in case of DataAccessException.
   */
  public int getCategoryByTag(final String tag,
                            final char firstIndicator,
                            final char secondIndicator) {
    final BibliographicCorrelationDAO dao = new BibliographicCorrelationDAO();

    try {
      List<BibliographicCorrelation> correlations = dao.getCategoryCorrelation(session, tag, firstIndicator, secondIndicator);
       if (correlations.stream().anyMatch(Objects::nonNull)) {
         Optional<BibliographicCorrelation> firstElement = correlations.stream().findFirst();
         if(firstElement.isPresent())
            return firstElement
              .get()
              .getKey()
              .getMarcTagCategoryCode();
       }

      return 0;

    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Checks if record is new then execute insert or update.
   *
   * @param record             -- the bibliographic record to save.
   * @param view               -- the view associated to user.
   * @param generalInformation -- @linked GeneralInformation for default values.
   * @throws DataAccessException in case of data access exception.
   */
  public void saveBibliographicRecord(final BibliographicRecord record, final RecordTemplate template, final int view, final GeneralInformation generalInformation, final String lang, final Map<String, String> configuration) throws DataAccessException {
    CatalogItem item = null;
    try {
      item = getCatalogItemByKey(record.getId(), view);
    } catch (DataAccessException exception) {
      // do not put any exception here!!!!!!!!!!!!! , because the microservice doesn't insert the record
    }

    try {

      if (item == null || item.getTags().isEmpty()) {
        item = insertBibliographicRecord(record, view, generalInformation, lang, configuration);
      } else {
        updateBibliographicRecord(record, item, view, generalInformation, configuration);
      }

      final int an = item.getAmicusNumber();

      BibliographicModel model = getItemModel(template, an);
      if (ofNullable(model).isPresent())
        item.setModelItem(model);

      if (isNotNullOrEmpty(record.getVerificationLevel()))
        item.getItemEntity().setVerificationLevel(record.getVerificationLevel().charAt(0));
      if (isNotNullOrEmpty(record.getCanadianContentIndicator()))
        ((BibliographicItem) item).getBibItmData().setCanadianContentIndicator(record.getCanadianContentIndicator().charAt(0));


      final BibliographicCatalogDAO dao = new BibliographicCatalogDAO();
      item.getModelItem().getModel().setLabel(template.getName() != null ? template.getName() : "Monografia");
      dao.saveCatalogItem(item, session);

    } catch (Exception e) {
      logger.error(Message.MOD_MARCCAT_00019_SAVE_RECORD_FAILURE, record.getId(), e);
      throw new DataAccessException(e);
    }
  }

  /**
   * Get BibliographicModel associated to record.
   *
   * @param template -- the current template.
   * @param an       -- the record id.
   */
  private BibliographicModel getItemModel(final RecordTemplate template, final int an) {
    if (ofNullable(template).isPresent()) {

      final BibliographicModelItemDAO dao = new BibliographicModelItemDAO();
      final ObjectMapper mapper = new ObjectMapper();
      try {
        ModelItem modelItem = dao.load(an, session);
        BibliographicModel model;
        if (modelItem != null) {
          model = (BibliographicModel) dao.load(an, session).getModel();
        } else {
          model = new BibliographicModel();
        }
        model.setId(template.getId());
        model.setLabel(template.getName());
        model.setFrbrFirstGroup(template.getGroup());
        model.setRecordFields(mapper.writeValueAsString(template));
        return model;
      } catch (Exception e) {
        logger.error(Message.MOD_MARCCAT_00023_SAVE_TEMPLATE_ASSOCIATED_FAILURE, template.getId(), an, e);
        throw new ModMarccatException(e);
      }
    }
    return null;
  }


  /**
   * Updates a bibliographic record.
   *
   * @param record             -- the record to update.
   * @param item               -- the catalog item associated to record.
   * @param view               -- the current view associated to record.
   * @param generalInformation -- {@linked GeneralInformation} for default values.
   * @throws DataAccessException in case of data access exception.
   */
  private void updateBibliographicRecord(final BibliographicRecord record, final CatalogItem item, final int view,
                                         final GeneralInformation generalInformation, final Map<String, String> configuration) throws DataAccessException {

    final RecordParser recordParser = new RecordParser();
    final int bibItemNumber = item.getAmicusNumber();
    final String newLeader = record.getLeader().getValue();
    recordParser.changeLeader(item, newLeader);

    record.getFields().forEach(field -> {

      final String tagNbr = field.getCode();
      final Field.FieldStatus status = field.getFieldStatus();

      if (tagNbr.equals(Global.MATERIAL_TAG_CODE) && status == Field.FieldStatus.CHANGED) {
        recordParser.changeMaterialDescriptionTag(item, field, session);
      }

      if (status == Field.FieldStatus.NEW
        || status == Field.FieldStatus.DELETED
        || status == Field.FieldStatus.CHANGED) {

        if (tagNbr.equals(Global.OTHER_MATERIAL_TAG_CODE)) {
          final Map<String, Object> mapRecordTypeMaterial = getHeaderTypeByRecordTypeCode(field.getFixedField().getMaterialTypeCode().charAt(0));
          final String formOfMaterial = (String) mapRecordTypeMaterial.get(Global.FORM_OF_MATERIAL_LABEL);
          recordParser.changeMaterialDescriptionOtherTag(item, field, formOfMaterial, generalInformation);
        }

        if (tagNbr.equals(Global.PHYSICAL_DESCRIPTION_TAG_CODE)) {
          recordParser.changePhysicalDescriptionTag(item, field, bibItemNumber);
        }

        if (tagNbr.equals(Global.CATALOGING_SOURCE_TAG_CODE) && status == Field.FieldStatus.CHANGED) {
          item.getTags().stream().filter(aTag -> !aTag.isFixedField() && aTag instanceof CataloguingSourceTag).forEach(aTag -> {
            final CataloguingSourceTag cst = (CataloguingSourceTag) aTag;
            cst.setStringText(new StringText(field.getVariableField().getValue()));
            cst.markChanged();
          });
        }

        if (field.getVariableField() != null && !tagNbr.equals(Global.CATALOGING_SOURCE_TAG_CODE)) {
          final org.folio.marccat.resources.domain.VariableField variableField = field.getVariableField();
          final CorrelationValues correlationValues = getCorrelationVariableField(variableField.getCategoryCode(),
            variableField.getInd1(), variableField.getInd2(), tagNbr);
          if (correlationValues == null) {
            logger.error(Message.MOD_MARCCAT_00018_NO_HEADING_TYPE_CODE, variableField.getCode());
            throw new DataAccessException();
          }

          try {
            if (field.getVariableField().getCategoryCode() == Global.BIB_NOTE_CATEGORY && !Global.PUBLISHER_CODES.contains(correlationValues.getValue(1))) {
              recordParser.changeNoteTag(item, field, correlationValues, bibItemNumber, view, configuration);
            } else if (field.getVariableField().getCategoryCode() == Global.BIB_NOTE_CATEGORY && Global.PUBLISHER_CODES.contains(correlationValues.getValue(1))) {
              recordParser.changePublisherTag(item, field, correlationValues, bibItemNumber, view, session, configuration);
            } else {
              recordParser.changeAccessPointTag(item, field, correlationValues, bibItemNumber, view, session, configuration);
            }

          } catch (HibernateException | SQLException e) {
            throw new DataAccessException(e);
          }
        }
      }
    });

  }

  /**
   * Set descriptors for each tag.
   *
   * @param item            -- the catalog item.
   * @param recordView      -- the record view.
   * @param cataloguingView -- the cataloguing view.
   * @throws DataAccessException in case of data access exception.
   */
  public void setDescriptors(final CatalogItem item, final int recordView, final int cataloguingView) throws DataAccessException {

    item.getTags().forEach(aTag -> {
      if (aTag instanceof AccessPoint) {
        try {
          AccessPoint apf = ((AccessPoint) aTag);
          Descriptor d = apf.getDAODescriptor().findOrCreateMyView(apf.getHeadingNumber(), View.makeSingleViewString(recordView), cataloguingView, session);
          apf.setDescriptor(d);
        } catch (HibernateException e) {
          throw new DataAccessException(e);
        }
      } else if (aTag instanceof BibliographicRelationshipTag) {
        BibliographicRelationshipTag relTag = (BibliographicRelationshipTag) aTag;
        relTag.copyFromAnotherItem();
      }
    });
  }

  /**
   * Insert a new bibliographic record.
   *
   * @param record -- the record bibliographic.
   * @param view   -- the current view associated to record.
   * @param giAPI  -- {@linked GeneralInformation} for default values.
   * @throws DataAccessException in case of data access exception.
   */
  private CatalogItem insertBibliographicRecord(final BibliographicRecord record, final int view, final GeneralInformation giAPI, final String lang, final Map<String, String> configuration) throws DataAccessException {
    final RecordParser recordParser = new RecordParser();
    final BibliographicCatalog catalog = new BibliographicCatalog();
    final int bibItemNumber = record.getId();
    final CatalogItem item = catalog.newCatalogItem(new Object[]{view, bibItemNumber});

    Leader leader = record.getLeader();
    item.getItemEntity().setLanguageOfCataloguing(lang);

    if (leader != null) {
      final BibliographicLeader bibLeader = catalog.createRequiredLeaderTag(item);
      catalog.toBibliographicLeader(leader.getValue(), bibLeader);
      item.addTag(bibLeader);
    }

    ControlNumberTag cnt = catalog.createRequiredControlNumberTag(item);
    item.addTag(cnt);

    DateOfLastTransactionTag dateOfLastTransactionTag = catalog.createRequiredDateOfLastTransactionTag(item);
    item.addTag(dateOfLastTransactionTag);

    record.getFields().stream().skip(1).forEach(field -> {
      final String tagNbr = field.getCode();
      if (tagNbr.equals(Global.MATERIAL_TAG_CODE) || tagNbr.equals(Global.OTHER_MATERIAL_TAG_CODE)) {
        final org.folio.marccat.resources.domain.FixedField fixedField = field.getFixedField();
        final Map<String, Object> mapRecordTypeMaterial;
        final String formOfMaterial;
        if (tagNbr.equals(Global.MATERIAL_TAG_CODE)) {
          mapRecordTypeMaterial = getMaterialTypeInfosByLeaderValues(leader.getValue().charAt(6), leader.getValue().charAt(7), tagNbr);
          formOfMaterial = (String) mapRecordTypeMaterial.get(Global.FORM_OF_MATERIAL_LABEL);
          fixedField.setHeaderTypeCode((int) mapRecordTypeMaterial.get(Global.HEADER_TYPE_LABEL));
        } else {
          mapRecordTypeMaterial = getMaterialTypeInfosByHeaderCode(fixedField.getHeaderTypeCode(), tagNbr);
          formOfMaterial = (String) mapRecordTypeMaterial.get(Global.FORM_OF_MATERIAL_LABEL);
        }

        recordParser.addMaterialDescriptionToCatalog(tagNbr, item, fixedField, giAPI, formOfMaterial);
      }

      if (tagNbr.equals(Global.PHYSICAL_DESCRIPTION_TAG_CODE)) {
        final org.folio.marccat.resources.domain.FixedField fixedField = field.getFixedField();
        recordParser.addPhysicalDescriptionTag(item, fixedField, bibItemNumber);
      }

      if (tagNbr.equals(Global.CATALOGING_SOURCE_TAG_CODE)) {
        final org.folio.marccat.resources.domain.VariableField variableField = field.getVariableField();
        CataloguingSourceTag cst = catalog.createRequiredCataloguingSourceTag(item);
        cst.setStringText(new StringText(variableField.getValue()));
        item.addTag(cst);
      }

      if (field.getVariableField() != null && !tagNbr.equals(Global.CATALOGING_SOURCE_TAG_CODE)) {
        final org.folio.marccat.resources.domain.VariableField variableField = field.getVariableField();
        final CorrelationValues correlationValues = getCorrelationVariableField(variableField.getCategoryCode(),
          variableField.getInd1(), variableField.getInd2(), tagNbr);
        if (correlationValues == null) {
          logger.error(Message.MOD_MARCCAT_00018_NO_HEADING_TYPE_CODE, variableField.getCode());
          throw new DataAccessException();
        }
        try {
          recordParser.insertNewVariableField(item, variableField, bibItemNumber, correlationValues, configuration, session, view);
        } catch (HibernateException exception) {
          logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
          throw new DataAccessException(exception);
        }
      }

    });
    setDescriptors(item, item.getUserView(), view);
    return item;
  }

  /**
   * Gets Validation for tag field.
   *
   * @param marcCategory the marc category used here as filter criterion.
   * @param tagNumber    the tag number used here as filter criterion.
   * @return Validation object containing subfield list.
   */
  public Validation getTagValidation(final int marcCategory,
                                     final String tagNumber) throws DataAccessException {
    final BibliographicValidationDAO daoBibliographicValidation = new BibliographicValidationDAO();
    try {
      return daoBibliographicValidation.load(session, tagNumber, marcCategory);
    } catch (final HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }


  /**
   * Delete a bibliographic record.
   *
   * @param itemNumber -- the amicus number associated to record.
   */
  public void deleteBibliographicRecordById(final Integer itemNumber, final int view) throws DataAccessException {
    final BibliographicCatalog catalog = new BibliographicCatalog();

    try {
      CatalogItem item = getCatalogItemByKey(itemNumber, view);
      catalog.deleteCatalogItem(item, session);
    } catch (RecordNotFoundException exception) {
      //ignore
    } catch (Exception exception) {
      logger.error(Message.MOD_MARCCAT_00022_DELETE_RECORD_FAILURE, itemNumber, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Unlock a record or heading locked from user previously.
   *
   * @param id       -- the key number or amicus number.
   * @param userName -- the username who unlock entity.
   */
  public void unlockRecord(final int id, final String userName) throws DataAccessException {
    try {
      final BibliographicCatalog catalog = new BibliographicCatalog();
      catalog.unlock(id, userName, session);
    } catch (RecordInUseException exception) {
      logger.error(Message.MOD_MARCCAT_00021_UNLOCK_FAILURE, id, userName, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Lock a record or heading.
   *
   * @param id       -- the key number or amicus number.
   * @param userName -- the username who unlock entity.
   * @param uuid     -- the uuid associated to lock/unlock session.
   */
  public void lockRecord(final int id, final String userName, final String uuid) throws DataAccessException {
    try {
      final BibliographicCatalog catalog = new BibliographicCatalog();
      catalog.lock(id, userName, uuid, session);
    } catch (RecordInUseException exception) {
      logger.error(Message.MOD_MARCCAT_00020_LOCK_FAILURE, id, userName, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Save the heading, if the capture already exists
   *
   * @param heading       the heading.
   * @param view          the view.
   * @param configuration the configuration.
   * @throws DataAccessException in case of data access failure.
   */
  public void saveHeading(final Heading heading, final int view,
                          final Map<String, String> configuration) throws DataAccessException {
    try {
      final BibliographicCatalog catalog = new BibliographicCatalog();
      final CatalogItem item = catalog.newCatalogItem(new Object[]{view});
      final TagImpl impl = new BibliographicTagImpl();
      final boolean isInd1IsEmpty = heading.getInd1().equals(EMPTY_VALUE);
      final boolean isInd2IsEmpty = heading.getInd2().equals(EMPTY_VALUE);
      final Correlation corr = impl.getCorrelation(heading.getTag(), (isInd1IsEmpty) ? " ".charAt(0) : heading.getInd1().charAt(0), (isInd2IsEmpty) ? " ".charAt(0) : heading.getInd2().charAt(0), heading.getCategoryCode(), session);
      final Tag newTag = catalog.getNewTag(item, corr.getKey().getMarcTagCategoryCode(), corr.getValues());
      if (newTag != null) {
        final StringText st = new StringText(heading.getDisplayValue());
        ((VariableField) newTag).setStringText(st);
        if (newTag instanceof Browsable) {
          final int skipInFiling = updateIndicatorNotNumeric(corr.getKey(), heading.getInd1(), heading.getInd2());
          ((Browsable) newTag).setDescriptorStringText(st);
          final Descriptor descriptor = ((Browsable) newTag).getDescriptor();
          descriptor.setSkipInFiling(skipInFiling);
          if(newTag.isNameTitle()) {
            createNameAndTitleDescriptor(configuration,descriptor,view);
          }
          final int headingNumber = createOrReplaceDescriptor(configuration, descriptor, view);
          heading.setKeyNumber(headingNumber);
        } else if (newTag.isPublisher()) {
          createPublisherDescriptor(heading, view, configuration, newTag);
        }
      }
    } catch (HibernateException | SQLException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Save a publisher heading, if the capture already exists
   *
   * @param heading       the heading.
   * @param view          the view.
   * @param configuration the configuration.
   * @param newTag        the new tag.
   * @throws DataAccessException in case of data access failure.
   */
  public void createPublisherDescriptor(final Heading heading, final int view, Map <String, String> configuration, final Tag newTag) throws HibernateException, SQLException {
    final List<PUBL_TAG> publisherTagUnits = ((PublisherManager) newTag).getPublisherTagUnits();
    for (PUBL_TAG publisherTag : publisherTagUnits) {
      final Descriptor descriptor = publisherTag.getDescriptor();
      final int headingNumber = createOrReplaceDescriptor(configuration, descriptor, view);
      heading.setKeyNumber(headingNumber);
    }
  }

  /**
   * Save a name title heading, if the capture already exists
   *
   * @param view          the view.
   * @param configuration the configuration.
   * @param descriptor    the descriptor.
   * @throws DataAccessException in case of data access failure.
   */
  public void createNameAndTitleDescriptor(final Map <String, String> configuration, final Descriptor descriptor, int view) throws HibernateException, SQLException {
    final NME_TTL_HDG nameTitleHeading  = (NME_TTL_HDG) descriptor;
    final int nameHeadingNumber = createOrReplaceDescriptor(configuration, nameTitleHeading.getNameHeading(), view);
    final int titleHeadingNumber = createOrReplaceDescriptor(configuration,nameTitleHeading.getTitleHeading() , view);
    nameTitleHeading.getNameHeading().setHeadingNumber(nameHeadingNumber);
    nameTitleHeading.setNameHeadingNumber(nameHeadingNumber);
    nameTitleHeading.getTitleHeading().setHeadingNumber(titleHeadingNumber);
    nameTitleHeading.setTitleHeadingNumber(titleHeadingNumber);
  }

  /**
   * Save a heading, if the capture already exists
   *
   * @param configuration the configuration.
   * @param descriptor    the descriptor.
   * @param view          the view.
   * @throws DataAccessException in case of data access failure.
   */
  public int createOrReplaceDescriptor(final Map <String, String> configuration, final Descriptor descriptor, final int view) throws HibernateException, SQLException {
    descriptor.setUserViewString(View.makeSingleViewString(view));
    descriptor.setConfigValues(configuration);
    final Descriptor dup = ((DescriptorDAO) (descriptor.getDAO())).getMatchingHeading(descriptor, session);
    if (dup == null) {
      descriptor.generateNewKey(session);
      descriptor.getDAO().save(descriptor, session);
      return descriptor.getHeadingNumber();
    } else {
      return dup.getHeadingNumber();
    }
  }

  /**
   * @param lang     the language code, used here as a filter criterion.
   * @param category the category, used here as a filter criterion.
   * @return a list of heading item types by marc category code associated with the requested language.
   * @throws DataAccessException in case of data access failure.
   */
  public List<Avp<String>> getFirstCorrelation(final String lang, final int category) throws DataAccessException {
    final CodeTableDAO daoCT = new CodeTableDAO();
    return daoCT.getList(session, FIRST_CORRELATION_HEADING_CLASS_MAP.get(category), locale(lang));
  }


  /**
   * Changes any non-numeric indicators from the correlation table
   * S for skipinfiling for bibliographic tags
   *
   * @param coKey
   * @param indicator1
   * @param indicator2
   */
  private int updateIndicatorNotNumeric(final CorrelationKey coKey, final String indicator1, final String indicator2) {
    final int skipInFiling = 0;
    if (coKey.getMarcFirstIndicator() == BIBLIOGRAPHIC_INDICATOR_NOT_NUMERIC)
      return (!indicator1.isEmpty()) ? Integer.parseInt(indicator1) : skipInFiling;
    else if (coKey.getMarcSecondIndicator() == BIBLIOGRAPHIC_INDICATOR_NOT_NUMERIC && !indicator2.isEmpty())
      return (!indicator2.equals(EMPTY_VALUE)) ? Integer.parseInt(indicator2) : skipInFiling;
    return skipInFiling;
  }


  /**
   * Update of an existing heading
   *
   * @param heading the heading.
   * @param view    the view.
   * @throws DataAccessException in case of data access failure.
   */
  public void updateHeading(final Heading heading, final int view) throws DataAccessException {
    try {
      final TagImpl impl = new BibliographicTagImpl();
      final BibliographicCatalog catalog = new BibliographicCatalog();
      final CatalogItem item = new BibliographicItem();
      final Correlation corr = impl.getCorrelation(heading.getTag(), heading.getInd1().charAt(0), heading.getInd2().charAt(0), 0, session);
      final Tag newTag = catalog.getNewTag(item, corr.getKey().getMarcTagCategoryCode(), corr.getValues());
      if (newTag != null) {
        final StringText st = new StringText(heading.getDisplayValue());
        ((VariableField) newTag).setStringText(st);
        if (newTag instanceof Browsable) {
          final int skipInFiling = updateIndicatorNotNumeric(corr.getKey(), heading.getInd1(), heading.getInd2());
          ((Browsable) newTag).setDescriptorStringText(st);
          final Descriptor descriptor = ((Browsable) newTag).getDescriptor();
          final DescriptorDAO descriptorDao = DescriptorFactory.getDao(heading.getCategoryCode());
          final Descriptor d = descriptorDao.load(heading.getKeyNumber(), view, session);
          if (d != null) {
            d.setSkipInFiling(skipInFiling);
            d.setStringText(descriptor.getStringText());
            d.getDAO().update(d, session);
          }
        }
      }
    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }

  }

  /**
   * delete the heading
   *
   * @param heading the heading.
   * @param view    the view.
   * @throws DataAccessException in case of data access failure.
   */
  public void deleteHeadingById(final Heading heading, final int view) throws DataAccessException {
    try {
      final DescriptorDAO descriptorDao = DescriptorFactory.getDao(heading.getCategoryCode());
      final Descriptor d = descriptorDao.load(heading.getKeyNumber(), view, session);
      d.getDAO().delete(d, session);
    } catch (HibernateException | ReferentialIntegrityException exception ) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }

  }

  /**
   * Executes a CCL query using the given data to get the total count of the documents
   *
   * @param cclQuery      the CCL query.
   * @param mainLibraryId the main library identifier.
   * @param locale        the current locale.
   * @param searchingView the target search view.
   * @return a list of docid matching the input query.
   */
  public int getCountDocumentByQuery(final String cclQuery, String[] attributes, String[] directions, final int mainLibraryId, final Locale locale, final int searchingView) {
    final Parser parser = new Parser(locale, mainLibraryId, searchingView, session);
    try (
      final Statement sql = stmt(connection());
      final ResultSet rs = executeQuery(sql, parser.parseAndCount(cclQuery, attributes, directions))) {
      int count = 0;
      while (rs.next()) {
        count = rs.getInt(1);
      }
      return count;
    } catch (final HibernateException | SQLException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      return 0;
    }
  }

  /**
   *  Loads tags list using the like operator on tag.
   *
   * @param tagNumber the tag number used as filter criterion.
   * @return
   * @throws DataAccessException
   */
  public List <String> getFilteredTagsList (final String tagNumber) throws DataAccessException {
    try {
      return new BibliographicCorrelationDAO().getFilteredTagsList(tagNumber, session);
    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   *  Loads tag list using the like operator on tag.
   *
   * @param tagNumber the tag number used as filter criterion.
   * @return
   * @throws DataAccessException
   */
  public FilteredTag getFilteredTag(final String tagNumber) throws DataAccessException {
    try {
      final BibliographicCorrelationDAO correlationDAO = new BibliographicCorrelationDAO();
      final FilteredTag filteredTag = new FilteredTag();
      final List <String> firstIndicators = new ArrayList <>();
      final List <String> secondIndicators = new ArrayList <>();
      correlationDAO.getFilteredTag(tagNumber, session)
        .stream().forEach( key -> setIndicators(firstIndicators, secondIndicators, key));
      filteredTag.setTag(tagNumber);
      filteredTag.setInd1(getDistinctIndicators(firstIndicators));
      filteredTag.setInd2(getDistinctIndicators(secondIndicators));
      filteredTag.setSubfields(correlationDAO.getSubfieldsTag(tagNumber, session));
      return filteredTag;
    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }

  /**
   * Return a distinct list of the numeric indicators including space character.
   *
   * @param indicators the list of the indicators used as filter criterion.
   * @return
   */
  public List <String> getDistinctIndicators(List <String> indicators) {
    return indicators
      .stream()
      .sorted()
      .distinct()
      .filter(StringUtils::isNumericSpace)
      .collect(Collectors.toList());
  }

  /**
   * Set the list of the first and second indicators or the skip in filing on the tag.
   *
   * @param firtIndicators the list of the first indicators number used as filter criterion.
   * @param secondIndicators the list of the second indicators used as filter criterion.
   * @param key the key used as filter criterion.
   * @return
   * @throws DataAccessException
   */
  private void setIndicators(final List <String> firtIndicators, final List <String> secondIndicators, final CorrelationKey key) {
    if (BIBLIOGRAPHIC_INDICATOR_NOT_NUMERIC != key.getMarcFirstIndicator())
      firtIndicators.add(valueOf(key.getMarcFirstIndicator()));
    else
      firtIndicators.addAll(Global.SKIP_IN_FILING_CODES);
    if(BIBLIOGRAPHIC_INDICATOR_NOT_NUMERIC != key.getMarcSecondIndicator()) {
      secondIndicators.add(valueOf(key.getMarcSecondIndicator()));
    } else {
      secondIndicators.addAll(Global.SKIP_IN_FILING_CODES);
    }
  }

  /**
   * Return all global variables
   *
   * @return all global variables
   * @throws DataAccessException
   */
  public  Map<String, String> getAllGlobalVariable ()  {
    try {
      return new GlobalVariableDAO().getAllGlobalVariable(session);
    } catch (HibernateException exception) {
      logger.error(Message.MOD_MARCCAT_00010_DATA_ACCESS_FAILURE, exception);
      throw new DataAccessException(exception);
    }
  }



}
