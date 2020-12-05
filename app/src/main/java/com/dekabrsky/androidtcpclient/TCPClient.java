package com.dekabrsky.androidtcpclient;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {

    public static final String TAG = TCPClient.class.getSimpleName();
    private final String SERVER_IP; //server IP address
    private final int SERVER_PORT;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private Socket socket;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, String curIP) {
        mMessageListener = listener;
        String[] splitString = curIP.split(":");
        SERVER_IP = splitString[0];
        SERVER_PORT = Integer.parseInt(splitString[1]);
        Log.d("TCP Client", "Creating...");
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        sendMsgToListener("--SOCKET IS closed--");
        try {
            socket.close();
        } catch (IOException e){
            sendMsgToListener("--SOCKET IS not closed--");
        }
        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;
        sendMsgToListener("--INIT--");

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT),5000);
            sendMsgToListener("--SOCKET ON " + SERVER_IP + ":" + SERVER_PORT +" IS OPEN--");

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage.equals("close")) {
                        socket.close();
                        sendMsgToListener("--SOCKET ON " + SERVER_IP + ":" + SERVER_PORT + " IS CLOSE--");
                    }
                    sendMsgToListener(mServerMessage);
                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                sendMsgToListener("--SOCKET ON " + SERVER_IP + ":" + SERVER_PORT + " IS CLOSE--");
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            sendMsgToListener("--EXCEPTION--");
        }

    }

    private void sendMsgToListener(String mServerMessage) {
        if (mServerMessage != null && mMessageListener != null) {
            //call the method messageReceived from MyActivity class
            mMessageListener.messageReceived(mServerMessage);
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}