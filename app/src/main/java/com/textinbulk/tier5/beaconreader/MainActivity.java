package com.textinbulk.tier5.beaconreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Filter;
import java.util.logging.MemoryHandler;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private ArrayList<BluetoothDevice> mLeDevice;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothLeScanner mLeScanning;
    public ScanSettings setting;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private int CompanyIdentifier = 0x0059;
    private TextView IdTextView;
    private TextView DistTextView;
    private TextView RSSTextView;
    private TextView RSSIonmetreTextView;
    private int ratiodb;
    private float ratiolinear;
    private float distance;
    public static final long SCAN_PERIOD = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        Button button = (Button) findViewById(R.id.scanButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        IdTextView = (TextView) findViewById(R.id.textView2);
        DistTextView = (TextView) findViewById(R.id.textView3);
        RSSTextView = (TextView) findViewById(R.id.textView4);
        RSSIonmetreTextView = (TextView) findViewById(R.id.textView5);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLeScanning = mBluetoothAdapter.getBluetoothLeScanner();
                setting = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters = new ArrayList<ScanFilter>();

            }
            ScanLeDevice(true);

        }

    }

    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null || mBluetoothAdapter.isEnabled()) {
            ScanLeDevice(false);
        }
    }

    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();

    }

    private void ScanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallBack);
                    } else {
                        mLeScanning.startScan(mScanCallBack);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallBack);
            } else {
                mLeScanning.startScan(filters, setting, mScanCallBack);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallBack);
            } else {
                mLeScanning.startScan(mScanCallBack);
            }

        }
    }

    private ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            Log.i("Rssi value", String.valueOf(result.getRssi()));
            String RSSIDisplay = "" + String.valueOf(result.getRssi()) + "dbm";
            RSSTextView.setText(RSSIDisplay);
            android.bluetooth.le.ScanRecord btScanRecord = result.getScanRecord();
            if (btScanRecord != null) {
                byte[] data = btScanRecord.getManufacturerSpecificData(CompanyIdentifier);
                IdTextView.setText(String.valueOf(CompanyIdentifier));
                if (data != null) {
                    Log.i("Rssi nearer is ", String.valueOf(data[data.length -1]));
                    String RSSIonmetreDisplay= "" + String.valueOf((data[data.length -1])) + "dbm";
                    RSSIonmetreTextView.setText(RSSIonmetreDisplay);

                    ratiodb=data[data.length-1]-result.getRssi();
                    ratiolinear=(float)Math.pow((double)10,(double)ratiodb/10);
                    distance=(float)Math.sqrt((double)ratiodb);
                    if(distance<1.0){
                      DistTextView.setText(String.valueOf("near"));
                    }
                    else
                        if(distance>=1.5 && distance <3.5)
                             DistTextView.setText(String.valueOf("Intermediate"));

                        else
                            if(distance>=3.5)
                                DistTextView.setText(String.valueOf("Far"));



                }
            }
            Connect();

        }
        @Override
        public void onBatchScanResults(List<ScanResult>result){
            for (ScanResult sr:result){
                Log.i("Scan Result-Results",sr.toString());
            }

        }
        @Override
        public void onScanFailed(int errorCode){
            Log.e("Scan Failed","errorCode"+errorCode);
        }

    };
    private BluetoothAdapter.LeScanCallback mLeScanCallBack =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                          Log.i("OnLeScan",device.toString());



                        }
                    });
                }
            };




    public  void Connect(){
        ScanLeDevice(false);

    }

}
