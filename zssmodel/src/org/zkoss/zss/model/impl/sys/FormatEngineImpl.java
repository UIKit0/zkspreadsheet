/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl.sys;

import java.text.Format;
import java.util.Locale;

import org.zkoss.poi.ss.format.CellFormat;
import org.zkoss.poi.ss.usermodel.BuiltinFormats;
import org.zkoss.poi.ss.usermodel.DataFormatter;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.poi.ss.util.NumberToTextConverter;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.impl.ReadOnlyRichTextImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.format.FormatContext;
import org.zkoss.zss.model.sys.format.FormatEngine;
import org.zkoss.zss.model.sys.format.FormatResult;
/**
 * 
 * @author Hawk
 * @since 3.5.0
 */
public class FormatEngineImpl implements FormatEngine {

	@Override
	public FormatResult format(SCell cell, FormatContext context){
		CellType type = cell.getType();
		if(type == CellType.FORMULA){
			type = cell.getFormulaResultType();
		}
		String format = cell.getCellStyle().getDataFormat();
		if(type==CellType.BLANK){
			return new FormatResultImpl(cell.getStringValue(),null);//no color as well 
		} else if(type == CellType.STRING){
			//handling as text/rich text
			if(cell.isRichTextValue()){
				return new FormatResultImpl(new ReadOnlyRichTextImpl(cell.getRichTextValue()));
			}
			if(SCellStyle.FORMAT_GENERAL.equals(format)){ // return the direct result if the format is general
				return new FormatResultImpl(cell.getStringValue(),null);//no color as well
			}
		}else if(type==CellType.ERROR){
			return new FormatResultImpl(cell.getErrorValue().getErrorString(),null);//no color as well
		}
		return format0(cell.getCellStyle().getDataFormat(), cell.getCellStyle().isDirectDataFormat(), cell.getValue(), context);
	}
	@Override
	public FormatResult format(String format, Object value, FormatContext context){
		return format0(format,false,value,context);
	}
	private FormatResult format0(String format, boolean direct,Object value, FormatContext context){
		ZssContext old = ZssContext.getThreadLocal();
		try{
			ZssContext zssContext = old==null?new ZssContext(context.getLocale(),-1): new ZssContext(context.getLocale(),old.getTwoDigitYearUpperBound());
			ZssContext.setThreadLocal(zssContext);
			
			//Have to transfer format that depends on locale
			//for example, m/d/yyyy will transfer to yyyy/m/d in TW
			if(!direct){
				int i = BuiltinFormats.getBuiltinFormat(format);
				if(i>=0){
					format = BuiltinFormats.getBuiltinFormat(i, context.getLocale());
				}
			}
			format = normalizeFormat(format);
			
			CellFormat formatter = CellFormat.getInstance(format, context.getLocale());
			Format javaFormat = null;
			if(value instanceof Number){//provide format object for further use
				javaFormat = DataFormatter.getJavaFormat(((Number)value).doubleValue(),format,context.getLocale());
			}
			boolean dateFromatted = false;
			if(value instanceof Double && formatter.isApplicableDateFormat((Double)value)){
				value = EngineFactory.getInstance().getCalendarUtil().doubleValueToDate((Double)value);
				dateFromatted = true;
			}
			return new FormatResultImpl(formatter.apply(value), javaFormat, dateFromatted);
		}finally{
			ZssContext.setThreadLocal(old);
		}
	}
	
	@Override
	public String getLocalizedFormat(String format, FormatContext context){
		return getFormat0(format,false,context);
	}
	@Override
	public String getFormat(SCell cell, FormatContext context){
		return getFormat0(cell.getCellStyle().getDataFormat(), cell.getCellStyle().isDirectDataFormat(),context);
	}
	
	private String getFormat0(String format, boolean direct, FormatContext context){
		ZssContext old = ZssContext.getThreadLocal();
		try{
			ZssContext zssContext = old==null?new ZssContext(context.getLocale(),-1): new ZssContext(context.getLocale(),old.getTwoDigitYearUpperBound());
			ZssContext.setThreadLocal(zssContext);
			//Have to transfer format that depends on locale
			//for example, m/d/yyyy will transfer to yyyy/m/d in TW
			if(!direct){
				int i = BuiltinFormats.getBuiltinFormat(format);
				if(i>=0){
					format = BuiltinFormats.getBuiltinFormat(i, context.getLocale());
				}
			}
			format = normalizeFormat(format);
			return format;
		}finally{
			ZssContext.setThreadLocal(old);
		}
	}	
	
	private boolean isDateFormatted(String format, Double value,Locale locale) {
		CellFormat formatter = CellFormat.getInstance(format, locale);
		if(formatter.isApplicableDateFormat(value)){
			return true;
		}
		return false;
	}
	
	@Override
	public String getEditText(SCell cell, FormatContext context) {
		ZssContext old = ZssContext.getThreadLocal();
		try{
			ZssContext zssContext = old==null?new ZssContext(context.getLocale(),-1): new ZssContext(context.getLocale(),old.getTwoDigitYearUpperBound());
			ZssContext.setThreadLocal(zssContext);
			
			final Locale locale = context.getLocale(); //ZSS-68
			
			final CellType cellType = cell.getType();
			switch(cellType) {
			case BLANK:
				return "";
			case BOOLEAN:
				return cell.getBooleanValue() ? "TRUE" : "FALSE";
			case ERROR:
				return cell.getErrorValue().getErrorString();
			case FORMULA:
				return "="+cell.getFormulaValue();
			case NUMBER:
				final double val = cell.getNumberValue().doubleValue();
				
				if (isDateFormatted(cell.getCellStyle().getDataFormat(),val,locale)) { //ZSS-15 edit date cells doesn't work
					String formatString = null;
					if (Math.abs(val) < 1) { //time only
						formatString = getDateFormatString(TIME, locale);//"h:mm:ss AM/PM"; //ZSS-67
						if (formatString == null) { //ZSS-76
							formatString = "h:mm:ss AM/PM";
						}
					} else if (isInteger(Double.valueOf(val))) { //date only
						formatString = getDateFormatString(DATE, locale); //"mm/dd/yyyy"; //ZSS-67
						if (formatString == null) { //ZSS-76
							formatString = "mm/dd/yyyy";
						}
					} else { //date + time
						formatString = getDateFormatString(DATE_TIME, locale);//"mm/dd/yyyy h:mm:ss AM/PM" //ZSS-67
						if (formatString == null) { //ZSS-76
							formatString = "mm/dd/yyyy h:mm:ss AM/PM";
						}
					}
					final boolean date1904 = false; // always false in new model
					return new DataFormatter(locale, false).formatRawCellContents(val, -1, formatString, date1904); //ZSS-68
				} else {
					return NumberToTextConverter.toText(val);
				}
			case STRING:
				return cell.getStringValue(); 
			}
			
		}finally{
			ZssContext.setThreadLocal(old);
		}
		return "";
	}
	
	private static String normalizeFormat(String format){
		//zss-510
		if(format==null || "".equals(format.trim())){
			format = SCellStyle.FORMAT_GENERAL;
		}
		return format;
	}
	
	//ZSS-67
	//date format type
	private static final int TIME = 0x13;
	private static final int DATE = 0x0e;
	private static final int DATE_TIME = 0x100;
	
	private static String getDateFormatString(int formatType, Locale locale) {
		return BuiltinFormats.getBuiltinFormat(formatType, locale);
	}

	private static boolean isInteger(Object value) {
		if (value instanceof Number) {
			return ((Number)value).intValue() ==  ((Number)value).doubleValue();
		}
		return false;
	}
}
