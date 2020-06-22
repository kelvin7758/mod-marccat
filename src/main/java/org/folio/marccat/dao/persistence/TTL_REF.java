package org.folio.marccat.dao.persistence;

import org.folio.marccat.dao.DescriptorDAO;
import org.folio.marccat.dao.TitleDescriptorDAO;

/**
 * The different cross references for the title
 *
 * @author paulm
 * @author carment
 */
public class TTL_REF extends REF {
  /**
   * The DAO.
   */
  public DescriptorDAO getTargetDAO() {
    return new TitleDescriptorDAO();
  }

}
