package com.eolwral.osmonitor.util;

public class WhoisUtilDataSet 
{
    private String ip = "N/A";
    private String country = "N/A";
    private String region = "N/A";
    private String isp = "N/A";
    private String org = "N/A";
    private String latitude = "N/A";
    private String longitude = "N/A";

	public void setIP(String newip)
	{
		ip = newip;
	}

	public String getIP()
	{
		return ip;
	}

	public void setCountry(String newcountry)
	{
		country = newcountry;
	}

	public String getCountry()
	{
		return country;
	}

	public void setRegion(String newregion)
	{
		region = newregion;
	}

	public String getRegion()
	{
		return region;
	}

	public void setISP(String newisp)
	{
		isp = newisp;
	}

	public String getISP()
	{
		return isp;
	}

	public void setOrg(String neworg)
	{
		org = neworg;
	}

	public String getOrg()
	{
		return org;
	}

	public void setLatitude(String newlatitude)
	{
		latitude = newlatitude;
	}

	public String getLatitude()
	{
		return latitude;
	}

	public void setLongitude(String newlongitude)
	{
		longitude = newlongitude;
	}

	public String getLongitude()
	{
		return longitude;
	}
	
	public String toString()
	{
		StringBuilder whoisInfo = new StringBuilder();
		whoisInfo.append("<b>IP:</b> "+ip+"<br/>");
		whoisInfo.append("<b>Country:</b> "+country+"<br/>");
		whoisInfo.append("<b>Region:</b> "+region+"<br/>");
		whoisInfo.append("<b>ISP:</b> "+isp+"<br/>");
		whoisInfo.append("<b>Org:</b> "+org+"<br/>");
		whoisInfo.append("<b>Latitude:</b> "+latitude+"<br/>");
		whoisInfo.append("<b>Longitude:</b> "+longitude+"<br />");
		return whoisInfo.toString(); 
	}
	
	public float getMapnLatitude()
	{
		return Float.parseFloat(latitude);
	}
	
	public float getMapLongtiude()
	{
		return Float.parseFloat(longitude);
	}
}
