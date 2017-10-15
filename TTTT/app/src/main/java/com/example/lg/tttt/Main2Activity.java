package com.example.lg.tttt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public int inSampleSize = 1;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private static String basePath;

    public float imageViewRotation = 90;
    public String TAG = "Camera Example :: ";

    private Button takePicBtn;
    private ImageView resultView;
    private TextView imgPath;
    private Gallery customGallery;
    private CustomGalleryAdapter customGalAdapter;

    private String[] imgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // App.을 실행하자 마자 지정한 경로의 생성 및 접근에 용이하도록 아래와 같이 생성
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");

        //File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        /*
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
//                return null;
            }
        }
        */
        basePath = mediaStorageDir.getPath();


        imgPath = (TextView)findViewById(R.id.imgpath);
        resultView = (ImageView)findViewById(R.id.resultview);
        takePicBtn = (Button)findViewById(R.id.takepicbtn);


        File file = new File(basePath);
        imgs = file.list();

        for(int i=0; i<imgs.length; i++){
            imgPath.setText(imgs[i]);
        }

        customGallery = (Gallery)findViewById(R.id.customgallery); // activity_main.xml에서 선언한 Gallery를 연결
        customGalAdapter = new CustomGalleryAdapter(getApplicationContext(), basePath); // 위 Gallery에 대한 Adapter를 선언
        customGallery.setAdapter(customGalAdapter); // Gallery에 위 Adapter를 연결



        // Gallery의 Item을 Click할 경우 ImageView에 보여주도록 함
        customGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bitmap bm = BitmapFactory.decodeFile(basePath+ File.separator +imgs[position]);
                Bitmap bm2 = ThumbnailUtils.extractThumbnail(bm, bm.getWidth() / inSampleSize, bm.getHeight() / inSampleSize);
                resultView.setImageBitmap(bm2);
                imgPath.setText(basePath+File.separator+imgs[position]);
            }
        });
        // Gallery의 Item을 LongClick할 경우 해당 File을 삭제하도록 함.
        // 이 부분에서 동작은 하나, 삭제 후 결과가 View에 반영이 안되어 추가 보완 필요
        customGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File temp = new File(basePath+File.separator+imgs[position]);
                temp.delete();
                return false;
            }
        });
    }
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }
}
