package com.github.hakko.musiccabinet.dao.util;

import java.util.Random;

public class DirectoryUtilTest {

	public static void main(String[] args) {
		Random rnd = new Random();
		
		for (int i = 0; i < 4000; i++) {
			new java.io.File("C:\\Temp\\Volumes\\iTunes\\iTunes Media\\Music\\artist" + i).mkdirs();
			for (int j = 0; j < 1 + rnd.nextInt(8); j++) {
				new java.io.File("C:\\Temp\\Volumes\\iTunes\\iTunes Media\\Music\\artist" + i + "\\album" + j).mkdirs();
			}
		}
	}
}
