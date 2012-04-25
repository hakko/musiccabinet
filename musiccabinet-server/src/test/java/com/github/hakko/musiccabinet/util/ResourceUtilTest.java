package com.github.hakko.musiccabinet.util;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

public class ResourceUtilTest {
	
	@Test (expected = IllegalArgumentException.class)
	public void detectNonExistingFile() {
		new ResourceUtil("resourceutil/nonexisting.xml");
	}

	@Test (expected = IllegalArgumentException.class)
	public void detectNonReadableFile() throws IOException {
		ResourceUtil resourceUtil = new ResourceUtil("resourceutil/small.xml");
		resourceUtil.setInputStream(mock(InputStream.class));
		
		resourceUtil.getContent();
	}

	@Test
	// if this test fails, check file encoding!
	// in a hex editor, it should say: 
	//  <  d  a  t  a  >            å     ä     ö         <  /  d  a  t  a  >
	// 3C 64 61 74 61 3E        C3 A5 C3 A4 C3 B6        3C 2F 64 61 74 61 3E
	public void readFileFromResource() {
		String xmlData = new ResourceUtil("resourceutil/small.xml").getContent();
		Assert.assertEquals(xmlData, "<data>åäö</data>");
	}

}