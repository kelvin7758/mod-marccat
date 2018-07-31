package org.folio.cataloging.resources.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Index
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "value", "label", "constraints" })
public class Index {

    @JsonProperty("value")
    private String code;

    @JsonProperty("label")
    private String description;

    @JsonProperty("constraints")
    @Valid
    private List<Constraint> constraints = new ArrayList<Constraint>();

    /**
     * 
     * @return
     *     The code
     */
    @JsonProperty("value")
    public String getCode() {
        return code;
    }

    /**
     * 
     * @param code
     *     The code
     */
    @JsonProperty("value")
    public void setCode(String code) {
        this.code = code;
    }

    public Index withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * 
     * @return
     *     The description
     */
    @JsonProperty("label")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *     The description
     */
    @JsonProperty("label")
    public void setDescription(String description) {
        this.description = description;
    }

    public Index withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * @return
     *     The constraints
     */
    @JsonProperty("constraints")
    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * 
     * @param constraints
     *     The constraints
     */
    @JsonProperty("constraints")
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Index withConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
        return this;
    }
}