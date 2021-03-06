package org.folio.marccat.resources.domain;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Heading
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"tag", "ind1", "ind2", "subfields"})
public class FilteredTag {

  @JsonProperty("tag")
  private String tag;

  @JsonProperty("ind1")
  private List <String> ind1 = new ArrayList <>();

  @JsonProperty("ind2")
  private List <String> ind2 = new ArrayList <>();

  @JsonProperty("subfields")
  private String subfields;

  /**
   * @return The tag
   */
  @JsonProperty("tag")
  public String getTag() {
    return tag;
  }

  /**
   * @param tag The tag
   */
  @JsonProperty("tag")
  public void setTag(String tag) {
    this.tag = tag;
  }

  public FilteredTag withTag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * @return The ind1
   */
  @JsonProperty("ind1")
  public List<String> getInd1() {
    return ind1;
  }

  /**
   * @param ind1 The ind1
   */
  @JsonProperty("ind1")
  public void setInd1( List<String> ind1) {
    this.ind1 = ind1;
  }

  public FilteredTag withInd1(List<String> ind1) {
    this.ind1 = ind1;
    return this;
  }

  /**
   * @return The ind2
   */
  @JsonProperty("ind2")
  public List<String> getInd2() {
    return ind2;
  }

  /**
   * @param ind2 The ind2
   */
  @JsonProperty("ind2")
  public void setInd2(List <String> ind2) {
    this.ind2 = ind2;
  }

  public FilteredTag withInd2(List <String> ind2) {
    this.ind2 = ind2;
    return this;
  }

  /**
   * @return The subfields
   */
  @JsonProperty("subfields")
  public String getSubfields() {
    return subfields;
  }

  /**
   * @param tag The subfields
   */
  @JsonProperty("subfields")
  public void setSubfields(String subfields) {
    this.subfields = subfields;
  }

  public FilteredTag withSubfields(String subfields) {
    this.subfields = subfields;
    return this;
  }
}
