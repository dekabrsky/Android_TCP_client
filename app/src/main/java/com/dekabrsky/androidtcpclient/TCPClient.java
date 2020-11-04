package com.dekabrsky.androidtcpclient;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public static final String SERVERIP = "192.168.0.108"; //поставь IP своего компьютера
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
            //здесь вы должны указать IP-адрес вашего компьютера.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //создание сокета для установления соединения с сервером
            Socket socket = new Socket(serverAddr, SERVERPORT);

            try {

                //отправление сообщения на сервер
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //получение сообщения, которое сервер отправляет обратно
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //сокет должен быть закрыт. Невозможно повторно подключиться к этому сокету
                //после его закрытия, что означает, что должен быть создан новый экземпляр сокета.
                socket.close();
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
