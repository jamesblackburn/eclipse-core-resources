package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

class LogReader {

	protected FileInputStream in;
	protected PageStore store;
	protected byte[] b4;
	protected byte[] pageBuffer;
	
	/** 
	 * Returns the Hashmap of the modified pages.
	 */
	public static Map getModifiedPages(PageStore store) throws PageStoreException {
		LogReader reader = new LogReader(store);
		reader.open(store);
		Map modifiedPages = reader.getModifiedPages();
		reader.close();
		return modifiedPages;
	}
	
	public LogReader(PageStore store) {
		this.store = store;
		this.pageBuffer = new byte[Page.SIZE];
		this.b4 = new byte[4];
	}

	/** 
	 * Open a log for reading.
	 */
	protected void open(PageStore store) throws PageStoreException {
		String name = store.getName();
		if (!Log.exists(name)) return;
		try {
			in = new FileInputStream(Log.name(name));
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogOpenFailure);
		}
	}

	/**
	 * Closes the log.
	 */
	protected void close() {
		if (in == null) return;
		try {
			in.close();
		} catch (IOException e) {
		}
		in = null;
	}
	
	/**
	 * Returns the Hashmap of modified pages read from the log.
	 */
	protected Map getModifiedPages() throws PageStoreException {
		Map modifiedPages = new TreeMap();
		if (in == null) return modifiedPages;
		Buffer buffer = new Buffer(pageBuffer);
		Field f4 = new Field(b4);
		readBuffer(b4);
		int numberOfPages = f4.getInt();
		int recordSize = 4 + Page.SIZE;
		int bytesAvailable = bytesAvailable();
		if (bytesAvailable() != (numberOfPages * recordSize)) return modifiedPages;
		for (int i = 0; i < numberOfPages; i++) {
			readBuffer(b4);
			readBuffer(pageBuffer);
			int pageNumber = f4.getInt();
			Page page = store.getPolicy().createPage(pageNumber, pageBuffer, store);
			Integer key = new Integer(pageNumber);
			modifiedPages.put(key, page);
		}
		return modifiedPages;
	}

	public void readBuffer(byte[] buffer) throws PageStoreException {
		try {
			in.read(buffer);
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogReadFailure);
		}
	}
	
	protected int bytesAvailable() throws PageStoreException {
		try {
			return in.available();
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogReadFailure);
		}
	}
		

}
