package com.github.hakko.musiccabinet.util;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class StringUtil {

	private String str;
	
	public StringUtil(String str) {
		this.str = str;
	}
	
	public InputStream getInputStream() throws ApplicationException {
		InputStream is;
		try {
			is = new ByteArrayInputStream(str.getBytes(UTF8));
		} catch (UnsupportedEncodingException e) {
			throw new ApplicationException("UTF-8 encoding not supported!", e);
		}
		return is;
	}

}