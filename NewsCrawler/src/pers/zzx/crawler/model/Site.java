package pers.zzx.crawler.model;

public class Site {
    private String url;
    private int httpStatusCode;
    private long size;
    private int outlinkNum;
    private String contentType;
    private String hashedName;

	public Site(String url, int httpStatusCode) {
    	this.url = url;
        this.httpStatusCode = httpStatusCode;
    }

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getHttpStatusCode() {
		return this.httpStatusCode;
	}

	public long getSize() {
		return this.size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getOutlinkNum() {
		return this.outlinkNum;
	}

	public void setOutlinkNum(int outlinkNum) {
		this.outlinkNum = outlinkNum;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getHashedName() {
		return this.hashedName;
	}

	public void setHashedName(String hashedName) {
		this.hashedName = hashedName;
	}
}
