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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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

    ImageView imgArrayShowNoseHeave, imgArrayShowTailHeave, imgBackground;
    TextView txtNotificationNoseTailHeavy;

    String NoseWeigh = "NoseWeigh";
    String LeftWeight = "LeftWeight";
    String RightWeight = "RightWeight";
    String DistanceZ = "DistanceZ";
    String DistanceX = "DistanceX";
    String DistanceY = "DistanceY";

    private List<dataSaved> listDataSaved;
    private ListView lvDataSaved;
    Button btnSaveData, btnLoadData;
    RelativeLayout rlDataSavedFragment, rlSendRequestSaveName;
    ImageView imgBackFragmentDataSaved, imgShowPlane, imgSelectBackground, imgRefreshBackground;
    ProgressBar prbLoadingUrl;
    TextView txtShowNameDataSaved;
    Spinner spnShowDataSaved;
    EditText edtNewNameSaved;
    Button btnSendSavedNameRequest, btnCancelSavedNameRequest;
    String loadDataFail = "Can't Load Data";
    public static int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static int REQUEST_CODE_SELECT_IMAGE = 2;
    String nameUriBackground = "Uri";
    String IMAGES_FOLDER_NAME = "Landing";
    String strIndexName = "index";
    String cookie = "";
    String roomID   = "23";
    String urlLogging = "https://avystore.com/appapi/user/generate_auth_cookie?username=0936156099&password=12345";
    OkHttpClient client = new OkHttpClient();
    ArrayAdapter spnAdapter;
    boolean flagGetCookie = false;
    boolean flagGetDataSaved = false;
    boolean flagSetDataSaved = false;
    boolean flagSyncImage = false;
    int[] positionHeight = new int[10];  //star form 1 -> 6
    int[] positionWidth = new int[10];
    //variable for save name motor
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String syncImage = "sync";
    String positionHeight1 = "Height1";
    String positionHeight2 = "Height2";
    String positionHeight3 = "Height3";
    String positionHeight4 = "Height4";
    String positionHeight5 = "Height5";
    String positionHeight6 = "Height6";

    String positionWidth1 = "Width1";
    String positionWidth2 = "Width2";
    String positionWidth3 = "Width3";
    String positionWidth4 = "Width4";
    String positionWidth5 = "Width5";
    String positionWidth6 = "Width6";
    Bitmap bmp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Log.i(LogFunction, "onCreate");
        initLayout();
        LoadDataBegin();
        btnLoadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.VISIBLE);
                if(!listDataSaved.isEmpty()){
                    return;
                }
                prbLoadingUrl.setVisibility(View.VISIBLE);
                if(cookie.equals("")) {
                    try {
                        flagGetCookie = true;
                        getUrl(urlLogging);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    String urlListDevice = "https://avystore.com/appapi/user/user_list_devices";
                    flagGetDataSaved = true;
                    RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("cookie", cookie)
                        .addFormDataPart("room_id", roomID)
                        .build();
                    postUrl(urlListDevice, requestBody);
                }
//                else{
//                    Toast.makeText(MainActivity.this, "Can't Get Data", Toast.LENGTH_SHORT).show();
//                    prbLoadingUrl.setVisibility(View.INVISIBLE);
//                }
            }
        });

        imgBackFragmentDataSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.INVISIBLE);
                prbLoadingUrl.setVisibility(View.INVISIBLE);
            }
        });

        lvDataSaved.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dataSaved data = listDataSaved.get(i);
                rlDataSavedFragment.setVisibility(View.INVISIBLE);
                txtShowNameDataSaved.setText(data.getName());
                edtDistanceX.setText(String.valueOf(data.getX()));
                edtDistanceY.setText(String.valueOf(data.getY()));
                edtDistanceZ.setText(String.valueOf(data.getZ()));
            }
        });

        //--------------------------------------change background--------------------------------------------------------
        imgSelectBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });
        imgRefreshBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmpBackground = Bitmap.createBitmap(imgBackground.getWidth(), imgBackground.getHeight(), Bitmap.Config.ARGB_8888);;
                bmpBackground.eraseColor(Color.TRANSPARENT);
                imgBackground.setImageBitmap(bmpBackground);
                editor.putString(nameUriBackground, nameUriBackground);
                editor.commit();
            }
        });
        //--------------------------------------change background--------------------------------------------------------

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtShowNameDataSaved.getText().toString().equals("") || listDataSaved.size() == 0){
                    Toast.makeText(MainActivity.this,"Load Data Before Save",Toast.LENGTH_SHORT).show();
                    return;
                }
                rlSendRequestSaveName.setVisibility(View.VISIBLE);
                int sub_device_id = 0;
                spnAdapter.clear();
                for(int i = 0; i < listDataSaved.size(); i++){
                    dataSaved data = listDataSaved.get(i);
                    if(data.getName().equals(txtShowNameDataSaved.getText().toString())){
                        edtNewNameSaved.setText(txtShowNameDataSaved.getText().toString());
                        sub_device_id = i;
                    }
                    spnAdapter.add(data.getName());
                }
                spnShowDataSaved.setSelection(sub_device_id);
            }
        });
        btnSendSavedNameRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtNewNameSaved.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Fill New Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                prbLoadingUrl.setVisibility(View.VISIBLE);
                String urlListDevice = "https://avystore.com/appapi/user/user_update_device";
                JSONObject imgPathObject = new JSONObject();
                JSONObject dataObject = new JSONObject();
                try {
                    imgPathObject.put("name", edtNewNameSaved.getText().toString());
                    imgPathObject.put("X", Integer.parseInt(edtDistanceX.getText().toString()));
                    imgPathObject.put("Y", Integer.parseInt(edtDistanceY.getText().toString()));
                    imgPathObject.put("Z", Integer.parseInt(edtDistanceZ.getText().toString()));
                    dataObject.put("room_id", roomID);
                    dataObject.put("device_id", "VP6");
                    dataObject.put("img_path", imgPathObject.toString());
                    dataObject.put("name", "ngoc");
                    dataObject.put("status", "off");
                    dataObject.put("sub_device_id", spnShowDataSaved.getSelectedItemPosition()+1);
                    dataObject.put("feature", 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                flagSetDataSaved = true;
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("cookie", cookie)
                        .addFormDataPart("device_value", dataObject.toString())
                        .build();
                Log.i("responseBodyData", dataObject.toString());
                postUrl(urlListDevice, requestBody);
            }
        });

        btnCancelSavedNameRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prbLoadingUrl.setVisibility(View.INVISIBLE);
                rlSendRequestSaveName.setVisibility(View.INVISIBLE);
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
                //-------draw image---------
                if(flagSyncImage){
                    float y_Z = (float) (Integer.parseInt(edtDistanceY.getText().toString()))/(float) (Integer.parseInt(edtDistanceZ.getText().toString()));
                    Log.i("BitmapDrawable", String.valueOf(y_Z));
                    clearBitmap();
                    drawLine(positionWidth[1],positionHeight[1],positionWidth[4],positionHeight[4], Color.RED);
                    drawLine(positionWidth[1],positionHeight[1],positionWidth[6],positionHeight[1], Color.RED);
                    drawLine(positionWidth[6],positionHeight[1],positionWidth[6],positionHeight[4], Color.RED);
                    drawLine(positionWidth[6]+(float)(1 - y_Z)*(positionWidth[2]-positionWidth[6]),
                            positionHeight[2],
                            positionWidth[2],
                            positionHeight[2], Color.MAGENTA);
                    drawLine(positionWidth[6]+(float)(1 - y_Z)*(positionWidth[2]-positionWidth[6]),
                            positionHeight[2],
                            positionWidth[6]+(float)(1 - y_Z)*(positionWidth[2]-positionWidth[6]),
                            positionHeight[4], Color.MAGENTA);
                    float x_Z = (float) (Integer.parseInt(edtDistanceX.getText().toString()))/(float) (Integer.parseInt(edtDistanceZ.getText().toString()));
                    drawLine(positionWidth[6]+(float)(1 - x_Z)*(positionWidth[3]-positionWidth[6]),
                            positionHeight[3],
                            positionWidth[3],
                            positionHeight[3], Color.GREEN);
                    drawLine(positionWidth[6]+(float)(1 - x_Z)*(positionWidth[3]-positionWidth[6]),
                            positionHeight[3],
                            positionWidth[6]+(float)(1 - x_Z)*(positionWidth[3]-positionWidth[6]),
                            positionHeight[4], Color.GREEN);
                    drawCycle(positionWidth[6]+(float)(1 - y_Z)*(positionWidth[2]-positionWidth[6]), positionHeight[5], Color.MAGENTA,
                            positionWidth[6]+(float)(1 - x_Z)*(positionWidth[3]-positionWidth[6]), positionHeight[5], Color.GREEN);

                }

                //--------------------------
                float MomentRemoveOrAdd = 0;
                float noseWeight = (float) Integer.parseInt(edtNoseTailWeight.getText().toString());
                float leftWeight = (float) Integer.parseInt(edtMainsLeftWeight.getText().toString());
                float rightWeight = (float) Integer.parseInt(edtMainsRightWeight.getText().toString());
                float distanceZ = (float) Integer.parseInt(edtDistanceZ.getText().toString());
                float distanceX = (float) Integer.parseInt(edtDistanceX.getText().toString());
                float distanceY = (float) Integer.parseInt(edtDistanceY.getText().toString());
                MomentRemoveOrAdd = ((noseWeight*(distanceZ-distanceX) - (leftWeight+rightWeight)*distanceX));
                txtTotalWeight.setText(String.valueOf((int)(noseWeight+leftWeight+rightWeight))+"g");
                float weightRemoveOrAdd = 0;
                if(MomentRemoveOrAdd > 0){
                    txtNotificationNoseTailHeavy.setText("NOSE HEAVY");
                    imgArrayShowNoseHeave.setVisibility(View.VISIBLE);
                    imgArrayShowTailHeave.setVisibility(View.INVISIBLE);
                    if(distanceY > distanceX){
                        weightRemoveOrAdd = MomentRemoveOrAdd/(distanceY-distanceX);
                        txtNoticeAddOrRemove.setText("Remove");
                    }
                    else if(distanceY < distanceX){
                        weightRemoveOrAdd = MomentRemoveOrAdd/(distanceX-distanceY);
                        txtNoticeAddOrRemove.setText("Add");
                    }
                    else{
                        txtNotificationWeightRemove.setText(String.valueOf((int)0));
                        Toast.makeText(MainActivity.this, "X must != Y", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    txtNotificationWeightRemove.setText(String.valueOf((int)weightRemoveOrAdd));

                }else if(MomentRemoveOrAdd < 0){
                    imgArrayShowNoseHeave.setVisibility(View.INVISIBLE);
                    imgArrayShowTailHeave.setVisibility(View.VISIBLE);
                    txtNotificationNoseTailHeavy.setText("TAIL HEAVY");
                    if(distanceY > distanceX){
                        weightRemoveOrAdd = -MomentRemoveOrAdd/(distanceY-distanceX);
                        txtNoticeAddOrRemove.setText("Add");
                    }
                    else if(distanceY < distanceX){
                        weightRemoveOrAdd = -MomentRemoveOrAdd/(distanceX-distanceY);
                        txtNoticeAddOrRemove.setText("Remove");
                    }
                    else{
                        txtNotificationWeightRemove.setText(String.valueOf((int)0));
                        Toast.makeText(MainActivity.this, "X must != Y", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    txtNotificationWeightRemove.setText(String.valueOf((int)weightRemoveOrAdd));
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
    }

    private void drawLine(float x, float y, float xEnd, float yEnd, int color) {
        Canvas canvas = new Canvas(bmp);
        imgShowPlane.draw(canvas);
        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(3f);
        canvas.drawLine(x, y, xEnd, yEnd, p);
        imgShowPlane.setImageBitmap(bmp);
    }

    private void drawCycle(float x1, float y1, int color1, float x2, float y2, int color2) {
        Canvas canvas = new Canvas(bmp);
        imgShowPlane.draw(canvas);

        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(color1);
        int radius = 20;
        p.setStrokeWidth(3f);
        canvas.drawCircle(x1, y1, radius, p);
        p.setColor(color2);
        canvas.drawCircle(x2, y2, radius, p);
        imgShowPlane.setImageBitmap(bmp);
    }
    private void clearBitmap() {
        bmp.eraseColor(Color.TRANSPARENT);
        imgShowPlane.setImageBitmap(bmp);
    }

    public void LoadDataBegin() {
        listDataSaved = new ArrayList<>();
        dataSavedAdapter adapter = new dataSavedAdapter(this, R.layout.list_view_data_saved, listDataSaved);
        lvDataSaved.setAdapter(adapter);

        //Creating the ArrayAdapter instance having the country list
        spnAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item);
        spnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spnShowDataSaved.setAdapter(spnAdapter);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        flagSyncImage = sharedPreferences.getBoolean(syncImage, false);
        positionHeight[1] = sharedPreferences.getInt(positionHeight1, 0);
        positionHeight[2] = sharedPreferences.getInt(positionHeight2, 0);
        positionHeight[3] = sharedPreferences.getInt(positionHeight3, 0);
        positionHeight[4] = sharedPreferences.getInt(positionHeight4, 0);
        positionHeight[5] = sharedPreferences.getInt(positionHeight5, 0);
        positionHeight[6] = sharedPreferences.getInt(positionHeight6, 0);
        positionWidth[1] = sharedPreferences.getInt(positionWidth1, 0);
        positionWidth[2] = sharedPreferences.getInt(positionWidth2, 0);
        positionWidth[3] = sharedPreferences.getInt(positionWidth3, 0);
        positionWidth[4] = sharedPreferences.getInt(positionWidth4, 0);
        positionWidth[5] = sharedPreferences.getInt(positionWidth5, 0);
        positionWidth[6] = sharedPreferences.getInt(positionWidth6, 0);

        imgShowPlane.setDrawingCacheEnabled(true);
        imgShowPlane.buildDrawingCache(true);

        //------get background---------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(!sharedPreferences.getString(nameUriBackground, nameUriBackground).equals(nameUriBackground)){
                try {
//                Toast.makeText(MainActivity.this, sharedPreferences.getString(nameUriBackground, nameUriBackground), Toast.LENGTH_SHORT).show();
                    String path = sharedPreferences.getString(nameUriBackground, nameUriBackground);
                    Log.d("filename_path", path);
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(path));
//                    Log.d("filename_path", path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imgBackground.setImageBitmap(bitmap);
                } catch (Exception exception){
                    Log.d("filename_path", "cant convert");
                }
            }
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
        txtShowNameDataSaved = findViewById(R.id.txtShowNameDataSaved);

        rlSendRequestSaveName = findViewById(R.id.rlSendRequestSaveName);
        spnShowDataSaved = findViewById(R.id.spnShowDataSaved);
        edtNewNameSaved = findViewById(R.id.edtNewNameSaved);
        btnSendSavedNameRequest = findViewById(R.id.btnSendSavedNameRequest);
        btnCancelSavedNameRequest = findViewById(R.id.btnCancelSavedNameRequest);
        imgShowPlane = findViewById(R.id.imgShowPlane);

        imgSelectBackground = findViewById(R.id.imgSelectBackground);
        imgBackground = findViewById(R.id.imgBackground);
        imgRefreshBackground = findViewById(R.id.imgRefreshBackground);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(bmp == null && imgShowPlane.getWidth() > 0 && imgShowPlane.getHeight() > 0){
            bmp = Bitmap.createBitmap(imgShowPlane.getWidth(), imgShowPlane.getHeight(), Bitmap.Config.ARGB_8888);
            if(!flagSyncImage){
                Bitmap bitmap = imgShowPlane.getDrawingCache();
                Log.i("BitmapDrawable", "Height: " + String.valueOf(bitmap.getHeight()));
                Log.i("BitmapDrawable", "Width: " + String.valueOf(bitmap.getWidth()));
                int count = 0;
                for(int i = 0; i < bitmap.getHeight(); i++){
                    for(int j = 0; j < bitmap.getWidth(); j++){
                        int pixel = bitmap.getPixel(j,i);
                        int redValue = Color.red(pixel);
//                        Log.i("BitmapDrawable", "position: " + positionHeight[count] + " - " + positionWidth[count]);
//                        Log.i("BitmapDrawable", "position: " + redValue);
                        if(redValue >= 233){
//                            Log.i("BitmapDrawable", "position: " + i + " - " + j);
//                            Log.i("BitmapDrawable", "position: " + redValue);
                            if(i >= (positionHeight[count] + 10)){
                                count++;
                                positionHeight[count] = i;
                                positionWidth[count] = j;
                                Log.i("BitmapDrawable", "position: " + positionHeight[count] + " - " + positionWidth[count]);
                            }
                        }
                    }
                }
                if(count >= 6){
                    flagSyncImage = true;
                    editor.putBoolean(syncImage, true);
                    editor.putInt(positionHeight1, positionHeight[1]);
                    editor.putInt(positionHeight2, positionHeight[2]);
                    editor.putInt(positionHeight3, positionHeight[3]);
                    editor.putInt(positionHeight4, positionHeight[4]);
                    editor.putInt(positionHeight5, positionHeight[5]);
                    editor.putInt(positionHeight6, positionHeight[6]);
                    editor.putInt(positionWidth1, positionWidth[1]);
                    editor.putInt(positionWidth2, positionWidth[2]);
                    editor.putInt(positionWidth3, positionWidth[3]);
                    editor.putInt(positionWidth4, positionWidth[4]);
                    editor.putInt(positionWidth5, positionWidth[5]);
                    editor.putInt(positionWidth6, positionWidth[6]);
                    editor.commit();
                    Log.i("BitmapDrawable", "Load data Image OK");
                }
                else{
                    Toast.makeText(MainActivity.this, "Can't load data Image", Toast.LENGTH_SHORT).show();
                    imgShowPlane.setBackgroundResource(R.mipmap.plane_4);
                    return;
                }
            }
            for(int i = 0; i < 6; i++){
                Log.i("BitmapDrawable", "position: " + positionHeight[i+1] + " - " + positionWidth[i+1]);
            }
            imgShowPlane.setBackgroundResource(R.mipmap.plane_3);
            drawLine(positionWidth[1],positionHeight[1],positionWidth[4],positionHeight[4], Color.RED);
            drawLine(positionWidth[1],positionHeight[1],positionWidth[6],positionHeight[1], Color.RED);
            drawLine(positionWidth[6],positionHeight[1],positionWidth[6],positionHeight[4], Color.RED);

            drawLine(positionWidth[6]+(float)(positionWidth[2]-positionWidth[6])/3, positionHeight[2], positionWidth[2], positionHeight[2], Color.MAGENTA);
            drawLine(positionWidth[6]+(float)(positionWidth[2]-positionWidth[6])/3, positionHeight[2],
                    positionWidth[6]+(float)(positionWidth[2]-positionWidth[6])/3, positionHeight[4], Color.MAGENTA);

            drawLine(positionWidth[6]+(float)2*(positionWidth[3]-positionWidth[6])/3, positionHeight[3], positionWidth[3], positionHeight[3], Color.GREEN);
            drawLine(positionWidth[6]+(float)2*(positionWidth[3]-positionWidth[6])/3, positionHeight[3],
                    positionWidth[6]+(float)2*(positionWidth[3]-positionWidth[6])/3, positionHeight[4], Color.GREEN);

            drawCycle(positionWidth[6]+(float)((positionWidth[1]-positionWidth[6])/3), positionHeight[5], Color.MAGENTA,
                    positionWidth[6]+(float)2*(positionWidth[1]-positionWidth[6])/3, positionHeight[5], Color.GREEN);
        }

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
                        String urlListDevice = "https://avystore.com/appapi/user/user_list_devices";
                        flagGetDataSaved = true;
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("cookie", cookie)
                                .addFormDataPart("room_id", roomID)
                                .build();
                        postUrl(urlListDevice, requestBody);
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
//                                Log.i("responseBodyData", data);
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
                    if(flagSetDataSaved){
                        flagSetDataSaved = false;
                        JSONObject root = null;
                        try {
                            root = new JSONObject(myResponse);
                            String status = root.getString("status");
                            if(status.equals("ok")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rlSendRequestSaveName.setVisibility(View.INVISIBLE);
                                        prbLoadingUrl.setVisibility(View.INVISIBLE);
                                        txtShowNameDataSaved.setText(edtNewNameSaved.getText().toString());
                                        Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                String urlListDevice = "https://avystore.com/appapi/user/user_list_devices";
                                flagGetDataSaved = true;
                                RequestBody requestBody = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("cookie", cookie)
                                        .addFormDataPart("room_id", roomID)
                                        .build();
                                postUrl(urlListDevice, requestBody);
                            }
                            else if(status.equals("error")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rlSendRequestSaveName.setVisibility(View.INVISIBLE);
                                        prbLoadingUrl.setVisibility(View.INVISIBLE);
                                        Toast.makeText(MainActivity.this, "DATA NO CHANGE", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", 21);
        intent.putExtra("aspectY", 9);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length >0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectImageUri = data.getData();
//                Toast.makeText(MainActivity.this, selectImageUri.toString(), Toast.LENGTH_SHORT).show();
                if(selectImageUri != null){
                    try {
                        //content://com.google.android.apps.photos.contentprovider/-1/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F5286/ORIGINAL/NONE/image%2Fjpeg/600183246
                        editor.putString(nameUriBackground, selectImageUri.toString());
                        editor.commit();
                        InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                        Log.i("filename_path", selectImageUri.toString());

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imgBackground.setImageBitmap(bitmap);
//                        saveBitmap(bitmap);
//                        saveImage(bitmap, nameImageBackGround );
//                        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/background.png";
//                        Log.d("filename_path", fileName);
//                        File sd = Environment.getExternalStorageDirectory();
//                        File dest = new File(sd, fileName);
//
////                        bitmap = (Bitmap)data.getExtras().get("data");
//                        try {
//                            FileOutputStream out = new FileOutputStream(dest);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                            out.flush();
//                            out.close();
//                        } catch (Exception e) {
//                            Log.d("filename_path", "Exception Write");
//                            e.printStackTrace();
//                        }

                    } catch (Exception exception){

                    }
                }
            }
        }
    }
    //--------------------------------------------------



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