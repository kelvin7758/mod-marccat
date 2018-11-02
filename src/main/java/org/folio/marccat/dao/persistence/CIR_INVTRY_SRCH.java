/*
 * (c) LibriCore
 *
 * Created on 16-may-2005
 *
 * CIR_INVTRY_SRCH.java
 */
package org.folio.marccat.dao.persistence;

import java.io.Serializable;

/**
 * @author Ruben
 * @version $Revision: 1.2 $, $Date: 2005/12/21 13:33:33 $
 * @since 1.0
 */
public class CIR_INVTRY_SRCH implements Serializable {

  private int inventorySearchNumber;
  private int branchOrganisationNumber;
  private char shelfListTypeCode;
  private char loanPrd;
  private String rangeFrom;
  private String rangeTo;
  private int copyCount;


  /**
   * @return Returns the copyCount.
   * @throws
   * @since 1.0
   */
  public int getCopyCount() {
    return copyCount;
  }

  /**
   * @param copyCount The copyCount to set.
   * @throws
   * @since 1.0
   */
  public void setCopyCount(int copyCount) {
    this.copyCount = copyCount;
  }

  /**
   * @return Returns the branchOrganisationNumber.
   * @throws
   * @since 1.0
   */
  public int getBranchOrganisationNumber() {
    return branchOrganisationNumber;
  }

  /**
   * @param branchOrganisationNumber The branchOrganisationNumber to set.
   * @throws
   * @since 1.0
   */
  public void setBranchOrganisationNumber(int branchOrganisationNumber) {
    this.branchOrganisationNumber = branchOrganisationNumber;
  }

  /**
   * @return Returns the inventorySearchNumber.
   * @throws
   * @since 1.0
   */
  public int getInventorySearchNumber() {
    return inventorySearchNumber;
  }

  /**
   * @param inventorySearchNumber The inventorySearchNumber to set.
   * @throws
   * @since 1.0
   */
  public void setInventorySearchNumber(int inventorySearchNumber) {
    this.inventorySearchNumber = inventorySearchNumber;
  }

  /**
   * @return Returns the loanPrd.
   * @throws
   * @since 1.0
   */
  public char getLoanPrd() {
    return loanPrd;
  }

  /**
   * @param loanPrd The loanPrd to set.
   * @throws
   * @since 1.0
   */
  public void setLoanPrd(char loanPrd) {
    this.loanPrd = loanPrd;
  }

  /**
   * @return Returns the rangeFrom.
   * @throws
   * @since 1.0
   */
  public String getRangeFrom() {
    return rangeFrom;
  }

  /**
   * @param rangeFrom The rangeFrom to set.
   * @throws
   * @since 1.0
   */
  public void setRangeFrom(String rangeFrom) {
    this.rangeFrom = rangeFrom;
  }

  /**
   * @return Returns the rangeTo.
   * @throws
   * @since 1.0
   */
  public String getRangeTo() {
    return rangeTo;
  }

  /**
   * @param rangeTo The rangeTo to set.
   * @throws
   * @since 1.0
   */
  public void setRangeTo(String rangeTo) {
    this.rangeTo = rangeTo;
  }

  /**
   * @return Returns the shelfListTypeCode.
   * @throws
   * @since 1.0
   */
  public char getShelfListTypeCode() {
    return shelfListTypeCode;
  }

  /**
   * @param shelfListTypeCode The shelfListTypeCode to set.
   * @throws
   * @since 1.0
   */
  public void setShelfListTypeCode(char shelfListTypeCode) {
    this.shelfListTypeCode = shelfListTypeCode;
  }
}