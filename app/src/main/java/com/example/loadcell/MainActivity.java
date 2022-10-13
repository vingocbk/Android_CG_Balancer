package com.example.loadcell;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@TargetApi(21)
public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    String LogFunction = "function";
    String deviceName = "Load Cell";
    private static final int REQUEST_BLUETOOTH_ADMIN_ID = 1;
    private static final int REQUEST_LOCATION_ID = 2;
    private static final int REQUEST_BLUETOOTH_ID = 3;

    //    BluetoothGatt mGatt;
    BluetoothGattService mCustomService;
    BluetoothGattCharacteristic mWriteCharacteristic;
    BluetoothGattCharacteristic mReadCharacteristic;
    List<BluetoothGattService> services;
    ImageView imgBleConnect, imgPositionCenter;
    TextView txtDataLoadCell1, txtDataLoadCell2, txtDataLoadCell3, txtTotalWeight;
    ProgressBar prbLoadingConnectBle;

    Button btnTareAllLoadCell, btnCalibrationFragment;
    Button btnCalibrationSave, btnCalibrationCancel;
    LinearLayout llPositionCenter;
    RelativeLayout rlCalibration;

    CheckBox cbCalibrationNoseTail, cbCalibrationMainsLeft, cbCalibrationMainsRight;
    EditText edtCalibrationWeight;

    EditText edtNoseTailWeight, edtMainsLeftWeight, edtMainsRightWeight;
    EditText edtDistanceZ, edtDistanceX, edtDistanceY;
    Button btnCalculateCG;
    TextView txtNoticeAddOrRemove, txtNotificationWeightRemove;

    ImageView imgArrayShowNoseHeave, imgArrayShowTailHeave;
    TextView txtNotificationNoseTailHeavy;
    //variable for save name motor
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String NoseWeigh = "NoseWeigh";
    String LeftWeight = "LeftWeight";
    String RightWeight = "RightWeight";
    String DistanceZ = "DistanceZ";
    String DistanceX = "DistanceX";
    String DistanceY = "DistanceY";

    private List<dataSaved> listDataSaved;
    private ListView lvDataSaved;
    Button btnSaveData, btnLoadData;
    RelativeLayout rlDataSavedFragment;
    ImageView imgBackFragmentDataSaved;
    ProgressBar prbLoadingUrl;
    String loadDataFail = "Can't Load Data";
    String cookie = "";
    String roomID   = "23";
    String urlLogging = "https://avystore.com/appapi/user/generate_auth_cookie?username=0936156099&password=12345";
    OkHttpClient client = new OkHttpClient();
    boolean flagGetCookie = false;
    boolean flagGetDataSaved = false;
    boolean flagSetDataSaved = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Log.i(LogFunction, "onCreate");
//        bleCheck();
//        locationCheck();
        initLayout();
        LoadDataBegin();

        btnLoadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.VISIBLE);
                prbLoadingUrl.setVisibility(View.VISIBLE);
                if(!cookie.equals("")){
                    String urlListDevice = "https://avystore.com/appapi/user/user_list_devices";
                    flagGetDataSaved = true;
                    RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("cookie", cookie)
                        .addFormDataPart("room_id", roomID)
                        .addFormDataPart("in", "1")
                        .addFormDataPart("id", "1")
                        .build();
                    postUrl(urlListDevice, requestBody);
                }
                else{
                    Toast.makeText(MainActivity.this, "Can't Get Data", Toast.LENGTH_SHORT).show();
                    prbLoadingUrl.setVisibility(View.INVISIBLE);
                }

            }
        });

        imgBackFragmentDataSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.INVISIBLE);
                prbLoadingUrl.setVisibility(View.INVISIBLE);
            }
        });

        btnCalculateCG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtNoseTailWeight.getText().toString().equals("")
                        || edtMainsLeftWeight.getText().toString().equals("")
                        || edtMainsRightWeight.getText().toString().equals("")
                        || edtDistanceZ.getText().toString().equals("")
                        || edtDistanceX.getText().toString().equals("")
                        || edtDistanceY.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Fill Full the Information!", Toast.LENGTH_SHORT).show();
                    return;
                }
                editor.putInt(NoseWeigh, Integer.parseInt(edtNoseTailWeight.getText().toString()));
                editor.putInt(LeftWeight, Integer.parseInt(edtMainsLeftWeight.getText().toString()));
                editor.putInt(RightWeight, Integer.parseInt(edtMainsRightWeight.getText().toString()));
                editor.putInt(DistanceZ, Integer.parseInt(edtDistanceZ.getText().toString()));
                editor.putInt(DistanceX, Integer.parseInt(edtDistanceX.getText().toString()));
                editor.putInt(DistanceZ, Integer.parseInt(edtDistanceY.getText().toString()));
                editor.commit();
                float weightRemoveOrAdd = 0;
                float noseWeight = (float) Integer.parseInt(edtNoseTailWeight.getText().toString());
                float leftWeight = (float) Integer.parseInt(edtMainsLeftWeight.getText().toString());
                float rightWeight = (float) Integer.parseInt(edtMainsRightWeight.getText().toString());
                float distanceZ = (float) Integer.parseInt(edtDistanceZ.getText().toString());
                float distanceX = (float) Integer.parseInt(edtDistanceX.getText().toString());
                float distanceY = (float) Integer.parseInt(edtDistanceY.getText().toString());
                weightRemoveOrAdd = ((noseWeight*(distanceZ-distanceX) - (leftWeight+rightWeight)*distanceX))/(distanceY-distanceX);

                txtTotalWeight.setText(String.valueOf((int)(noseWeight+leftWeight+rightWeight))+"g");
                if(weightRemoveOrAdd > 0){
                    imgArrayShowNoseHeave.setVisibility(View.VISIBLE);
                    imgArrayShowTailHeave.setVisibility(View.INVISIBLE);
                    txtNotificationNoseTailHeavy.setText("NOSE HEAVY");
                    txtNoticeAddOrRemove.setText("Remove");
                    txtNotificationWeightRemove.setText(String.valueOf((int)weightRemoveOrAdd));

                }else if(weightRemoveOrAdd < 0){
                    imgArrayShowNoseHeave.setVisibility(View.INVISIBLE);
                    imgArrayShowTailHeave.setVisibility(View.VISIBLE);
                    txtNotificationNoseTailHeavy.setText("TAIL HEAVY");
                    txtNoticeAddOrRemove.setText("Add");
                    txtNotificationWeightRemove.setText(String.valueOf(-(int)weightRemoveOrAdd));
                }
                else {
                    imgArrayShowNoseHeave.setVisibility(View.INVISIBLE);
                    imgArrayShowTailHeave.setVisibility(View.INVISIBLE);
                    txtNotificationNoseTailHeavy.setText("The plane is balanced");
                    txtNoticeAddOrRemove.setText("Remove");
                    txtNotificationWeightRemove.setText(String.valueOf((int)weightRemoveOrAdd));
                }
            }
        });

        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) llPositionCenter.getLayoutParams();
        marginParams.setMargins(0, 0, 340, 0);
    }


    public void LoadDataBegin() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        edtNoseTailWeight.setText(String.valueOf(sharedPreferences.getInt(NoseWeigh, 500)));
        edtMainsLeftWeight.setText(String.valueOf(sharedPreferences.getInt(LeftWeight, 500)));
        edtMainsRightWeight.setText(String.valueOf(sharedPreferences.getInt(RightWeight, 500)));
        edtDistanceZ.setText(String.valueOf(sharedPreferences.getInt(DistanceZ, 500)));
        edtDistanceX.setText(String.valueOf(sharedPreferences.getInt(DistanceX, 500)));
        edtDistanceY.setText(String.valueOf(sharedPreferences.getInt(DistanceY, 1000)));
        txtTotalWeight.setText(String.valueOf((int)(sharedPreferences.getInt(NoseWeigh, 500)
                +sharedPreferences.getInt(LeftWeight, 500)+sharedPreferences.getInt(RightWeight, 500)))+"g");

        listDataSaved = new ArrayList<>();
        dataSavedAdapter adapter = new dataSavedAdapter(this, R.layout.list_view_data_saved, listDataSaved);
        lvDataSaved.setAdapter(adapter);

        try {
            flagGetCookie = true;
            getUrl(urlLogging);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initLayout() {
        imgBleConnect = findViewById(R.id.imgBleConnect);
        txtDataLoadCell1 = findViewById(R.id.txtDataLoadCell1);
        txtDataLoadCell2 = findViewById(R.id.txtDataLoadCell2);
        txtDataLoadCell3 = findViewById(R.id.txtDataLoadCell3);

        btnTareAllLoadCell = findViewById(R.id.btnTareAllLoadCell);
        btnCalibrationFragment = findViewById(R.id.btnCalibrationFragment);

        prbLoadingConnectBle = findViewById(R.id.prbLoadingConnectBle);

        btnCalibrationSave = findViewById(R.id.btnCalibrationSave);
        btnCalibrationCancel = findViewById(R.id.btnCalibrationCancel);
        cbCalibrationNoseTail = findViewById(R.id.cbCalibrationNoseTail);
        cbCalibrationMainsLeft = findViewById(R.id.cbCalibrationMainsLeft);
        cbCalibrationMainsRight = findViewById(R.id.cbCalibrationMainsRight);
        edtCalibrationWeight = findViewById(R.id.edtCalibrationWeight);

        imgPositionCenter = findViewById(R.id.imgPositionCenter);
        llPositionCenter = findViewById(R.id.llPositionCenter);
        rlCalibration = findViewById(R.id.rlCalibration);

        edtNoseTailWeight = findViewById(R.id.edtNoseTailWeight);
        edtMainsLeftWeight = findViewById(R.id.edtMainsLeftWeight);
        edtMainsRightWeight = findViewById(R.id.edtMainsRightWeight);
        edtDistanceZ = findViewById(R.id.edtDistanceZ);
        edtDistanceX = findViewById(R.id.edtDistanceX);
        edtDistanceY = findViewById(R.id.edtDistanceY);
        btnCalculateCG = findViewById(R.id.btnCalculateCG);
        imgArrayShowNoseHeave = findViewById(R.id.imgArrayShowNoseHeave);
        imgArrayShowTailHeave = findViewById(R.id.imgArrayShowTailHeave);
        txtNotificationNoseTailHeavy = findViewById(R.id.txtNotificationNoseTailHeavy);
        txtNoticeAddOrRemove = findViewById(R.id.txtNoticeAddOrRemove);
        txtNotificationWeightRemove = findViewById(R.id.txtNotificationWeightRemove);
        txtTotalWeight = findViewById(R.id.txtTotalWeight);

        lvDataSaved = findViewById(R.id.lvDataSaved);
        btnSaveData = findViewById(R.id.btnSaveData);
        btnLoadData = findViewById(R.id.btnLoadData);
        rlDataSavedFragment = findViewById(R.id.rlDataSavedFragment);
        imgBackFragmentDataSaved = findViewById(R.id.imgBackFragmentDataSaved);
        prbLoadingUrl = findViewById(R.id.prbLoadingUrl);
    }

    public void getUrl(String url) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
//        Log.i("responseBodyData", dataReturn[0]);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Can.t Connect Internet", Toast.LENGTH_SHORT).show();
                        prbLoadingUrl.setVisibility(View.INVISIBLE);
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String dataReturn = "";
//                    assert responseBody != null;
                    dataReturn = responseBody.string();
                    Log.i("responseBodyData", dataReturn);
                    if(flagGetCookie){
                        flagGetCookie = false;
                        JSONObject root = new JSONObject(dataReturn);
                        cookie = root.getString("cookie");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prbLoadingUrl.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Can.t Connect Internet", Toast.LENGTH_SHORT).show();
                            prbLoadingUrl.setVisibility(View.INVISIBLE);
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    public void postUrl(String url, RequestBody requestBody){
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Can.t Connect Internet", Toast.LENGTH_SHORT).show();
                        prbLoadingUrl.setVisibility(View.INVISIBLE);
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.i("responseBodyData", myResponse);
                    if(flagGetDataSaved){
                        flagGetDataSaved = false;
                        prbLoadingUrl.setVisibility(View.INVISIBLE);
                        JSONObject root = null;
                        try {
                            root = new JSONObject(myResponse);
                            JSONArray arrayRooms = root.getJSONArray("rooms");
                            listDataSaved.clear();
                            for(int i = 0; i < arrayRooms.length(); i++){
                                JSONObject mJsonObjectProperty = arrayRooms.getJSONObject(i);
                                String data = mJsonObjectProperty.getString("img_path");
                                JSONObject mJsonData = new JSONObject(data);
                                Log.i("responseBodyData", data);
                                String name = mJsonData.getString("name");
                                int X = mJsonData.getInt("X");
                                int Y = mJsonData.getInt("Y");
                                int Z = mJsonData.getInt("Z");
                                listDataSaved.add(new dataSaved(i + 1, name, X, Y, Z));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lvDataSaved.invalidateViews();
                                    lvDataSaved.refreshDrawableState();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }





    protected void scanDeviceBleToConnect(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.i(LogFunction, "onResume -> enableBtIntent");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                Log.i(LogFunction, "onResume -> mLEScanner");
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LogFunction, "onResume");
//        imgBleConnect.setBackgroundResource(mipmap.ble_disconnect);
//        scanDeviceBleToConnect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LogFunction, "onPause");
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
            }
            mGatt.disconnect();
        }

    }

    @Override
    protected void onDestroy() {
        Log.i(LogFunction, "onDestroy");
        if (mGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
        }
        imgBleConnect.setBackgroundResource(R.mipmap.ble_disconnect);
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LogFunction, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        Log.i(LogFunction, "scanLeDevice " + enable);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        Log.i(LogFunction, "run stopLeScan");
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
//                            return;
                        }

                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        Log.i(LogFunction, "run stopScan");
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(LogFunction, "startLeScan");
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.i(LogFunction, "startScan");
//                mLEScanner.startScan(mScanCallback);
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(LogFunction, "stopLeScan");
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                Log.i(LogFunction, "stopScan");
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(LogFunction, "onScanResult");
            Log.i("callbaogckType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            //Log.i("getName", result.getDevice().getName());
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                Log.i(LogFunction, "return false");
//                return;
            }
            if (result.getDevice().getName() != null) {
                if (result.getDevice().getName().equals(deviceName)) {
                    BluetoothDevice btDevice = result.getDevice();
                    connectToDevice(btDevice);
                }
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(LogFunction, "onBatchScanResults");
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(LogFunction, "onScanFailed");
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Log.i(LogFunction, "onLeScan");
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            Log.i(LogFunction, "connectToDevice");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
            }
            mGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            Log.i("onConnectionStateChange", "Status: " + status);
            Log.i(LogFunction, "onConnectionStateChange");
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    //Toast.makeText(MainActivity.this, "CONNECTED to " + gatt.getDevice().getName().toString(), Toast.LENGTH_SHORT).show();
                    Log.i("gattCallback", "STATE_CONNECTED");
                    imgBleConnect.setBackgroundResource(R.mipmap.ble_connect);
                    prbLoadingConnectBle.setVisibility(View.INVISIBLE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                        return;
                    }
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    imgBleConnect.setBackgroundResource(R.mipmap.ble_disconnect);
//                    btnSendEcg.setText("START ECG");
//                    btnSendSpo2.setText("START SPO2");
                    onDestroy();
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(LogFunction, "onServicesDiscovered");
                services = gatt.getServices();

                mGatt = gatt;
                /*get the service characteristic from the service*/
//                mCustomService = gatt.getService(UUID.fromString("D973F2E0-B19E-11E2-9E96-0800200C9A66"));
//                mCustomService = gatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
                mCustomService = gatt.getService(services.get(2).getUuid());
                Log.w("writeCharacteristic", services.get(2).getUuid().toString());

                /*get the write characteristic from the service*/
//                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("D973F2E2-B19E-11E2-9E96-0800200C9A66"));
//                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
                mWriteCharacteristic = mCustomService.getCharacteristic(services.get(2).getCharacteristics().get(1).getUuid());
                Log.w("writeCharacteristic", services.get(2).getCharacteristics().get(0).getUuid().toString());

                /*get the read characteristic from the service*/
//                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("d973f2e1-b19e-11e2-9e96-0800200c9a66"));
//                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));
                mReadCharacteristic = mCustomService.getCharacteristic(services.get(2).getCharacteristics().get(0).getUuid());
                Log.w("writeCharacteristic", services.get(2).getCharacteristics().get(1).getUuid().toString());

                /*turn on notification to listen data on ReadCharacteristic*/
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    return;
                }
                if (!mGatt.setCharacteristicNotification(mReadCharacteristic, true)) {
                    Log.w("writeCharacteristic", "Failed to setCharacteristicNotification");
                }
                /*turn on notification to listen data on onCharacteristicChanged*/
                BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//                BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));
//                BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(services.get(3).getCharacteristics().get(1).getUuid());
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?

            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d("onCharacteristicChanged", "onCharacteristicChanged" + Arrays.toString(characteristic.getValue()));
            int asIntLoadCell1 = (characteristic.getValue()[0] & 0xFF)
                    | ((characteristic.getValue()[1] & 0xFF) << 8)
                    | ((characteristic.getValue()[2] & 0xFF) << 16)
                    | ((characteristic.getValue()[3] & 0xFF) << 24);
            float asFloatLoadCell1 = Float.intBitsToFloat(asIntLoadCell1);
            int asIntLoadCell2 = (characteristic.getValue()[4] & 0xFF)
                    | ((characteristic.getValue()[5] & 0xFF) << 8)
                    | ((characteristic.getValue()[6] & 0xFF) << 16)
                    | ((characteristic.getValue()[7] & 0xFF) << 24);
            float asFloatLoadCell2 = Float.intBitsToFloat(asIntLoadCell2);
            int asIntLoadCell3 = (characteristic.getValue()[8] & 0xFF)
                    | ((characteristic.getValue()[9] & 0xFF) << 8)
                    | ((characteristic.getValue()[10] & 0xFF) << 16)
                    | ((characteristic.getValue()[11] & 0xFF) << 24);
            float asFloatLoadCell3 = Float.intBitsToFloat(asIntLoadCell3);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtDataLoadCell1.setText(String.valueOf(asFloatLoadCell1));
                    txtDataLoadCell2.setText(String.valueOf(asFloatLoadCell2));
                    txtDataLoadCell3.setText(String.valueOf(asFloatLoadCell3));
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt,    characteristic, status);
            Log.d("onCharacteristicWrite", "Characteristic " + Arrays.toString(characteristic.getValue()) + " written");

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.d("onCharacteristicRead", Arrays.toString(characteristic.getValue()));

        }
    };
}