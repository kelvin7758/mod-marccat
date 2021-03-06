package org.folio.marccat.resources;

import org.folio.marccat.config.constants.Global;
import org.folio.marccat.resources.domain.HeadingDecoratorCollection;
import org.folio.marccat.shared.MapHeading;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.folio.marccat.config.constants.Global.BASE_URI;
import static org.folio.marccat.integration.MarccatHelper.doGet;
import static org.folio.marccat.resources.shared.MappingUtils.toHeading;

@RestController
@RequestMapping(value = BASE_URI, produces = "application/json")
public class BrowseAPI extends BaseResource {

  @GetMapping("/browse")
  public HeadingDecoratorCollection getFirstPage(
    @RequestParam final String query,
    @RequestParam final int view,
    @RequestParam final int mainLibrary,
    @RequestParam final int pageSize,
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant,
    @RequestHeader(Global.OKAPI_URL) String okapiUrl) {
    return doGet((storageService, configuration) -> {
      final HeadingDecoratorCollection container = new HeadingDecoratorCollection();
      container.setHeadings(
        storageService.getFirstPage(query, view, mainLibrary, pageSize, lang)
          .stream()
          .map(toHeading)
          .collect(toList()));
      return container;
    }, tenant, okapiUrl, configurator);
  }


  @GetMapping("/next-page")
  public HeadingDecoratorCollection getNextPage(
    @RequestParam final String query,
    @RequestParam final int view,
    @RequestParam final int mainLibrary,
    @RequestParam final int pageSize,
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant,
    @RequestHeader(Global.OKAPI_URL) String okapiUrl) {
    return doGet((storageService, configuration) -> {
      final List<MapHeading> headings = storageService.getNextPage(query, view, mainLibrary, pageSize, lang);
      final HeadingDecoratorCollection headingCollection = new HeadingDecoratorCollection();
      headingCollection.setHeadings(headings
        .stream()
        .map(toHeading)
        .collect(toList()));
      return headingCollection;
    }, tenant, okapiUrl, configurator);
  }


  @GetMapping("/previous-page")
  public HeadingDecoratorCollection getPreviousPage(
    @RequestParam final String query,
    @RequestParam final int view,
    @RequestParam final int mainLibrary,
    @RequestParam final int pageSize,
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant,
    @RequestHeader(Global.OKAPI_URL) String okapiUrl) {
    return doGet((storageService, configuration) -> {
      final HeadingDecoratorCollection container = new HeadingDecoratorCollection();
      container.setHeadings(
        storageService
          .getPreviousPage(query, view, mainLibrary, pageSize, lang)
          .stream()
          .map(toHeading)
          .collect(toList()));
      return container;
    }, tenant, okapiUrl,configurator);
  }


  @GetMapping("/headings-by-tag")
  public HeadingDecoratorCollection getHeadingsByTag(
    @RequestParam final String tag,
    @RequestParam final String indicator1,
    @RequestParam final String indicator2,
    @RequestParam final String stringText,
    @RequestParam final int view,
    @RequestParam final int mainLibrary,
    @RequestParam final int pageSize,
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant,
    @RequestHeader(Global.OKAPI_URL) String okapiUrl) {
    return doGet((storageService, configuration) -> {
      final List<MapHeading> headings = storageService.getHeadingsByTag(tag, indicator1, indicator2, stringText, view, mainLibrary, pageSize);
      final HeadingDecoratorCollection headingCollection = new HeadingDecoratorCollection();
      headingCollection.setHeadings(headings
        .stream()
        .map(toHeading)
        .collect(toList()));
      return headingCollection;
    }, tenant, okapiUrl, configurator);
  }

}
