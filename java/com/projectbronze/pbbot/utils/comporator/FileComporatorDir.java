package com.projectbronze.pbbot.utils.comporator;

import java.io.File;
import java.util.Comparator;

public class FileComporatorDir implements Comparator<File>{

	public static FileComporatorDir instance = new FileComporatorDir();
	@Override
	public int compare(File file1, File file2) {
		if(file1.isDirectory() && !file2.isDirectory())
		{
			return -1;
		}
		if(!file2.isDirectory() && file2.isDirectory())
		{
			return 1;
		}
		return file1.compareTo(file2);
	}

	
}
