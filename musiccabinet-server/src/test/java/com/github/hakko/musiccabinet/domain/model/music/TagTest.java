package com.github.hakko.musiccabinet.domain.model.music;

import junit.framework.Assert;

import org.junit.Test;

public class TagTest {

	@Test
	public void validateConstructor() {
		
		final String name = "female vocalists";
		final short count = 12;
		
		Tag tag = new Tag(name, count);
		
		Assert.assertEquals(name, tag.getName());
		Assert.assertEquals(count, tag.getCount());
		
	}
	
	@Test
	public void validateSetters() {
		
		final String name = "new weird america";
		final short count = 64;
		
		Tag tag = new Tag();
		tag.setName(name);
		tag.setCount(count);
		
		Assert.assertEquals(name, tag.getName());
		Assert.assertEquals(count, tag.getCount());
	}

	@Test
	public void validateTagEquality() {
		Tag t1 = new Tag("pop", (short) 12);
		Tag t2 = new Tag("pop", (short) 12);

		Tag t3 = new Tag("rock", (short) 12);
		Tag t4 = new Tag("pop", (short) 13);
		Tag t5 = new Tag("indie", (short) 42);
		
		Assert.assertTrue(t1.equals(t1));
		Assert.assertTrue(t1.equals(t2));

		Assert.assertFalse(t1.equals(t3));
		Assert.assertFalse(t1.equals(t4));
		Assert.assertFalse(t1.equals(t5));

		Assert.assertFalse(t1.equals(new Object()));
		Assert.assertFalse(t1 == null);
	}

}