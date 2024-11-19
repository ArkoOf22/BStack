package com.example.BrowserStack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.RandomAccessFile;

@Service
public class FileWatcherService {

    private final static String FILE_NAME="log.txt";
    private final static String READ_MODE="r";
    private final static String DESTINATION="/topic/lag";
    private long offset;
    private final RandomAccessFile randomAccessFile;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public FileWatcherService() throws IOException {
        randomAccessFile = new RandomAccessFile(FILE_NAME, READ_MODE);
        offset=initialOffset();
    }
    public void sendUpdates() throws IOException {
        long fileLength=randomAccessFile.length();

        randomAccessFile.seek(offset);

        while (randomAccessFile.getFilePointer() < fileLength) {
            String latestData=randomAccessFile.readLine();
            String payLoad = "{\"content\":\""+latestData+"\"}";

            messagingTemplate.convertAndSend(DESTINATION,payLoad);
        }
        offset=fileLength;
    }

    private long initialOffset() throws IOException {
        int lines=0;

        while (randomAccessFile.readLine()!=null) {
            lines++;
        }
        if(lines>10) {
            offset=lines-10;
        }
        return offset;
    }
}
