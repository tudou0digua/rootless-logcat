package com.tananaev.logcat;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FileManager
 * Author: chenbin
 * Time: 2019-01-24
 */
public class FileManager {

    private volatile static FileManager instance;

    private List<List<String>> array = new ArrayList<>();
    private List<String> cacheData = new ArrayList<>();
    private final byte[] lock = new byte[0];
    private volatile boolean isSaving = false;

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        AtomicInteger atomicInteger = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "KeyLog-thread-" + atomicInteger.incrementAndGet());
        }
    });

    public static FileManager getInstance() {
        if (instance == null) {
            synchronized (FileManager.class) {
                if (instance == null) {
                    instance = new FileManager();
                }
            }
        }
        return instance;
    }

    public void saveAllLogToFile() {
        addData(null, true);
    }

    public synchronized void addData(List<String> data) {
        addData(data, false);
    }

    private synchronized void addData(List<String> data, boolean justSave) {
        if (!justSave && (data == null || data.size() == 0)) {
            return;
        }

        synchronized (lock) {
            if (data != null) {
                cacheData.addAll(data);
            }
//            Log.e("TAG", "addData: cacheData.size(): "+ cacheData.size());
            if (justSave || cacheData.size() >= 1 * 10000) {
                List<String> list = new ArrayList<>(cacheData);
                addDataToArray(list);
                cacheData = new ArrayList<>();
                if (!isSaving) {
                    saveToFile();
                }
            }
        }
    }

    private synchronized void addDataToArray(List<String> data) {
        if (data != null && data.size() > 0) {
            Log.e("TAG", "addDataToArray: "+ data.size());
            array.add(data);
        }
    }

    private synchronized List<String> getTopList() {
        if (array != null && array.size() > 0) {
            Log.e("TAG", "getTopList: "+ array.size());
            return array.remove(0);
        }
        return null;
    }

    private void saveToFile() {
        final List<String> data = getTopList();
        if (data == null || data.size() == 0) {
            Log.e("TAG", "saveToFile: data == null || data.size() == 0");
            isSaving = false;
            return;
        }
        Log.e("TAG", "saveToFile: "+ data.size());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "start saveToFile: ------------");
                isSaving = true;
                PrintWriter printWriter = null;
                try {

                    File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/a_logs2");
                    if (!folder.exists()) {
                        boolean result = folder.mkdirs();
                    }
                    File file = new File(folder, new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
                            Locale.getDefault()).format(new Date()) + ".log");

                    printWriter = new PrintWriter(new FileWriter(file, true), true);
                    for (String line : data) {
                        if (line != null) {
                            printWriter.println(line);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                }

                Log.e("TAG", "end saveToFile: ------------");

                saveToFile();
            }
        });
    }

}
