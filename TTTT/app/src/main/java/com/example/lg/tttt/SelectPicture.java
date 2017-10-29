package com.example.lg.tttt;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class SelectPicture extends AppCompatActivity implements ListView.OnScrollListener, GridView.OnItemClickListener{

    boolean mBusy = false;
    ProgressDialog mLoagindDialog;
    GridView mGvImageList;
    ImageAdapter mListAdapter;
    ArrayList<ThumbImageInfo> mThumbImageInfoList;
    long time=0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);

        Intent intent = getIntent();
        time= intent.getLongExtra("time",0);
        mThumbImageInfoList = new ArrayList<ThumbImageInfo>();
        mGvImageList = (GridView) findViewById(R.id.gvImageList);
        mGvImageList.setOnScrollListener(this);
        mGvImageList.setOnItemClickListener(this);

        new DoFindImageList().execute();
    }

    private long findThumbList()
    {
        long returnValue = 0;
        Time t = new Time();
        t.set(0, 0, 0, 28, 9, 2017);    //초, 분, 시간, 일, 월(0~11), 년도, 2013년 12월 1일로 셋팅

        long start = t.toMillis(true);

        Time e = new Time();
        e.set(0, 0, 0, 12, 7, 2017);    //초, 분, 시간, 일, 월(0~11), 년도, 2013년 12월 1일로 셋팅

        long end = t.toMillis(true);

        String where = MediaStore.Images.Media.DATE_TAKEN +" >= " + start+"and"
                + MediaStore.Images.Media.DATE_TAKEN + "<="+end;


        String selection = MediaStore.Images.Media.DATE_TAKEN + " >="
                + start + " AND "
                + MediaStore.Images.Media.DATE_TAKEN + " <= "
                + end;

        // Select 하고자 하는 컬럼
        String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };

        // 쿼리 수행
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATE_TAKEN + " >=" + time,
                null, MediaStore.Images.Media.DATE_ADDED + " desc ");
        /*
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATE_TAKEN +" >= ? and"
                        + MediaStore.Images.Media.DATE_TAKEN + "<=?",
                new String[] {start + "", end+ ""}, MediaStore.Images.Media.DATE_ADDED + " desc ");
        */

        if (imageCursor != null && imageCursor.getCount() > 0)
        {
            // 컬럼 인덱스
            int imageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
            int imageDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);

            // 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
            // 리스트에 더해준다.
            while (imageCursor.moveToNext())
            {
                ThumbImageInfo thumbInfo = new ThumbImageInfo();

                thumbInfo.setId(imageCursor.getString(imageIDCol));
                thumbInfo.setData(imageCursor.getString(imageDataCol));
                thumbInfo.setCheckedState(false);

                mThumbImageInfoList.add(thumbInfo);
                returnValue++;
            }
        }
        imageCursor.close();
        return returnValue;
    }

    // 화면에 이미지들을 뿌려준다.
    private void updateUI()
    {
        mListAdapter = new ImageAdapter (this, R.layout.image_cell, mThumbImageInfoList);
        mGvImageList.setAdapter(mListAdapter);
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {}

    // 스크롤 상태를 판단한다.
    // 스크롤 상태가 IDLE 인 경우(mBusy == false)에만 이미지 어댑터의 getView에서
    // 이미지들을 출력한다.
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        switch (scrollState)
        {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mBusy = false;
                mListAdapter.notifyDataSetChanged();
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                break;
        }
    }

    // 아이템 체크시 현재 체크상태를 가져와서 반대로 변경(true -> false, false -> true)시키고
    // 그 결과를 다시 ArrayList의 같은 위치에 담아준다
    // 그리고 어댑터의 notifyDataSetChanged() 메서드를 호출하면 리스트가 현재 보이는
    // 부분의 화면을 다시 그리기 시작하는데(getView 호출) 이러면서 변경된 체크상태를
    // 파악하여 체크박스에 체크/언체크를 처리한다.
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    {
        ImageAdapter adapter = (ImageAdapter) arg0.getAdapter();
        ThumbImageInfo rowData = (ThumbImageInfo)adapter.getItem(position);
        boolean curCheckState = rowData.getCheckedState();

        rowData.setCheckedState(!curCheckState);

        mThumbImageInfoList.set(position, rowData);
        adapter.notifyDataSetChanged();
    }

    // ***************************************************************************************** //
    // Image Adapter Class
    // ***************************************************************************************** //
    static class ImageViewHolder
    {
        ImageView ivImage;
        CheckBox chkImage;
    }

    private class ImageAdapter extends BaseAdapter
    {
        static final int VISIBLE = 0x00000000;
        static final int INVISIBLE = 0x00000004;
        private Context mContext;
        private int mCellLayout;
        private LayoutInflater mLiInflater;
        private ArrayList<ThumbImageInfo> mThumbImageInfoList;
        @SuppressWarnings("unchecked")
        private LRUCache mCache; // 캐쉬

        public ImageAdapter(Context c, int cellLayout, ArrayList<ThumbImageInfo> thumbImageInfoList)
        {
            mContext = c;
            mCellLayout = cellLayout;
            mThumbImageInfoList = thumbImageInfoList;

            mLiInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 캐쉬 초기화 : 캐쉬의 최대 보관 크기 30개
            mCache = new LRUCache<String, Bitmap>(30);
        }

        public int getCount()
        {
            return mThumbImageInfoList.size();
        }

        public Object getItem(int position)
        {
            return mThumbImageInfoList.get(position);
        }

        public long getItemId(int position)
        {
            return position;
        }

        @SuppressWarnings("unchecked")
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = mLiInflater.inflate(mCellLayout, parent, false);
                ImageViewHolder holder = new ImageViewHolder();

                holder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
                holder.chkImage = (CheckBox) convertView.findViewById(R.id.chkImage);

                convertView.setTag(holder);
            }

            final ImageViewHolder holder = (ImageViewHolder) convertView.getTag();

            if (((ThumbImageInfo) mThumbImageInfoList.get(position)).getCheckedState())
                holder.chkImage.setChecked(true);
            else
                holder.chkImage.setChecked(false);

            if (!mBusy)
            {
                try
                {
                    String path = ((ThumbImageInfo) mThumbImageInfoList.get(position)).getData();
                    Bitmap bmp = (Bitmap) mCache.get(path); // 일단 캐쉬에서 Bitmap 객체를 가져온다.

                    // bmp 객체를 캐쉬에서 가져 왔다면 그것을 그대로 사용하고
                    if (bmp != null)
                    {
                        holder.ivImage.setImageBitmap(bmp);
                    }
                    // 캐쉬에 내용이 없다면 실제 파일로 Bitmap객체를 만들어 주고 이를
                    // 캐쉬에 넣어준다.
                    else
                    {
                        BitmapFactory.Options option = new BitmapFactory.Options();

                        if (new File(path).length() > 100000)
                            option.inSampleSize = 10;
                        else
                            option.inSampleSize = 2;

                        bmp = BitmapFactory.decodeFile(path, option);
                        holder.ivImage.setImageBitmap(bmp);

                        mCache.put(path, bmp); // 캐쉬에 넣어준다.
                    }

                    holder.ivImage.setVisibility(VISIBLE);
                    setProgressBarIndeterminateVisibility(false);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    setProgressBarIndeterminateVisibility(false);
                }
            }
            else
            {
                setProgressBarIndeterminateVisibility(true);
                holder.ivImage.setVisibility(INVISIBLE);
            }

            return convertView;
        }
    }
    // ***************************************************************************************** //
    // Image Adapter Class End
    // ***************************************************************************************** //

    // ***************************************************************************************** //
    // AsyncTask Class
    // ***************************************************************************************** //
    private class DoFindImageList extends AsyncTask<String, Integer, Long>
    {
        @Override
        protected void onPreExecute()
        {
            mLoagindDialog = ProgressDialog.show(SelectPicture.this, null, "로딩중...", true, true);
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(String... arg0)
        {
            long returnValue = 0;
            returnValue = findThumbList();
            return returnValue;
        }

        @Override
        protected void onPostExecute(Long result)
        {
            updateUI();
            mLoagindDialog.dismiss();
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }
    }
    // ***************************************************************************************** //
    // AsyncTask Class End
    // ***************************************************************************************** //
}
