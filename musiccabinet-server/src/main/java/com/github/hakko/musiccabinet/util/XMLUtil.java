package com.github.hakko.musiccabinet.util;

public class XMLUtil {

	public static String removeISOControlChars(String input) {
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isISOControl(c) && c != 0x09
					&& c != 0x0A && c != 0x0D) {
				chars[i] = ' ';
			}
		}
		return new String(chars);
	}

}