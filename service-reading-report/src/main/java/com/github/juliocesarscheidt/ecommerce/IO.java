package com.github.juliocesarscheidt.ecommerce;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class IO {
	
	public static void copyTo(File source, File target) {
		try {
			target.getParentFile().mkdirs();
			System.out.println("Copying file from " + source.toString() + " to " + target.toString());
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void appendToFile(File target, String reportData) {
		try {
			System.out.println("Appending to file " + target.toString());
			Files.write(target.toPath(), reportData.getBytes(), StandardOpenOption.APPEND);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
