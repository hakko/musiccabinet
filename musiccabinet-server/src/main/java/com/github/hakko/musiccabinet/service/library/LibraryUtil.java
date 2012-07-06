package com.github.hakko.musiccabinet.service.library;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;

import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.File;

public class LibraryUtil {

	public static final String FINISHED = "FINISHED";

	public static final Message<Object> FINISHED_MESSAGE = 
			MessageBuilder.withPayload(new Object())
			.setHeader(LibraryUtil.FINISHED, true).build();
	
	public static GenericMessage<DirectoryContent> msg(
			String directory, Set<String> subDirectories, Set<File> files) {
		return new GenericMessage<DirectoryContent>(
				new DirectoryContent(directory, subDirectories, files));
	}
	
	@SafeVarargs
	public static <T> Set<T> set(T... t) {
		return new HashSet<>(Arrays.asList(t));
	}
	
	public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
		Set<T> intersection = new HashSet<>();
		
		for (T t : s1) {
			if (s2.contains(t)) {
				intersection.add(t);
			}
		}
		
		return intersection;
	}
	
	public static <T> void removeIntersection(Set<T> s1, Set<T> s2) {
		Set<T> intersection = intersection(s1, s2);
		s1.removeAll(intersection);
		s2.removeAll(intersection);
	}

}