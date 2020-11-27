package com.dekabrsky.androidtcpclient;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public static final String SERVERIP = "192.168.0.103"; //поставь IP своего компьютера
    public static final int SERVERPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Конструктор класса. OnMessagedReceived прослушивает сообщения с сервера
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Отправляет сообщение, введенное клиентом, на сервер
     * @param message текст, введенный клиентом
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            Socket socket = new Socket(serverAddr, SERVERPORT);
            String mServerMessage = "init";
            try {
                PrintWriter mBufferOut = new PrintWriter(socket.getOutputStream());
                Log.e("TCP Client", "C: Sent.");
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int charsRead = 0; char[] buffer = new char[2024]; //choose your buffer size if you need other than 1024
                while (mRun) {
                    charsRead = mBufferIn.read(buffer);
                    mServerMessage = new String(buffer).substring(0, charsRead);
                    if (mServerMessage != null && mMessageListener != null) {
                        Log.e("in if---------->>", " Received : '" + mServerMessage + "'");
                    }
                    mServerMessage = null;
                }
                Log.e("-------------- >>", " Received : '" + mServerMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                Log.e("-------------- >>", "Close socket " );
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    //Объявление интерфейса. Метод messageReceived(String message) должен быть
    //реализован в классе MyActivity в on asynctask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
