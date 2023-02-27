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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    ImageView imgArrayShowNoseHeave, imgArrayShowTailHeave, imgBackground, imgSetSavedFragmentBackground,
            imgDataSavedFragmentBackground, imgLoadDataSettingLocal;
    TextView txtNotificationNoseTailHeavy;

    Animation blinkTextview;

    String NoseWeigh = "NoseWeigh";
    String LeftWeight = "LeftWeight";
    String RightWeight = "RightWeight";
    String DistanceZ = "DistanceZ";
    String DistanceX = "DistanceX";
    String DistanceY = "DistanceY";

    File fileSaveText;
    String file_folder_name_save_setting = "CGBalance";
    String file_name_save_setting = "CGBalance.txt";
    int MAX_NUMBER_SETTING = 20;

    private List<dataSaved> listDataSaved;
    private ListView lvDataSaved;
    Button btnSaveData, btnLoadData;
    RelativeLayout rlDataSavedFragment, rlSendRequestSaveName;
    ImageView imgBackFragmentDataSaved, imgShowPlane, imgSelectBackground, imgRefreshBackground;
    TextView txtShowNameDataSaved;
    Spinner spnShowDataSaved;
    EditText edtNewNameSaved;
    Button btnSendSavedSettingToLocal, btnCancelSavedNameRequest;
    public static int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static int REQUEST_CODE_SELECT_IMAGE = 2;
    public static int REQUEST_CODE_WRITE_PERMISSION = 10;
    public static int REQUEST_CODE_READ_PERMISSION = 11;
    public static int REQUEST_CODE_SELECT_FILE_SETTING = 3;
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
        CheckPermission();
        initLayout();
        LoadDataBegin();
        btnLoadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.VISIBLE);
                updateDataToListViewSetting();
            }
        });

        imgBackFragmentDataSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlDataSavedFragment.setVisibility(View.INVISIBLE);
            }
        });
        imgLoadDataSettingLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                data.setType("*/*");
                data = Intent.createChooser(data, "Choose a file");
                if(data.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(data, REQUEST_CODE_SELECT_FILE_SETTING);
                }
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
                imgSetSavedFragmentBackground.setImageBitmap(bmpBackground);
                imgDataSavedFragmentBackground.setImageBitmap(bmpBackground);
                editor.putString(nameUriBackground, nameUriBackground);
                editor.commit();
            }
        });
        //--------------------------------------change background--------------------------------------------------------

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlSendRequestSaveName.setVisibility(View.VISIBLE);
                updateDataToListViewSetting();
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
        btnSendSavedSettingToLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtNewNameSaved.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Fill New Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(edtDistanceX.getText().toString().equals("")
                || edtDistanceY.getText().toString().equals("")
                || edtDistanceZ.getText().toString().equals("")){
                    rlSendRequestSaveName.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Save error, Fill Information Distant!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String dataContents = getDataSettingLocal();
                try {
                    JSONArray jsonArray = new JSONArray(dataContents);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", edtNewNameSaved.getText().toString());
                    jsonObject.put("X", Integer.parseInt(edtDistanceX.getText().toString()));
                    jsonObject.put("Y", Integer.parseInt(edtDistanceY.getText().toString()));
                    jsonObject.put("Z", Integer.parseInt(edtDistanceZ.getText().toString()));
                    jsonArray.put(spnShowDataSaved.getSelectedItemPosition(), jsonObject);
                    saveTextFileToLocal(jsonArray.toString());
                    saveTextFileToDownloadFolder(jsonArray.toString());
                    rlSendRequestSaveName.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btnCancelSavedNameRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                txtNotificationNoseTailHeavy.startAnimation(blinkTextview);
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
                    imgSetSavedFragmentBackground.setImageBitmap(bitmap);
                    imgDataSavedFragmentBackground.setImageBitmap(bitmap);
                } catch (Exception exception){
                    Log.d("filename_path", "cant convert");
                }
            }
        }
        //get data setting local begin
        readSettingFromLocalBegin();
    }

    void readSettingFromLocalBegin(){
        //Create file if not exist
//        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),file_folder_name_save_setting);
//        if (!fileDir.exists())
//        {
//            fileDir.mkdirs();
//        }
        fileSaveText = new File(MainActivity.this.getFilesDir(),file_name_save_setting);
        if (!fileSaveText.exists()){
            try{
                fileSaveText.createNewFile();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        //read data from file
        int length = (int) fileSaveText.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileSaveText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if(in != null){
                in.read(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String contentsLoadSettingLocalFile = getDataSettingLocal();
        if(contentsLoadSettingLocalFile.equals("")){
            JSONArray jsonArraySetting = new JSONArray();
            for(int i = 0; i < MAX_NUMBER_SETTING; i++){
                JSONObject jsonObjectSetting1 = new JSONObject();
                try {
                    jsonObjectSetting1.put("name", "Model " + String.valueOf(i+1));
                    jsonObjectSetting1.put("X", 5);
                    jsonObjectSetting1.put("Y", 10);
                    jsonObjectSetting1.put("Z", 15);
                    jsonArraySetting.put(jsonObjectSetting1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            contentsLoadSettingLocalFile = jsonArraySetting.toString();
            Log.i("jsonObject put", contentsLoadSettingLocalFile);
            //write data begin to local file
            saveTextFileToLocal(contentsLoadSettingLocalFile);
        }
        updateDataToListViewSetting();
    }

    void updateDataToListViewSetting(){
        String contentsLoadSettingLocalFile = getDataSettingLocal();
        try {
            JSONArray jsonArraySetting = new JSONArray(contentsLoadSettingLocalFile);
            listDataSaved.clear();
            for(int i = 0; i < MAX_NUMBER_SETTING; i++){
                JSONObject jsonObjectSetting = jsonArraySetting.getJSONObject(i);
                String name = jsonObjectSetting.getString("name");
                int X = jsonObjectSetting.getInt("X");
                int Y = jsonObjectSetting.getInt("Y");
                int Z = jsonObjectSetting.getInt("Z");
                listDataSaved.add(new dataSaved(i + 1, name, X, Y, Z));
            }
            lvDataSaved.invalidateViews();
            lvDataSaved.refreshDrawableState();
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, "Can,t load data setting", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    String getDataSettingLocal(){
        //read data from file
        int length = (int) fileSaveText.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileSaveText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String(bytes);
    }

    public void saveTextFileToLocal(String content){
        try {
            FileOutputStream fos = new FileOutputStream(fileSaveText);
            try {
                fos.write(content.getBytes());
                fos.close();
                Toast.makeText(MainActivity.this, "save!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "error save!", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "file not found!", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveTextFileToDownloadFolder(String content){
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),file_folder_name_save_setting);
//        Log.i("jsonObject put", fileDir.getName());
//        Log.i("jsonObject put", fileDir.getAbsolutePath());

        if(fileDir.exists()){
            fileDir.delete();
            if(fileDir.exists()){
                try {
                    fileDir.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(fileDir.exists()){
                    getApplicationContext().deleteFile(fileDir.getName());
                }
            }
        }
        if (!fileDir.exists())
        {
            fileDir.mkdirs();
        }
        File fileSaveTextDownload = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/" + file_folder_name_save_setting,file_name_save_setting);
        if(fileSaveTextDownload.exists()){
//            Toast.makeText(MainActivity.this, "exists!", Toast.LENGTH_SHORT).show();
            fileSaveTextDownload.deleteOnExit();
            fileSaveTextDownload.delete();
//            getApplicationContext().deleteFile(fileSaveTextDownload.getAbsolutePath());
//            getApplicationContext().deleteFile("/Download/CGBalance/CGBalance.txt");

        }
        try {
            FileOutputStream fos = new FileOutputStream(fileSaveTextDownload);
            try {
                fos.write(content.getBytes());
                fos.close();
                Toast.makeText(MainActivity.this, "save!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "error save!", Toast.LENGTH_SHORT).show();
                Log.i("jsonObject put", "error save!");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(MainActivity.this, "file not found!", Toast.LENGTH_SHORT).show();
            Log.i("jsonObject put", "file not found!");
        }
    }

    void CheckPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_PERMISSION);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_PERMISSION);
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
        txtShowNameDataSaved = findViewById(R.id.txtShowNameDataSaved);

        rlSendRequestSaveName = findViewById(R.id.rlSendRequestSaveName);
        spnShowDataSaved = findViewById(R.id.spnShowDataSaved);
        edtNewNameSaved = findViewById(R.id.edtNewNameSaved);
        btnSendSavedSettingToLocal = findViewById(R.id.btnSendSavedSettingToLocal);
        btnCancelSavedNameRequest = findViewById(R.id.btnCancelSavedNameRequest);
        imgShowPlane = findViewById(R.id.imgShowPlane);

        imgSelectBackground = findViewById(R.id.imgSelectBackground);
        imgBackground = findViewById(R.id.imgBackground);
        imgRefreshBackground = findViewById(R.id.imgRefreshBackground);

        imgSetSavedFragmentBackground = findViewById(R.id.imgSetSavedFragmentBackground);
        imgDataSavedFragmentBackground = findViewById(R.id.imgDataSavedFragmentBackground);
        imgLoadDataSettingLocal = findViewById(R.id.imgLoadDataSettingLocal);
        blinkTextview = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
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
        if(requestCode == REQUEST_CODE_WRITE_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(MainActivity.this, "Permission write granted!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Permission write denied!", Toast.LENGTH_SHORT).show();
//                finish();
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
                        imgSetSavedFragmentBackground.setImageBitmap(bitmap);
                        imgDataSavedFragmentBackground.setImageBitmap(bitmap);
                    } catch (Exception exception){

                    }
                }
            }
        }
        if(requestCode == REQUEST_CODE_SELECT_FILE_SETTING && resultCode == RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                byte[] bytes = getBytesFromUri(getApplicationContext(), uri);
                String dataFile = new String(bytes);
                saveTextFileToLocal(dataFile);
                updateDataToListViewSetting();
            }
        }
    }

    byte[] getBytesFromUri(Context context, Uri uri){
        InputStream iStream = null;
        try {
            iStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ( (len = iStream.read(buffer)) != -1){
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
    //--------------------------------------------------

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

}