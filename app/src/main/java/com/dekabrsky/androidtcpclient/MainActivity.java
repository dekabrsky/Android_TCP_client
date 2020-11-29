package com.dekabrsky.androidtcpclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity
{
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;
    private String curIP = "192.168.0.102";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        final EditText newIPText = (EditText) findViewById(R.id.edit_ip);
        Button send = (Button)findViewById(R.id.send_button);
        Button update = (Button)findViewById(R.id.update_button);
        Button reload = (Button)findViewById(R.id.reload_button);
        final TextView curIPView = (TextView) findViewById(R.id.current_ip);
        curIPView.setText(curIP);


        //relate the listView from java to the one created in xml
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mAdapter.notifyDataSetChanged();
        mList.setAdapter(mAdapter);

        // подключаемся к серверу
        new connectTask().execute("");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //добавляем текст в лист
                arrayList.add("c: " + message);

                //отправляем сообщение на сервер
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //обновляем лист
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newIP = newIPText.getText().toString();
                curIP = newIP;
                curIPView.setText(curIP);
                newIPText.setText("");
                mTcpClient.sendMessage("close");
                new connectTask().execute("");
                mAdapter.notifyDataSetChanged();
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTcpClient.sendMessage("close");
                mAdapter.notifyDataSetChanged();
                /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);*/
                new connectTask().execute("");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {
            Log.i("TCP", "doInBackground");
            //создаем экземпляр ТСР-клиента
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //Здесь определяется метод отправки сообщения
                public void messageReceived(String message) {
                    //этот метод вызывает onProgressUpdate
                    publishProgress(message);
                }
            }, curIP);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //добавляем в лист сообщение от сервера
            arrayList.add(values[0]);

            // уведомим адаптер об изменении набора данных. Это означает, что получено
            // новое сообщение от сервера и добавлено в лист
            mAdapter.notifyDataSetChanged();
        }
    }
}