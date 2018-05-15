package com.proyekakhir.realapps.utils;

import java.io.File;
import java.io.FileFilter;

public class ImageFileFilter implements FileFilter{

    private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
    File file;

    public ImageFileFilter(File newfile)
    {
        this.file=newfile;
    }

    @Override
    public boolean accept(File file) {
        for (String extension : okFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }
}
