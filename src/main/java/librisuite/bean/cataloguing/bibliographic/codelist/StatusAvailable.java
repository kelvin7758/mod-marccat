/*
 * (c) LibriCore
 * 
 * $Author: wimc $
 * $Date: 2005/03/10 09:45:07 $
 * $Locker:  $
 * $Name:  $
 * $Revision: 1.1 $
 * $Source: /source/LibriSuite/src/librisuite/bean/cataloguing/bibliographic/codelist/MusicMaterialType.java,v $
 * $State: Exp $
 */
package librisuite.bean.cataloguing.bibliographic.codelist;

import librisuite.hibernate.T_STS_AVBLE;

/**
 * @author Carmen
 * @version $Revision: 1.1 $, $Date: 2005/03/10 09:45:07 $
 * @since 1.0
 */
public class StatusAvailable extends CodeListBean {

	public StatusAvailable() {
		super(T_STS_AVBLE.class);
	}

}
