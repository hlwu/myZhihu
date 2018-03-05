package com.hlwu.myapp.draw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawView extends View
{

	Paint mPaint = null;		// used to draw line
	Paint mBitmapPaint = null; 	// used to draw bitmap
	Path mPath = null;			// save the point
	Bitmap mBitmap = null;		// used as choosing picture
	Bitmap mBottomBitmap = null;// used as bottom background
	Canvas mCanvas = null;		// what's it used for
	float posX,posY;			// used as touched position
	private final float TOUCH_TOLERANCE = 4;
	
	private DrawPath mDrawPath = null;
	private List<DrawPath> mSavePath 	= null;
	private List<DrawPath> mDeletePath	= null;
	private String mImagePath = null;
	
	private int mImageWidth = 480;
	private int mImageHeight = 800;
	private int mBottomBitmapDrawHeight = 0;
	
	private boolean isFirstDraw = true;
	private Context mContext = null;
	
    public Context getmContext() {
        return mContext;
    }
    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    //+s For double click
	private boolean hasMoved = false;
    int clickCount = 0;
    long preClickTime = 0;
    //+e For double click
	
	public DrawView(Context context,AttributeSet attr,int defStyle)
	{
		super(context,attr,defStyle);
		Log.d("flaggg", "1");
		init();
	}
	public DrawView(Context context,AttributeSet attr)
	{
		super(context,attr);
        Log.d("flaggg", "2");
		init();
	}
	
	public DrawView(Context context)
	{
		super(context);
        Log.d("flaggg", "3");
		init();
	}
	
	private void init()
	{
		mPaint = new Paint();
	    mPaint.setAntiAlias(true);
	    mPaint.setDither(true);
	    mPaint.setColor(0xFFCCCCCC);
//	    mPaint.setStyle(Paint.Style.FILL);//起点→路径→终点, 包围起来的图形是填充的
        mPaint.setStyle(Paint.Style.STROKE);
	    mPaint.setStrokeJoin(Paint.Join.ROUND);
	    mPaint.setStrokeCap(Paint.Cap.ROUND);
	    mPaint.setStrokeWidth(12);
	    
	    mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	    
	    mSavePath = new ArrayList<DrawPath>();
	    mDeletePath = new ArrayList<DrawPath>();
	    mImagePath = initPath();

	}
	
    public void setNaviBarVisible(Activity activity, boolean visible) {
        View decorView = activity.getWindow().getDecorView();
        if (decorView != null) {
            int flag = decorView.getSystemUiVisibility();
            if (visible) {
                flag = flag & ~
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION & ~
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION & ~
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flag = flag |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(flag);
        }
    }
	
	private String initPath()
	{
		String ph = Environment.getExternalStorageDirectory().getAbsolutePath();
		if(ph == null)
		{
			return null;
		}
		ph += "/ddxxtuya";
		File imageFile = new File(ph);
		if( !imageFile.exists() )
		{
			imageFile.mkdir();
		}
		return ph;
	}
	
	private class DrawPath
	{
		Path path;
		Paint paint;
	}
	
	@Override
	protected void onSizeChanged(int w,int h,int oldw,int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d("flaggg", "onSizeChanged : " + w + "    " + h + "    " + oldw + "    " + oldh);
//		if(mBottomBitmap == null)
		mBottomBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//		if(mCanvas == null)
		mCanvas = new Canvas(mBottomBitmap);
        mImageWidth = mBottomBitmap.getWidth();
        mImageHeight = mBottomBitmap.getHeight();
        Log.d("flaggg", "onSizeChanged : "+mBottomBitmap.getWidth()+"    "+mBottomBitmap.getHeight());
	}
	
//	public void setBitmap(int w, int h) {
//	    mBottomBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBottomBitmap);
//        Log.d("flaggg", "setBitmap: "+mCanvas.getWidth()+"    "+mCanvas.getHeight());
//	}
	
	@Override
	public void onDraw(Canvas canvas) {
        Log.d("flaggg", "onDraw : "+canvas.getWidth()+"    "+canvas.getHeight());

        if(isFirstDraw) {
            canvas.drawColor(0x00000000);
            isFirstDraw = false;
        }
		int nCanvasWidth 	= canvas.getWidth();
		int nCanvasHeight 	= canvas.getHeight();
		int nBitmapWidth 	= mBottomBitmap.getWidth();
		int nBitmapHeight 	= mBottomBitmap.getHeight();
		mBottomBitmapDrawHeight = (nCanvasHeight - nBitmapHeight)/2;
		canvas.drawBitmap(mBottomBitmap,0,mBottomBitmapDrawHeight,mBitmapPaint);
		if(mPath != null)
		{
			canvas.drawPath(mPath, mPaint);	
		}
		
		
//		canvas.drawRect(10,10,100,100,mPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		float y = event.getY();
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			    Log.d("flaggg", "DOWN");
				mPath = new Path();
				mDrawPath = new DrawPath();
				mPath.moveTo(x, y);
				mDrawPath.paint = new Paint(mPaint);
				mDrawPath.path	= mPath;
				posX = x;
				posY = y;
				postInvalidate();
				
				break;
			case MotionEvent.ACTION_MOVE:
				Log.d("flaggg", "MOVE");
				float dx = Math.abs(x - posX);
				float dy = Math.abs(y - posY);
				if(dx >= TOUCH_TOLERANCE || dy > TOUCH_TOLERANCE)
				{
					mPath.quadTo(posX, posY, (x + posX)/2, (y + posY)/2);
					posX = x;
					posY = y;
					hasMoved = true;
				}
				postInvalidate();
				break;
			case MotionEvent.ACTION_UP:
                Log.d("flaggg", "UP");
				mPath.lineTo(posX, posY);
				mPath.offset(0, -mBottomBitmapDrawHeight);
//				 avoid the previous line is cleared when press again
				mCanvas.drawPath(mPath, mPaint); //It will keep the path don't dismiss.
				mSavePath.add(mDrawPath);
				mPath = null;
				postInvalidate();
				
				//++s. Click event
				if(!hasMoved) {
				    if (clickCount == 0 || (System.currentTimeMillis() - preClickTime) > 500) {
				        Log.d("flaggg", "click once");
				        preClickTime = System.currentTimeMillis();
				        clickCount = 1;
				    } else if (clickCount == 1) {
				        Log.d("flaggg", "click twice");
				        long curTime = System.currentTimeMillis();
				        if((curTime - preClickTime) < 500){
				            doubleClick();
				        }
				        clickCount = 0;
				        preClickTime = 0;
				    }else{
				        Log.d("flaggg", "wrong clickCount = " + clickCount);
				        clickCount = 0;
				        preClickTime = 0;
				    }
				}
				hasMoved = false;
				//++e. Click event
				break;
		}
		return true;
	}
	
    private void doubleClick() {
        Log.d("flaggg", "double click");
            Log.d("flaggg", "show floating btn");
            Intent showIntent = new Intent(getmContext(), TopWindowService.class);
            showIntent.putExtra(TopWindowService.OPERATION,
                    TopWindowService.OPERATION_SHOW_OR_NOT);
            getmContext().startService(showIntent);
//            getmContext().bindService(showIntent, sc, Context.BIND_AUTO_CREATE);
    }
    
//    private ServiceConnection sc = new ServiceConnection() {
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d("flaggg", "DialerService connected !");
//        }
//
//        public void onServiceDisconnected(ComponentName name) {
//            Log.d("flaggg", "DialerService disconnected !");
//        }
//    };
	
	public boolean setBitmap(String imagePath)
//    public boolean setBitmap(InputStream imagePath)
	{
	    
	    Log.d("flaggg", "setBitmap " + imagePath);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//        Bitmap bitmap = BitmapFactory.decodeStream(imagePath);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float nxScale = -1;
		float nyScale = -1;
		Log.d("flaggg", "setBitmap width: " + width + "; height: " + height);
        Log.d("flaggg", "setBitmap mImageWidth: " + mImageWidth + "; mImageHeight: " + mImageHeight);
		if( width!=0 && height!=0)
		{
//			nxScale = (float)width/mImageWidth;	
//			nyScale = (float)height/mImageHeight;
//	        Log.d("flaggg", "setBitmap nxScale: " + nxScale + "; nyScale: " + nyScale);
//			if (nxScale>=1 && nyScale >=1 || nxScale<1 && nyScale<1)
//			{
////				if(nxScale > nyScale)
////				{
//					width = (int)(width/nxScale);
////					height = (int)(height/nxScale);
////				}
////				else
////				{
////					width = (int)(width/nyScale);
//					height = (int)(height/nyScale);
////				}
//				
//			}
//			if (nxScale >=1 && nyScale <1)
//			{
//				width = mImageWidth;
//			}
//			if(nxScale <=1 && nyScale >=1)
//			{
//				height = mImageHeight;
//			}
//			mBitmap = Bitmap.createScaledBitmap(bitmap,width,height,true);
//			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);		
            mBitmap = Bitmap.createScaledBitmap(bitmap,mImageWidth,mImageHeight,true);
            mBottomBitmap = Bitmap.createBitmap(mImageWidth,mImageHeight,Bitmap.Config.ARGB_8888);  
			mSavePath.clear();
			mDeletePath.clear();
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
			postInvalidate();

			return true;
		}
		else {
			return false;
		}
		
	}
	
	public void setBitmapColor(int color)
	{
		mBottomBitmap.eraseColor(color);
		mSavePath.clear();
		mDeletePath.clear();
		postInvalidate();
	}
	
	public void setPaint(Paint paint)
	{
	    if(paint != null)
	        mPaint = paint;
	    
		postInvalidate();
	}
	
	public void saveImage(String imagePath)
	{
		if (mImagePath == null || mBitmap == null)
		{
			return;
		}
		String imageName = null;
		int nStart = imagePath.lastIndexOf('/');
		int nEnd   = imagePath.lastIndexOf('.');
		
		imageName = imagePath.substring(nStart,nEnd);
		imageName += ".png";
		imageName = mImagePath + imageName;
		File file = new File(imageName);
		
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			mBottomBitmap.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clearImage()
	{
	    Log.d("flaggg", "clearImage");
		mSavePath.clear();
		mDeletePath.clear();
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			
		}
		postInvalidate();
		
	}
	
	public void undo()
	{
	    Log.d("flaggg", "undo");
		int nSize = mSavePath.size();
		if (nSize >= 1)
		{
			mDeletePath.add(0, mSavePath.get(nSize-1) );
			mSavePath.remove(nSize -1);
		}
		else
			return;
		
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
		}
		
		Iterator<DrawPath> iter = mSavePath.iterator();
		DrawPath temp;
		while(iter.hasNext())
		{
			temp = iter.next();
			mCanvas.drawPath(temp.path, temp.paint);
		}
		postInvalidate();
		
	}
	
	public void redo()
	{
	    Log.d("flaggg", "redo");
		int nSize = mDeletePath.size();
		if (nSize >= 1)
		{
			mSavePath.add( mDeletePath.get(0) );
			mDeletePath.remove(0);
		}
		else
			return;
		
		
		if(mBitmap != null)
		{
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
			mCanvas.drawBitmap(mBitmap, 0,0, mBitmapPaint);
		}
		else
		{
			int width = mCanvas.getWidth();
			int height = mCanvas.getHeight();
			mBottomBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBottomBitmap);
		}
		
		Iterator<DrawPath> iter = mSavePath.iterator();
		DrawPath temp;
		while(iter.hasNext())
		{
			temp = iter.next();
			mCanvas.drawPath(temp.path, temp.paint);
		}
		postInvalidate();
	}
}
