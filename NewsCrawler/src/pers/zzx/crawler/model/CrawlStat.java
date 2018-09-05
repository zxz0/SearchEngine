package pers.zzx.crawler.model;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class CrawlStat {
    private Hashtable<String, Integer> statusCodeCount;	// String: including the description. multi-thread safe (Hashmap not). not in increasing order, though
    private Hashtable<String, Integer> countentTypeCount;
    private Hashtable<String, Integer> fileSizeCategoryCount;
    private List<Site> results;
    private List<String> outUrls;	// all ourUrls (String), include the ones in results
    private int numProcessedPage;
    
    public CrawlStat() {
        this.statusCodeCount = new Hashtable<String, Integer>();
        this.countentTypeCount = new Hashtable<String, Integer>();
        this.fileSizeCategoryCount = new Hashtable<String, Integer>();
        
        this.fileSizeCategoryCount.put("< 1KB", 0);
        this.fileSizeCategoryCount.put("1KB ~ <10KB", 0);
        this.fileSizeCategoryCount.put("10KB ~ <100KB", 0);
        this.fileSizeCategoryCount.put("100KB ~ <1MB", 0);
        this.fileSizeCategoryCount.put(">= 1MB", 0);
        
        this.results = new LinkedList<Site>();
        this.outUrls = new LinkedList<String>();
        numProcessedPage = 0;
    }
    
    public void addOneToStatusCodeCount(String statusCodeAndDescription) {
    	// if key not exist, put 1
    	this.statusCodeCount.put(statusCodeAndDescription, statusCodeCount.containsKey(statusCodeAndDescription) ? (statusCodeCount.get(statusCodeAndDescription) + 1) : 1);
    }
    
    public void addOneToCountentTypeCount(String countentType) {
    	this.countentTypeCount.put(countentType, countentTypeCount.containsKey(countentType) ? (countentTypeCount.get(countentType) + 1) : 1);
    }
    
    public void addOneToFileSizeCategoryCount(String fileSizeCategorye) {
    	this.fileSizeCategoryCount.put(fileSizeCategorye, fileSizeCategoryCount.get(fileSizeCategorye) + 1);
    	// changing the value for an existing key won't change the iteration order of the Map (which is the order in which the keys were first inserted to the Map).
    }
    
    public Hashtable<String, Integer> getStatusCodeCount() {
    	return this.statusCodeCount;
    }
    
    public Hashtable<String, Integer> getCountentTypeCount() {
    	return this.countentTypeCount;
    }
    
    public Hashtable<String, Integer> getFileSizeCategoryCount() {
    	return this.fileSizeCategoryCount;
    }
    
    public void addResult(Site site) {
    	this.results.add(site);
    }
    
    public List<Site> getResults() {
    	return this.results;
    }
    
    public void addUrl(String url) {
    	this.outUrls.add(url);
    }
    
    public List<String> getUrls() {
    	return this.outUrls;
    }
    
    public int getNumProcessedPage() {
    	return this.numProcessedPage;
    }
    
    public void addOneToProcessedPage() {
    	this.numProcessedPage++;
    }
    
    public int getTotalLinks() {
    	return this.outUrls.size();
    }
}