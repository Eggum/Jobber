package com.example.jobber;

import android.content.pm.PackageInstaller;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

enum serverCommands
{
    ERROR(-1),
    NONE(0),
    JOB_LIST(1),
    UPLOAD_JOB(2),
    DOWNLOAD_JOB(3),
    UPLOAD_FILE(4),
    DOWNLOAD_FILE(5),
    ARCHIVE_JOB(20),
    DELETE_JOB(255);

    private int value;
    serverCommands(int i) {
        value = i;
    }

    int get_int(){
       return value;
    }
}

interface socketCallback
{
    void sessionFinished(int i);
    void threadFinished();
    void onException(Exception e);
}

class SocketCommunicator
{
    class SocketTask
    {
        serverCommands cmd;
        byte[] outData;
        byte[] inData;
        File file;

        SocketTask(serverCommands c, byte[] d, File f)
        {
            cmd = c;
            outData = d;
            file = f;
        }

        public serverCommands getCmd(){
            return cmd;
        }

        public byte[] getOutData() {
            return outData;
        }

        public byte[] getInData() {
            return inData;
        }

        public File getFile() {
            return file;
        }
    }

    class SocketSession
    {
        boolean error = false;
        List<SocketTask> tasks;

        SocketSession() {
            tasks = new ArrayList<>();
        };

        public boolean getError(){
            return error;
        }
        public List<SocketTask> getTasks(){
            return tasks;
        }
    }

    private String serverIP;
    private int serverPort;

    private socketCallback callback;
    List<SocketSession> sessions;
    private SocketSession draft;
    private int finishedSessions;
    private Thread thread;

    SocketCommunicator(socketCallback c, String serverIP, int serverPort){
        callback = c;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        sessions = new ArrayList<>();
        draft = null;
        finishedSessions = 0;
        thread = null;
    };

    boolean clear_sessions(boolean force) {
        if(finishedSessions == sessions.size() || force)
        {
            sessions = new ArrayList<>();
            draft = null;
            finishedSessions = 0;
            return true;
        }
        else
            return false;
    }

    boolean clear_sessions() {
        return clear_sessions(false);
    }

    void start_sessions() {
        //Concurrency issue?
        if(thread == null && finishedSessions < sessions.size())
        {
            thread = new Thread(new RunSession());
            thread.start();
        }

    }

    void new_session() {
        draft = new SocketSession();
    }

    void add_task(serverCommands command, byte[] data, File file) {
        SocketTask task = new SocketTask(command, data, file);
        draft.tasks.add(task);
    }

    int add_session(ArrayList<SocketSession> session){
        sessions.addAll(session);
        start_sessions();
        return sessions.size() -1;
    }

    int add_session()
    {
        int index = sessions.size();
        sessions.add(draft);
        draft = null;

        start_sessions();

        return index;
    }

    class RunSession implements Runnable {
        Socket connectionSocket;

        RunSession() {}

        void connect() throws IOException {
            InetAddress serverAddr = InetAddress.getByName(serverIP);
            connectionSocket = new Socket();

            //Blocking
            connectionSocket.connect(new InetSocketAddress(serverAddr, serverPort), 5000);
        }

        void next_session(){

            for(SocketTask task : sessions.get(finishedSessions).tasks){
                try{
                    InputStream in = connectionSocket.getInputStream();
                    OutputStream out = connectionSocket.getOutputStream();

                    //Server command
                    out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(task.cmd.get_int()).array());

                    //Write to Server
                    out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(task.outData.length).array());
                    out.write(task.outData);

                    out.flush();

                    //Read reply
                    int bytesRead = 0;
                    byte[] dataLength = new byte[4];

                    while(bytesRead < dataLength.length){
                        bytesRead += in.read(dataLength, bytesRead, dataLength.length-bytesRead);
                    }

                    bytesRead = 0;
                    task.inData = new byte[ByteBuffer.wrap(dataLength).order(ByteOrder.LITTLE_ENDIAN).getInt()];

                    // Check for end of file? IE read() != -1
                    while(bytesRead < task.inData.length){
                        bytesRead += in.read(task.inData, bytesRead, task.inData.length-bytesRead);
                    }

                    //If file, upload it. If empty file download one.
                    if(task.file != null){
                        long fileLength = task.file.length();

                        //Send file
                        if (fileLength > 0) {
                            FileInputStream fileIn = new FileInputStream(task.file);

                            out.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(fileLength).array());

                            long transferred = 0;
                            while(transferred < fileLength)
                            {
                                byte[] fileData = new byte[8*1024];

                                int batchRead = fileIn.read(fileData);
                                out.write(fileData, 0, batchRead);

                                transferred += batchRead;
                                //Log.d("Uploaded", "g" + transferred);
                            }

                            //byte[] tullebyte = new byte[1];
                            //in.read(tullebyte);

                            fileIn.close();
                        }

                        //Receive file
                        else {
                            FileOutputStream fileOut = new FileOutputStream(task.file);

                            bytesRead = 0;
                            byte[] byteFileLength = new byte[8];

                            while(bytesRead < byteFileLength.length){
                                bytesRead += in.read(byteFileLength, bytesRead, byteFileLength.length-bytesRead);
                            }

                            fileLength = ByteBuffer.wrap(byteFileLength).order(ByteOrder.LITTLE_ENDIAN).getLong();

                            //Download whole chunk in one go?
                            long received = 0;
                            while(received < fileLength){
                                byte[] fileData = new byte[8*1024];

                                bytesRead = in.read(fileData);
                                fileOut.write(fileData,0,bytesRead);

                                received += bytesRead;
                            }
                            fileOut.close();
                        }
                    }
                }
                catch (IOException e) {
                    Log.e("Session in SocketThread", e.toString());
                    sessions.get(finishedSessions).error = true;
                    //callback.onException(e);
                    break;
                }
            }
            callback.sessionFinished(finishedSessions++);
        }

        @Override
        public void run() {
            try{
                connect();
                while(sessions.size() > finishedSessions)
                    next_session();
            }
            catch( Exception e) {
                Log.e("SocketThread", e.toString());
                if(callback != null)
                    callback.onException(e);
            }
            if( connectionSocket.isConnected()){
                try{
                    connectionSocket.close();
                }
                catch( Exception e) {
                    Log.e("SocketThread", e.toString());
                    if(callback != null)
                        callback.onException(e);
                }
            }
            callback.threadFinished();
            thread = null;
        }
    }
}