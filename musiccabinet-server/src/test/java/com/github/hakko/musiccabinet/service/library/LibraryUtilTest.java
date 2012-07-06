package com.github.hakko.musiccabinet.service.library;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class LibraryUtilTest {
	
	@Test
	public void createsSet() {
		Set<Integer> s = LibraryUtil.set(1, 2, 3);
		
		Assert.assertEquals(3, s.size());
		Assert.assertTrue(s.contains(1));
		Assert.assertTrue(s.contains(2));
		Assert.assertTrue(s.contains(3));
	}
	
	@Test
	public void calculatesIntersection() {
		Set<Integer> s1 = new HashSet<>(Arrays.asList(1, 2, 3, 4));
		Set<Integer> s2 = new HashSet<>(Arrays.asList(3, 4, 5, 6));
		
		Set<Integer> intersection = LibraryUtil.intersection(s1, s2);
		
		Assert.assertEquals(LibraryUtil.set(3, 4), intersection);
	}

	@Test
	public void removesIntersection() {
		Set<Integer> s1 = new HashSet<>(Arrays.asList(1, 2, 3));
		Set<Integer> s2 = new HashSet<>(Arrays.asList(3, 4, 5));
		
		LibraryUtil.removeIntersection(s1, s2);
		
		Assert.assertEquals(LibraryUtil.set(1, 2), s1);
		Assert.assertEquals(LibraryUtil.set(4, 5), s2);
	}

}