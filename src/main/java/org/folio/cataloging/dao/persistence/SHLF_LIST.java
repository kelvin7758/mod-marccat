/*
 * (c) LibriCore
 * 
 * Created on 21-jun-2004
 * 
 * SHLF_LIST.java
 */
package org.folio.cataloging.dao.persistence;

import org.folio.cataloging.business.common.ConfigHandler;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.business.descriptor.Descriptor;
import org.folio.cataloging.business.descriptor.SortFormParameters;
import org.folio.cataloging.dao.DAOIndexList;
import org.folio.cataloging.dao.DAOShelfList;
import org.folio.cataloging.dao.common.HibernateUtil;
import org.folio.cataloging.shared.CorrelationValues;

import java.io.Serializable;

/**
 * @author Usuario
 * @version $Revision: 1.16 $, $Date: 2006/07/12 15:42:56 $
 * @since 1.0
 */
public class SHLF_LIST extends Descriptor implements Serializable {

	private static final long serialVersionUID = 1L;
	private int shelfListKeyNumber;
	private int mainLibraryNumber;
	private char typeCode/*Defaults.getChar("bibliographic.shelflist.type")*/;
	private Integer amicusNumber;
	private ConfigHandler configHandler =ConfigHandler.getInstance();

	public SHLF_LIST() {
	   super();
	   setDefaultTypeCode();
	}

	public Integer getAmicusNumber() {
		return amicusNumber;
	}

	public void setAmicusNumber(Integer amicusNumber) {
		this.amicusNumber = amicusNumber;
	}

	/**
	 * @return mainLibraryNumber
	 */
	public int getMainLibraryNumber() {
		return mainLibraryNumber;
	}

	/**
	 * Getter for shelfListKeyNumber
	 * 
	 * @return shelfListKeyNumber
	 */
	public int getShelfListKeyNumber() {
		return shelfListKeyNumber;
	}

	/**
	 * Getter for shelfListTypeCode
	 * 
	 * @return shelfListTypeCode
	 */
	public char getTypeCode() {
		return typeCode;
	}

	/**
	 * Setter for mainLibraryNumber
	 * 
	 * @param i mainLibraryNumber
	 */
	public void setMainLibraryNumber(int i) {
		mainLibraryNumber = i;
	}

	/**
	 * Setter for shelfListKeyNumber
	 * 
	 * @param i shelfListKeyNumber
	 */
	public void setShelfListKeyNumber(int i) {
		shelfListKeyNumber = i;
		getKey().setHeadingNumber(i);   // to preserve Descriptor behaviour
	}
	/**
	 * Setter for shelfListTypeCode
	 * 
	 * @param c shelfListTypeCode
	 */
	public void setTypeCode(char c) {
		typeCode = c;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getAccessPointClass()
	 */
	public Class getAccessPointClass() {
		return SHLF_LIST_ACS_PNT.class;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getCategory()
	 */
	public int getCategory() {
		return 14;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return new CorrelationValues(
			(short)getTypeCode(),
			CorrelationValues.UNDEFINED,
			CorrelationValues.UNDEFINED);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.Persistence#generateNewKey()
	 */
	@Override
	public void generateNewKey() throws DataAccessException {
		//TODO add/passing "session" to descriptor

		/*
		 * The default implementation sets key.headingNumber to the new stringValue.
		 * SHLF_LIST differs from most descriptors in that the key field is not
		 * used (since there are no user views)
		 */
			super.generateNewKey();
			setShelfListKeyNumber(getKey().getHeadingNumber());
	}

	/* (non-Javadoc)
         * @see librisuite.hibernate.Descriptor#getDAO()
         */
	public HibernateUtil getDAO() {
		return new DAOShelfList();
	}
	
	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getBrowseKey()
	 */
	public String getBrowseKey() {
		String result = null;
		DAOIndexList dao = new DAOIndexList();
		try {
			result = dao.getIndexBySortFormType((short)200, (short)getTypeCode());
			if (result != null) {
				return result;
			}
			else {
				return super.getBrowseKey();
			}
		} catch (DataAccessException e) {
			return super.getBrowseKey();
		}
	}


	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getDefaultBrowseKey()
	 */
	public String getDefaultBrowseKey() {
		return "28P30";
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getNextNumberKeyFieldCode()
	 */
	public String getNextNumberKeyFieldCode() {
		return "FL";
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getReferenceClass()
	 */
	public Class getReferenceClass(Class targetClazz) {
		return null;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setTypeCode((char)v.getValue(1));
	}


	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getSortFormParameters()
	 */
	public SortFormParameters getSortFormParameters() {
		return new SortFormParameters(200, (int)getTypeCode(), 0, 0, 0);
	}

	/* (non-Javadoc)
	 * @see Descriptor#isCanTransfer()
	 */
	public boolean isCanTransfer() {
		return false;
	}

	/* (non-Javadoc)
	 * @see Descriptor#getHeadingNumberSearchIndexKey()
	 */
	public String getHeadingNumberSearchIndexKey() {
		return "232P";
	}

	
	public String getLockingEntityType() {
		return "FL";
	}

	public void setDefaultTypeCode(){
		String typCode;
		typCode= (configHandler.findValue("t_shlf_list_typ","bibliographic.shelflist.type"));
		setTypeCode(typCode.charAt(0));
	}
}
