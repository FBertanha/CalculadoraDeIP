package com.bertanha.calculadoradeip;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText rede;
    private EditText broadcast;
    private EditText firstHost;
    private EditText lastHost;
    private EditText qtdHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFields();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Button calculateButton = (Button) findViewById(R.id.button_calculate);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateIP();
            }
        });

        Button cleanButton  = (Button) findViewById(R.id.button_clean);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanFields();
            }
        });
    }

    private void cleanFields() {
        ip.setText("");
        rede.setText("");
        broadcast.setText("");
        firstHost.setText("");
        lastHost.setText("");
        qtdHost.setText("");

    }


    public void calculateIP() {
        String ipBinary;
        String maskBinary;
        String ipRede;
        String broadcastIp;
        String firstHostIp;
        String lastHostIp;
        int hostCount;

        ipBinary = parseToBinary(getIp());
        maskBinary = generateBinaryMask(getMask(), false);

        ipRede = parseToString(addDotsInBinaryString(calculateRede(ipBinary, maskBinary)));
        broadcastIp = parseToString(addDotsInBinaryString(calculateBroadcast(ipBinary, generateBinaryMask(getMask(), true))));
        firstHostIp = calculateFirstHost(ipRede);
        lastHostIp = calculateLastHost(broadcastIp);
        hostCount = getCountHost(firstHostIp, lastHostIp);

        rede.setText(ipRede);
        broadcast.setText(broadcastIp);
        firstHost.setText(firstHostIp);
        lastHost.setText(lastHostIp);
        qtdHost.setText(String.valueOf(hostCount));

    }

    @NonNull
    private String calculateFirstHost(String ipRede) {
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(ipRede);

        try {
            String ip[] = ipRede.split("\\.");

            stringBuilder.append(ip[0]);
            stringBuilder.append('.');
            stringBuilder.append(ip[1]);
            stringBuilder.append('.');
            stringBuilder.append(ip[2]);
            stringBuilder.append('.');
            stringBuilder.append(Integer.parseInt(ip[3]) + 1);

        } catch (Exception e) {
            Log.e("ERROR", "calculateFirstHost: " + stringBuilder);
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    @NonNull
    private String calculateLastHost(String ipBroadcast) {
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(ipRede);

        try {
            String ip[] = ipBroadcast.split("\\.");

            stringBuilder.append(ip[0]);
            stringBuilder.append('.');
            stringBuilder.append(ip[1]);
            stringBuilder.append('.');
            stringBuilder.append(ip[2]);
            stringBuilder.append('.');
            stringBuilder.append(Integer.parseInt(ip[3]) - 1);

        } catch (Exception e) {
            Log.e("ERROR", "calculateLastHost: " + stringBuilder);
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private int getCountHost(String fistHost, String lastHost) {
        int count = 0;
        String first = fistHost.split("\\.")[3];
        String last = lastHost.split("\\.")[3];

        try {
            count = Integer.parseInt(last) - Integer.parseInt(first) + 1;
        } catch (Exception e) {
            Log.e("ERROR", "calculateLastHost: " + count);
            e.printStackTrace();
        }

        return count;

    }


    private String calculateBroadcast(String binaryIp, String binaryMask) {
        String broadcast;
        String ipWithoutDots = removeDots(binaryIp);
        String maskWithoutDots = removeDots(binaryMask);


        broadcast = operatorAndOr(ipWithoutDots, maskWithoutDots, "OR");
        Log.i("broadcast", "calculateBroadcast: " + broadcast);

        return broadcast;
    }

    private String operatorAndOr(String binaryIp, String binaryMask, String operator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= 32; i++) {
            boolean ip = getBooleanFromBit(binaryIp, i);
            boolean mask = getBooleanFromBit(binaryMask, i);

            if(operator.equalsIgnoreCase("AND")) {
                if(ip && mask) {
                    stringBuilder.append(1);
                } else {
                    stringBuilder.append(0);
                }
            } else {
                if(ip || mask) {
                    stringBuilder.append(1);
                } else {
                    stringBuilder.append(0);
                }
            }
        }
        return stringBuilder.toString();
    }

    private boolean getBooleanFromBit(String binaryString, int index) {
        return Boolean.parseBoolean(String.valueOf(binaryString.charAt(index - 1)).equals("1") ?  "true" : "false");
    }

    private String calculateRede(String binaryIp, String binaryMask) {
        String rede;
        String ipWithoutDots = removeDots(binaryIp);
        String maskWithoutDots = removeDots(binaryMask);

        rede = operatorAndOr(ipWithoutDots, maskWithoutDots, "AND");
        Log.i("rede", "calculateRede: " + rede);

        return rede;
    }

    private String removeDots(String str) {
        return str.replace(".", "");
    }

    private String addDotsInBinaryString(String binaryString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(binaryString);
        stringBuilder.insert(8, '.');
        stringBuilder.insert(8 * 2 + 1, '.');
        stringBuilder.insert(8 * 3 + 2, '.');
        return stringBuilder.toString();
    }


    private String parseToString(String binaryStringWitDots) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            String ip[] = binaryStringWitDots.split("\\.");

            for (String s:
                    ip) {

                stringBuilder.append(binaryToString(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        Log.e("PARSED", "parseToString: " + stringBuilder);
        return stringBuilder.toString();
    }

    @NonNull
    private String parseToBinary(String ipWithDots) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            String ip[] = ipWithDots.split("\\.");

            for (String s:
                 ip) {
                stringBuilder.append(stringToBinary(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (fullBinaryIp.endsWith(".")) {
//            fullBinaryIp = fullBinaryIp.substring(0, fullBinaryIp.length() -1);
//        }
        Log.i("PARSED", "parseToBinary: " + stringBuilder);
        return stringBuilder.toString();
    }

    private String getIp() {
        String ip = "";
        try {
            if(getIpAndMask().contains("/")) {
                ip = getIpAndMask().substring(0, getIpAndMask().indexOf("/"));
            } else {
                return getIpAndMask();
            }
        } catch (NumberFormatException e) {
            Log.e("ERROR", "getOnlyIp: " + e.getMessage());
            e.printStackTrace();
        }
        return ip;
    }


    private int getMask() {
        int mask = 0;

        try {
            if(getIpAndMask().contains("/")) {
                mask = Integer.parseInt(getIpAndMask().substring(getIpAndMask().indexOf("/") + 1));
            }
        } catch (NumberFormatException e) {
            Log.e("ERROR", "getMaskLengthFromIp: " + e.getMessage());
            e.printStackTrace();
        }
        return mask;
    }

    @NonNull
    private String generateBinaryMask(int length, boolean reverse) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= 32; i++) {

            if(i <= length) {
                stringBuilder.append((!reverse ? 1 : 0));
            } else {
                stringBuilder.append((!reverse ? 0 : 1));
            }
        }

        return stringBuilder.toString();
    }

//    private String generateBinaryMaskReverse(int length) {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 1; i <= 32; i++) {
//
//            if(i <= length) {
//                stringBuilder.append(0);
//            } else {
//                stringBuilder.append(1);
//            }
//            if(i < 32 && i %8 == 0) {
//                stringBuilder.append(".");
//            }
//        }
//
//        return stringBuilder.toString();
//    }

    private String binaryToString(String binaryIp) {
        String ipString = "";
        Log.e("BINARY TO PARSE", "binaryToString: " + binaryIp);
        try {
            ipString = String.valueOf(Long.parseLong(binaryIp, 2)) + ".";
            //ipString = String.valueOf(Long.parseLong(binaryIp, 2));
        } catch (NumberFormatException e) {
            Log.e("ERROR", "binaryToString: " + e.getMessage());
            e.printStackTrace();
        }
        return ipString;
    }

    @NonNull
    private String stringToBinary(String ip) {
        String ipBinary = "";
        try {
            //ipBinary = String.format("%8s", Long.toBinaryString(Long.parseLong(ip))).replace(' ', '0') + ".";
            ipBinary = String.format("%8s", Long.toBinaryString(Long.parseLong(ip))).replace(' ', '0');
        } catch (NumberFormatException e) {
            Log.e("ERROR", "stringToBinary: " + e.getMessage());
            e.printStackTrace();
        }
        return ipBinary;

    }

    private void loadFields() {
        ip = (EditText) findViewById(R.id.edit_ip);
        rede = (EditText) findViewById(R.id.edit_rede);
        broadcast = (EditText) findViewById(R.id.edit_broadcast);
        firstHost = (EditText) findViewById(R.id.edit_first_host);
        lastHost = (EditText) findViewById(R.id.edit_last_host);
        qtdHost = (EditText) findViewById(R.id.edit_qtd_host);
    }

    @NonNull
    private String getIpAndMask() {
        return ip.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
