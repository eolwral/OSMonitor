package com.eolwral.osmonitor.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class WhoisUtil extends DefaultHandler {

    private boolean in_ip = false;
    private boolean in_country = false;
    private boolean in_region = false;
    private boolean in_isp = false;
    private boolean in_org = false;
    private boolean in_latitude = false;
    private boolean in_longitude = false;
    
    private WhoisUtilDataSet ParsedDataSet =  new WhoisUtilDataSet();

    public WhoisUtilDataSet getParsedData() {
        return this.ParsedDataSet;
    }

    @Override
    public void startDocument() throws SAXException { }

    @Override
    public void endDocument() throws SAXException { }

    @Override
    public void startElement(String namespaceURI, String localName,
              String qName, Attributes atts) throws SAXException {
   	 
   	 if (localName.equals("ip")) 
   		 this.in_ip = true;
   	 else if (localName.equals("countrycode")) 
   		 this.in_country = true;
   	 else if (localName.equals("region")) 
   		 this.in_region = true;
   	 else if (localName.equals("isp")) 
   		 this.in_isp = true;
   	 else if (localName.equals("org")) 
   		 this.in_org = true;
   	 else if (localName.equals("latitude")) 
   		 this.in_latitude = true;
   	 else if (localName.equals("longitude")) 
   		 this.in_longitude = true;
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
              throws SAXException {
   	 if (localName.equals("ip")) 
   		 this.in_ip = false;
   	 else if (localName.equals("countrycode")) 
   		 this.in_country = false;
   	 else if (localName.equals("region")) 
   		 this.in_region = false;
   	 else if (localName.equals("isp")) 
   		 this.in_isp = false;
   	 else if (localName.equals("org")) 
   		 this.in_org = false;
   	 else if (localName.equals("latitude")) 
   		 this.in_latitude = false;
   	 else if (localName.equals("longitude")) 
   		 this.in_longitude = false;
    }
    
   @Override
   public void characters(char ch[], int start, int length) {
       if(this.in_ip)
    	   ParsedDataSet.setIP(new String(ch, start, length));
       else if(this.in_country)
    	   ParsedDataSet.setCountry(new String(ch, start, length));
       else if(this.in_region)
    	   ParsedDataSet.setRegion(new String(ch, start, length));
       else if(this.in_isp)
    	   ParsedDataSet.setISP(new String(ch, start, length));
       else if(this.in_org)
    	   ParsedDataSet.setOrg(new String(ch, start, length));
       else if(this.in_latitude)
    	   ParsedDataSet.setLatitude(new String(ch, start, length));
       else if(this.in_longitude)
    	   ParsedDataSet.setLongitude(new String(ch, start, length));
   }
}
