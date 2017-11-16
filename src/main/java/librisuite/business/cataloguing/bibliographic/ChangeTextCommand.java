/*
 * (c) LibriCore
 * 
 * Created on Aug 13, 2004
 * 
 * ChangeTextCommand.java
 */
package librisuite.business.cataloguing.bibliographic;

import librisuite.bean.cataloguing.common.EditBean;

import com.libricore.librisuite.common.Command;
import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.8 $, $Date: 2006/01/05 13:25:58 $
 * @since 1.0
 */
public class ChangeTextCommand implements Command {
	private VariableField tag;
	private StringText stringText;
	private StringText oldStringText;
	private EditBean bean;
	private int oldUpdateStatus;

	public ChangeTextCommand(EditBean bean, VariableField t, StringText s){
		setBean(bean);
		setTag(t);
		setStringText(s);
		setOldStringText(t.getStringText());
		setOldUpdateStatus(t.getUpdateStatus());
		
	}
	/* (non-Javadoc)
	 * @see com.libricore.librisuite.common.Command#execute()
	 */
	public void execute() {
		getTag().setStringText(getStringText());
		getTag().markChanged();
	}

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.common.Command#reExecute()
	 */
	public void reExecute() {
		execute();
		int i = getBean().getCatalogItem().getTags().indexOf(getTag());
		getBean().setTagIndex(i);
	}

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.common.Command#unExecute()
	 */
	public void unExecute() {
		getTag().setStringText(getOldStringText());
		int i = getBean().getCatalogItem().getTags().indexOf(getTag());
		getBean().setTagIndex(i);
	}

	/**
	 * 
	 */
	public StringText getOldStringText() {
		return oldStringText;
	}

	/**
	 * 
	 */
	public StringText getStringText() {
		return stringText;
	}

	/**
	 * 
	 */
	public VariableField getTag() {
		return tag;
	}

	/**
	 * 
	 */
	public void setOldStringText(StringText text) {
		oldStringText = text;
	}

	/**
	 * 
	 */
	public void setStringText(StringText text) {
		stringText = text;
	}

	/**
	 * 
	 */
	public void setTag(VariableField tag) {
		this.tag = tag;
	}

	/**
	 * 
	 */
	public EditBean getBean() {
		return bean;
	}

	/**
	 * 
	 */
	public void setBean(EditBean bean) {
		this.bean = bean;
	}

	/**
	 * 
	 */
	public int getOldUpdateStatus() {
		return oldUpdateStatus;
	}

	/**
	 * 
	 */
	public void setOldUpdateStatus(int i) {
		oldUpdateStatus = i;
	}

}
