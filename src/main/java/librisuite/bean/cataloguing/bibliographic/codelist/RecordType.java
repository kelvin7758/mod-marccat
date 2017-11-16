/*
 * (c) LibriCore
 * 
 * Created on Dec 2, 2005
 * 
 * RecordType.java
 */
package librisuite.bean.cataloguing.bibliographic.codelist;

import librisuite.hibernate.T_AUT_REC_TYP;

/**
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2005/12/12 12:54:36 $
 * @since 1.0
 */
public class RecordType extends CodeListBean {

	/**
	 * Class constructor
	 *
	 * @param clazz
	 * @since 1.0
	 */
	public RecordType() {
		super(T_AUT_REC_TYP.class);
	}

}
