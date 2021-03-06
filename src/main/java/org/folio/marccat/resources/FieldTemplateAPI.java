package org.folio.marccat.resources;

import org.folio.marccat.config.constants.Global;
import org.folio.marccat.config.log.Message;
import org.folio.marccat.resources.domain.FieldTemplate;
import org.folio.marccat.shared.CatalogingInformation;
import org.folio.marccat.shared.Validation;
import org.springframework.web.bind.annotation.*;

import static java.util.Optional.ofNullable;
import static org.folio.marccat.config.constants.Global.BASE_URI;
import static org.folio.marccat.integration.MarccatHelper.doGet;
import static org.folio.marccat.resources.shared.FixedFieldUtils.isFixedField;
import static org.folio.marccat.resources.shared.RecordUtils.getVariableField;

@RestController
@RequestMapping(value = BASE_URI, produces = "application/json")
public class FieldTemplateAPI extends BaseResource implements CatalogingInformation {


  @GetMapping("/field-template")
  public FieldTemplate getFieldTemplate(
    @RequestParam final int categoryCode,
    @RequestParam final String ind1,
    @RequestParam final String ind2,
    @RequestParam final String code,
    @RequestParam final Integer headerType,
    @RequestParam final String leader,
    @RequestParam final String valueField,
    @RequestParam final String lang,
    @RequestHeader(Global.OKAPI_TENANT_HEADER_NAME) final String tenant,
    @RequestHeader(Global.OKAPI_URL) String okapiUrl) {
    return doGet((storageService, configuration) ->
        !isFixedField(code)
          ? ofNullable(storageService.getCorrelationVariableField(categoryCode, ind1, ind2, code))
          .map(correlationValues -> {
            final Validation validation = storageService.getSubfieldsByCorrelations(
              categoryCode,
              correlationValues.getValue(1),
              correlationValues.getValue(2),
              correlationValues.getValue(3));
            final FieldTemplate fieldTemplate = new FieldTemplate();
            fieldTemplate.setVariableField(
              getVariableField(
                categoryCode,
                ind1,
                ind2,
                code,
                correlationValues,
                storageService.getHeadingTypeDescription(
                  correlationValues.getValue(1),
                  lang,
                  categoryCode),
                validation));
            return fieldTemplate;
          }).orElseGet(() -> {
            logger.error(Message.MOD_MARCCAT_00016_FIELD_PARAMETER_INVALID, categoryCode, code);
            return new FieldTemplate();
          })
          : ofNullable(CatalogingInformation.getFixedField(storageService, headerType, code, leader, valueField, lang, configuration))
          .map(fixedField -> {
            final FieldTemplate fieldT = new FieldTemplate();
            fieldT.setFixedField(fixedField);
            return fieldT;
          }).orElseGet(() -> {
            logger.error(Message.MOD_MARCCAT_00016_FIELD_PARAMETER_INVALID, categoryCode, code);
            return new FieldTemplate();
          })
      , tenant, okapiUrl, configurator);

  }
}
