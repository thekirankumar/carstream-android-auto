package com.thekirankumar.youtubeauto.exoplayer;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * Created by kiran.kumar on 05/02/18.
 */

public class PlayerQueue implements MediaQueue {
    public static final String FILE_PREFIX = "file://";
    private final String startingItemPath;
    private File[] currentQueue;
    private int currentIndex = 0;

    public PlayerQueue(String startingItemPath) {
        if (startingItemPath.startsWith(FILE_PREFIX)) {
            startingItemPath = startingItemPath.substring(FILE_PREFIX.length());
        }
        this.startingItemPath = startingItemPath;
        buildQueue();
    }

    public int currentIndex() {
        return currentIndex;
    }

    public File[] getCurrentQueue() {
        return currentQueue;
    }

    private void buildQueue() {
        File startingItem = new File(startingItemPath);
        File parentDir = new File(startingItem.getParent());
        File[] filesList = parentDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && !file.getName().toLowerCase().endsWith(".jpg");
            }
        });

        Arrays.sort(filesList);
        for (int i = 0; i < filesList.length; i++) {
            File file = filesList[i];
            if (file.equals(startingItem)) {
                currentIndex = i;
            }
        }
        this.currentQueue = filesList;
    }

    public String current() {
        return currentQueue[currentIndex].getPath();
    }

    @Override
    public void next() {
        currentIndex++;
    }

    @Override
    public void previous() {
        currentIndex--;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < currentQueue.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    @Override
    public void resetPosition(int position) {
        currentIndex = position;
    }


}
