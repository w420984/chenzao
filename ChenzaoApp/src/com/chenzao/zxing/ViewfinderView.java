/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chenzao.zxing;

import com.xiaosajie.chenzao.R;
import com.google.zxing.ResultPoint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
	private Context context;


  private static final long ANIMATION_DELAY = 100L;
  private static final int OPAQUE = 0xFF;

  private final Paint paint;
  private Bitmap resultBitmap;
  private final int resultPointColor;
  private Bitmap bmp_Scan_Rect = null;
  private Bitmap bmp_Scan_Line = null;
  private Collection<ResultPoint> possibleResultPoints;
  private Collection<ResultPoint> lastPossibleResultPoints;
  public static final int COLOR_MASK = 0x77000000;
  private int scan_line_offsetY;

  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
	super(context, attrs);
	this.context = context;
	
	// Initialize these once for performance rather than calling them every time in onDraw().
	paint = new Paint();
	Resources resources = getResources();
	resultPointColor = resources.getColor(R.color.possible_result_points);
	possibleResultPoints = new HashSet<ResultPoint>(5);
  }

  @Override
  public void onDraw(Canvas canvas) {
    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(COLOR_MASK);
    paint.setAntiAlias(true);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
    canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
    canvas.drawRect(0, frame.bottom, width, height, paint);

    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(OPAQUE);
      canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
    } else {
        // 绘制扫描框
        if (bmp_Scan_Rect != null && !bmp_Scan_Rect.isRecycled()) {
            canvas.drawBitmap(bmp_Scan_Rect, frame.left, frame.top, paint);
        }

        // 绘制扫描线
        if (scan_line_offsetY == 0){
        	scan_line_offsetY = frame.top;
        }
        if (bmp_Scan_Line != null && !bmp_Scan_Line.isRecycled()) {    
            canvas.drawBitmap(bmp_Scan_Line, frame.left+2, scan_line_offsetY, paint);
        }
        scan_line_offsetY += 5;
        if (scan_line_offsetY >= frame.top + frame.height()){
        	scan_line_offsetY = frame.top;
        }
        
        // 绘制文字说明
        paint.setColor(Color.WHITE);
        paint.setTextSize(this.context.getResources().getDimension(
                R.dimen.zxing_scan_info_size));

        float bmpRectBottomMargin = this.context.getResources().getDimension(
                R.dimen.zxing_rect_bottom_margin);

        String info1 = this.context.getResources().getString(
                R.string.scan_hint);

        Rect rect = new Rect();
        paint.getTextBounds(info1, 0, 1, rect);
        canvas.drawText(info1, (width - paint.measureText(info1)) / 2,
                frame.bottom + bmpRectBottomMargin, paint);
        
/*
      Collection<ResultPoint> currentPossible = possibleResultPoints;
      Collection<ResultPoint> currentLast = lastPossibleResultPoints;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
        possibleResultPoints = new HashSet<ResultPoint>(5);
        lastPossibleResultPoints = currentPossible;
        paint.setAlpha(OPAQUE);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentPossible) {
          canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
        }
      }
      if (currentLast != null) {
        paint.setAlpha(OPAQUE / 2);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentLast) {
          canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
        }
      }
*/
      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }
  }

  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  @Override
protected void onAttachedToWindow() {
	// TODO Auto-generated method stub
	super.onAttachedToWindow();
    // 初始化扫描框和扫描线
    if(bmp_Scan_Rect == null || ( bmp_Scan_Rect!=null && bmp_Scan_Rect.isRecycled())){
        bmp_Scan_Rect = BitmapFactory.decodeResource(getResources(),
                R.drawable.qrcode_focus);
    }
    if(bmp_Scan_Line == null || (bmp_Scan_Line!=null && bmp_Scan_Line.isRecycled())){        
        bmp_Scan_Line = BitmapFactory.decodeResource(getResources(),
                R.drawable.qrcode_line);	
    }	
    Rect frame = CameraManager.get().getFramingRect();
    if (frame != null) {
        scan_line_offsetY = frame.top;
    }
}

@Override
protected void onDetachedFromWindow() {
	// TODO Auto-generated method stub
	super.onDetachedFromWindow();
    if(bmp_Scan_Rect != null) {
        bmp_Scan_Rect.recycle();
        bmp_Scan_Rect = null;
    }
    if(bmp_Scan_Line != null) {
        bmp_Scan_Line.recycle();
        bmp_Scan_Line = null;
    }	
}

/**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    possibleResultPoints.add(point);
  }

}
