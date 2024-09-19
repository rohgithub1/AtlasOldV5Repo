package com.cci.rest.service.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class AtlasUtility {

	public static String getFormatedDateTimeString(Date date)
	{
		String formatedDateString = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			//Check for null value of date. 
			if(date != null)
			{
				formatedDateString = sdf.format(date);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return formatedDateString;
		
	}
	
	
	public static Date getFormatedSqlDateTime(String dateString)
	{
		System.out.println(dateString);
		Date formatedDate = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date tempDate = sdf.parse(dateString);
			System.out.println(tempDate);
			sdf.applyPattern("MM/dd/yyyy HH:mm:ss");
			dateString = sdf.format(tempDate);
		System.out.println(dateString);
			formatedDate = sdf.parse(dateString);
			
			System.out.println(formatedDate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return formatedDate;
	}
	
//	FOR SCHEDULER 
	public static Date getFormatedSqlDateTimeScheduler(String dateString)
	{
		System.out.println(dateString);
		Date formatedDate = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date tempDate = sdf.parse(dateString);
			System.out.println(tempDate);
			sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
			dateString = sdf.format(tempDate);
		System.out.println(dateString);
			formatedDate = sdf.parse(dateString);
			
			System.out.println(formatedDate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return formatedDate;
	}

	

}
