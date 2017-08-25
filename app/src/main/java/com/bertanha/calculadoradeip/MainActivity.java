package com.bertanha.calculadoradeip;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText mask;
    private TextView network;
    private TextView broadcast;
    private TextView firstHost;
    private TextView lastHost;
    private TextView amountHost;

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
                calculate();
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
        String default_address = getString(R.string.tv_default_address);
        //ip.setText(default_address);
        //mask.setText(default_address);
        network.setText(default_address);
        broadcast.setText(default_address);
        firstHost.setText(default_address);
        lastHost.setText(default_address);
        amountHost.setText(default_address);

        ip.requestFocus();

    }



    public void calculate() {
        String ipBinary;
        String maskBinary;
        String networkDecimal;
        String broadcastDecimal;
        String firstHostDecimal;
        String lastHostDecimal;
        int hostCount;

        if (!validateFields()) {
            return;
        }

        ipBinary = parseToBinary(getIp());
        maskBinary = parseToBinary(getMask());

        networkDecimal = parseToString(addDotsInBinaryString(calculateRede(ipBinary, maskBinary)));
        broadcastDecimal = parseToString(addDotsInBinaryString(calculateBroadcast(ipBinary, maskBinary)));
        firstHostDecimal = calculateFirstHost(networkDecimal);
        lastHostDecimal = calculateLastHost(broadcastDecimal);
        hostCount = getCountHost(firstHostDecimal, lastHostDecimal);

        network.setText(networkDecimal);
        broadcast.setText(broadcastDecimal);
        firstHost.setText(firstHostDecimal);
        lastHost.setText(lastHostDecimal);
        amountHost.setText(String.valueOf(hostCount));

    }

    private boolean validateFields() {
        return validateAddress(ip, getIp()) && validateAddress(mask, getMask());
    }

    private boolean validateAddress(EditText et, String address) {
        String mAddress[] = address.split("\\.");

        if (address.isEmpty()) {
            et.setError(getString(R.string.et_err_empty));
            return false;
        }
        if (mAddress.length != 4) {
            et.setError(getString(R.string.et_err_length));
            return false;
        }

//        try {
//            if (Integer.parseInt(mAddress[3]) >= 255) {
//                et.setError(getString(R.string.et_err_out_of_range));
//                return false;
//            }
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            et.setError(getString(R.string.et_err_not_numeric));
//            return false;
//        }


        for (String a : mAddress) {
            try {
                if (Integer.parseInt(a) < 0 || Integer.parseInt(a) > 255) {
                    et.setError(getString(R.string.et_err_out_of_range));
                    return false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                et.setError(getString(R.string.et_err_not_numeric));
                return false;
            }
        }
        return true;
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
        String inverseMaskWithoutDots = removeDots(reverseMask(binaryMask));


        broadcast = operatorAndOr(ipWithoutDots, inverseMaskWithoutDots, "OR");
        Log.i("broadcast", "calculateBroadcast: " + broadcast);

        return broadcast;
    }

    private String reverseMask(String binaryMask) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();

        stringBuilder.append(binaryMask.substring(0, binaryMask.indexOf('0')));
        stringBuilder1.append(binaryMask.substring(binaryMask.indexOf('0')));

        System.out.println(stringBuilder.toString().replace('1', '0') + stringBuilder1.toString().replace('0', '1'));
        return stringBuilder.toString().replace('1', '0') + stringBuilder1.toString().replace('0', '1');

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
        try {
            return Boolean.parseBoolean(String.valueOf(binaryString.charAt(index - 1)).equals("1") ?  "true" : "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String calculateRede(String binaryIp, String binaryMask) {
        String rede;
        String ipWithoutDots = removeDots(binaryIp);
        String maskWithoutDots = removeDots(binaryMask);

        rede = operatorAndOr(ipWithoutDots, maskWithoutDots, "AND");
        Log.i("network", "calculateRede: " + rede);

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
        return ip.getText().toString();
    }


    private String getMask() {
        return mask.getText().toString();
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
        ip = (EditText) findViewById(R.id.et_ip);
        mask = (EditText) findViewById(R.id.et_mask);
        network = (TextView) findViewById(R.id.tv_network);
        broadcast = (TextView) findViewById(R.id.tv_broadcast);
        firstHost = (TextView) findViewById(R.id.tv_first_host);
        lastHost = (TextView) findViewById(R.id.tv_last_host);
        amountHost = (TextView) findViewById(R.id.tv_amount_host);

        //mask.addTextChangedListener(MaskWatcher.buildAddress());
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
