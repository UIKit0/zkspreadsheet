/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.util.Arrays;

import org.zkoss.zss.model.sys.dependency.ObjectRef;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class ObjectRefImpl extends RefImpl implements ObjectRef{

	private static final long serialVersionUID = 1L;
	
	private final String[] _objectIdPath; 
	
	private final ObjectType _objType;
	
	public ObjectRefImpl(AbstractChartAdv chart,String[] objectIdPath){
		super(RefType.OBJECT,chart.getSheet().getBook().getBookName(),chart.getSheet().getSheetName(), null,-1,-1,-1,-1);
		this._objectIdPath = objectIdPath;
		_objType = ObjectType.CHART;
	}
	public ObjectRefImpl(AbstractChartAdv chart,String objectId){
		super(RefType.OBJECT,chart.getSheet().getBook().getBookName(),chart.getSheet().getSheetName(), null,-1,-1,-1,-1);
		this._objectIdPath = new String[]{objectId};
		_objType = ObjectType.CHART;
	}
	
	public ObjectRefImpl(AbstractDataValidationAdv validation,String objectId){
		super(RefType.OBJECT,validation.getSheet().getBook().getBookName(),validation.getSheet().getSheetName(), null,-1,-1,-1,-1);
		this._objectIdPath = new String[]{objectId};
		_objType = ObjectType.DATA_VALIDATION;
	}

	@Override
	public String getObjectId() {
		return _objectIdPath[_objectIdPath.length-1];
	}

	@Override
	public String[] getObjectIdPath() {
		return _objectIdPath;
	}
	@Override
	public ObjectType getObjectType() {
		return _objType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((bookName == null) ? 0 : bookName.hashCode());
		result = prime * result
				+ ((sheetName == null) ? 0 : sheetName.hashCode());
		result = prime * result + ((_objType == null) ? 0 : _objType.hashCode());
		result = prime * result + Arrays.hashCode(_objectIdPath);
		return result;
	}
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ObjectRefImpl other = (ObjectRefImpl) obj;
		if (bookName == null) {
			if (other.bookName != null)
				return false;
		} else if (!bookName.equals(other.bookName))
			return false;
		if (sheetName == null) {
			if (other.sheetName != null)
				return false;
		} else if (!sheetName.equals(other.sheetName))
			return false;
		if (_objType != other._objType)
			return false;
		if (!Arrays.equals(_objectIdPath, other._objectIdPath))
			return false;

		return true;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(_objType);
		for(String id:_objectIdPath){
			sb.append(":").append(id);
		}
		return sb.toString();
	}
	
	
}
