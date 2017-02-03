package com.shutterfly.main;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.shutterfly.model.Customer;
import com.shutterfly.model.Image;
import com.shutterfly.model.Order;
import com.shutterfly.model.SiteVisit;

 /**
 * Main Class that ingests event data(json array from .txt file) and implements analytic method
   that the top x customers with the highest Simple Lifetime Value from data 
 * Creation date: (02/01/2017)
 * @author: Swapnil Pharate
 * 
**/
 

public class MainClass {
   
	//static class level variables
	public static final String INPUT_FILE="C:\\shutterfly\\input.txt";
	public static final String OUTPUT_FILE="C:\\shutterfly\\output.txt";
	public static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SS");
	public static List eventList= new ArrayList<>();
	
	
	public static void main(String[] args) throws IOException,ParseException {
			
		BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));
		List<Customer> topCustomers = new ArrayList<Customer>();
		String event = "";
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = reader.readLine();
            
		    while (line != null) {
		        sb.append(line);
		        sb.append("\n");
		        line = reader.readLine();
		    }
		    event = sb.toString();   
		    ingest(event,eventList);    //calls ingest method to load data for further processing
		    topCustomers=topXSimpleLTVCustomers(100,eventList);  //get top X customers with high Lifetime value   
		    writeTopCustomersToFile(topCustomers);
		    System.out.println("Process Completed");
		    
		    
		} finally {
		    reader.close();
		}
	}

	/*
	 * This method returns top x customers with highest LTV
	 */
	private static List<Customer> topXSimpleLTVCustomers(int x, List eventList) {
		// TODO Auto-generated method stub
		
		float noOfSiteVisitPerWeek;
		double customerExpendituresPerVisit;
		double totalCustomerExpenditure;
		
		List<Customer> topxCustomers = new ArrayList<Customer>();
		List<SiteVisit> siteVisitByCustomer = new ArrayList<SiteVisit>();
		List<Customer> customerList = new ArrayList<Customer>();
	    List<SiteVisit> siteVisitList = new ArrayList<SiteVisit>();
	    List<Image> imageList = new ArrayList<Image>();
	    List<Order> orderList = new ArrayList<Order>();
	    
	    try{
	    //add objects from eventlist to different list of different event types	
	    for(int i =0;i<eventList.size();i++)
	    {
	    	if(eventList.get(i) instanceof Customer)
	    	{
	    		customerList.add((Customer) eventList.get(i));
	    		
	    	}
	    	
	    	else if(eventList.get(i) instanceof SiteVisit)
	    	{
	    		siteVisitList.add((SiteVisit) eventList.get(i));
	    		
	    	}
	    	
	    	else if(eventList.get(i) instanceof Order)
	    	{
	    		orderList.add((Order) eventList.get(i));
	    		
	    	}
	    	
	    	else if(eventList.get(i) instanceof Image)
	    	{
	    		imageList.add((Image) eventList.get(i));
	    		
	    	}
	    			    	
	    }
	    	
	   //iterate though customer list to find each customers LTV 
	    for(Customer c:customerList)
	    {
	    	Date dateMin = null;
	    	Date dateMax = null ;
	    	float diff;
	    	double totalVisits=1;
	    	siteVisitByCustomer = getSiteVisitsByCustomer(c.getKey(),siteVisitList);
	    	if(!siteVisitByCustomer.isEmpty())
	    	{
		   	  dateMin = formatter.parse(siteVisitByCustomer.get(0).getEventTime());
			  dateMax = formatter.parse(siteVisitByCustomer.get(siteVisitByCustomer.size()-1).getEventTime());
	    	}
	    	
	    	if(dateMin==null || dateMax==null)
	    	{
	    		diff=0;
	    	}
	    	else
	    	{
	    		diff = dateMax.getTime() - dateMin.getTime();
	    		totalVisits=siteVisitByCustomer.size();
	    	}
	    		
			float weeks;
			if(diff!=0)
			{
				weeks = (diff / (7 * 24 * 60 * 60 * 1000 ));
				
				if(weeks< 0)
				{
					weeks=1;
				}
			}
			else
			{
				weeks=1;
				
			}
			
			
			double numberOfSiteVisitsPerWeek = totalVisits/weeks;
				
			
	    	totalCustomerExpenditure =totalCustomerExpenditure(c.getKey(),orderList);
	    	customerExpendituresPerVisit = totalCustomerExpenditure / totalVisits;
	    	
	    	Double customerLTV = 52*(customerExpendituresPerVisit*numberOfSiteVisitsPerWeek)*10; 
	    	c.setCustomerLTV(customerLTV);
	   		topxCustomers.add(c);
	   		   	 
	      }
	    
	      //Sort in descending order of Customer LTV to get top X customers
	      Collections.sort(topxCustomers,new Comparator<Customer>() {
	    	    @Override
	    	    public int compare(Customer a, Customer b) {
	    	        return b.getCustomerLTV().compareTo(a.getCustomerLTV());
	    	    }
	    	});
	    
	    }  catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return topxCustomers.subList(0, x);
	}

	/*
	 * This method writes top customers with LTV to output file
	 */
	private static void writeTopCustomersToFile(List<Customer> topxCustomers) {
		// TODO Auto-generated method stub
		int count = 0;
		File fout=null;
		FileOutputStream fos=null;
		OutputStreamWriter out=null;
		
		try {

			fout = new File(OUTPUT_FILE);
			fos = new FileOutputStream(fout);
			out = new OutputStreamWriter(fos);
			BufferedWriter oFile = new BufferedWriter(out);

			oFile.write("CustomerName "+"     "+" Customer LTV");
			oFile.write("\r\n");
			for (Customer cust:topxCustomers) {
				
				oFile.write(cust.getLastName() +"                     "+ cust.getCustomerLTV());
				oFile.write("\r\n");
				
			}
			oFile.flush();
			oFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 This ingest method reads json array data and stores in list of objects of different types
	 */
	private static void ingest(String event, List eventList) {
		// TODO Auto-generated method stub
	 
		try {
			
	  
		 JsonReader jsonReader = Json.createReader(new StringReader(event));
		 JsonArray array = jsonReader.readArray();
		 
	    for(int i=0; i<array.size(); i++){ // loop through the events
	   
	    	JsonObject temp = array.getJsonObject(i);
	             	
	        if(temp.getString("type").equals("CUSTOMER"))
	        {
	        	Customer customer = new Customer();
	        	customer.setType(temp.getString("type"));
	        	customer.setVerb(temp.getString("verb"));
	        	customer.setKey(temp.getString("key"));
	        	customer.setEventTime(temp.getString("event_time"));
	        	customer.setLastName(temp.getString("last_name"));
	        	customer.setAdrCity(temp.getString("adr_city"));
	        	customer.setAdrState(temp.getString("adr_state"));
	        	
	        	eventList.add(customer); //start setting the values for your node...
	        }
	        
	        else if (temp.getString("type").equals("SITE_VISIT"))
	        {
	        	SiteVisit siteVisit = new SiteVisit();
	        	siteVisit.setType(temp.getString("type"));
	        	siteVisit.setVerb(temp.getString("verb"));
	        	siteVisit.setKey(temp.getString("key"));
	        	siteVisit.setEventTime(temp.getString("event_time"));
	        	siteVisit.setCustomerId(temp.getString("customer_id"));
	        	siteVisit.setEventTime(temp.getString("event_time"));
	        	eventList.add(siteVisit);
	        	
	        }
	        
	        else if (temp.getString("type").equals("ORDER"))
	        {
	        	Order order = new Order();
	        	order.setType(temp.getString("type"));
	        	order.setVerb(temp.getString("verb"));
	        	order.setKey(temp.getString("key"));
	        	order.setEventTime(temp.getString("event_time"));
	        	order.setCustomerId(temp.getString("customer_id"));
	        	order.setTotalAmount(temp.getString("total_amount"));
	        	eventList.add(order);
	        	
	        }
	        
	        else if(temp.getString("type").equals("IMAGE"))
	        {
	        	Image image = new Image();
	        	image.setType(temp.getString("type"));
	        	image.setVerb(temp.getString("verb"));
	        	image.setKey(temp.getString("key"));
	        	image.setEventTime(temp.getString("event_time"));
	        	image.setCustomerId(temp.getString("customer_id"));
	        	image.setCameraMake(temp.getString("camera_make"));
	        	image.setCameraModel(temp.getString("camera_model"));
	        	eventList.add(image);
	        	
	        	
	        }
	        
	    }
		} catch (JsonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 This method returns total Expenditure(amout) by visit by particular customer
	 */
	private static double totalCustomerExpenditure(String customerId, List<Order> orders) {
		// TODO Auto-generated method stub
		List<Order> ordersList = new ArrayList<Order>();
		double ordersTotalAmount = 0;
		
		for(Order o:orders)
		{
			
			if(o.getCustomerId().equals(customerId))
			{
				ordersTotalAmount = ordersTotalAmount+ Double.parseDouble(o.getTotalAmount().substring(0,o.getTotalAmount().length()-4));	
				
			}
			
		}
		
		return ordersTotalAmount;
		
	}

	/*
	 This method returns List of site visit by particular customer
	 */
	private static List<SiteVisit> getSiteVisitsByCustomer(String customerId, List<SiteVisit> siteVisitList) throws ParseException {
		// TODO Auto-generated method stub
		
		List<SiteVisit> siteVisits = new ArrayList<SiteVisit>();
		
		
		for(SiteVisit sv:siteVisitList)
		{
			
			if(sv.getCustomerId().equals(customerId))
			{
				siteVisits.add(sv);				
			}
			
			
		}
		
		//Sort site visit collection by dates 
		Collections.sort(siteVisits, new Comparator<SiteVisit>(){
			   public int compare(SiteVisit o1, SiteVisit o2){
				   Date date1=null;
				   Date date2=null;
				    try {
				    	date1 = formatter.parse(o1.getEventTime());
						date2 = formatter.parse(o2.getEventTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
					return date1.compareTo(date2);
				
			   }
			});
		
		
		return siteVisits;
		
	}
	
}
