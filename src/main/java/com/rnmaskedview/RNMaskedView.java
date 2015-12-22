package com.rnmaskedview;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.controller.ForwardingControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.view.ReactViewGroup;

/*package*/ class RNMaskedView extends ReactViewGroup implements DataSubscriber<CloseableReference<CloseableImage>> {

    private Resources mResources;
    private Drawable mMaskImage = null;
    private Bitmap mMaskBitmap = null;
    private Paint mPaint;
    private int mOldWidth = -1, mOldHeight = -1;
    private PorterDuffXfermode mDuffMode;

    public RNMaskedView(ReactContext reactContext) {
        super(reactContext);

        mResources = reactContext.getResources();

        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null); //Only works for software layers
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDuffMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    }

    public void loadMask(ReadableMap source) {
        if (source.hasKey("uri")) {
            String uriString = source.getString("uri");
            double scale = 1;

            if (source.hasKey("scale")) {
                scale = source.getDouble("scale");
            }

            Uri uri = Uri.parse(uriString);

            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
//                    .setPostprocessor(postprocessor)
//                    .setResizeOptions(resizeOptions)
                    .setProgressiveRenderingEnabled(false)
                    .build();

            ImagePipeline pipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>> ds = pipeline.fetchDecodedImage(imageRequest, null);
            ds.subscribe(this, UiThreadImmediateExecutorService.getInstance());
        }
        else {
            Log.e("RNMaskedView", "Couldnt set mask image, no uri in source");
        }
    }

    //Drawing
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mMaskImage != null) {
            if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                maybeReloadBitmapMask();

            mPaint.setXfermode(mDuffMode);
                canvas.drawBitmap(mMaskBitmap, 0.0f, 0.0f, mPaint);
            mPaint.setXfermode(null);
            }
        }
    }

    private void maybeReloadBitmapMask() {
        if (getMeasuredWidth() != mOldWidth || getMeasuredHeight() != mOldHeight) {

            if (mMaskBitmap != null && !mMaskBitmap.isRecycled()) {
                mMaskBitmap.recycle();
            }

            mMaskBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mMaskBitmap);
            mMaskImage.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            mMaskImage.draw(canvas);

            mOldWidth = getMeasuredWidth();
            mOldHeight = getMeasuredHeight();
        }
    }

    public void onNewResult(DataSource<CloseableReference<CloseableImage>> dataSource) {
        Log.e("RNMaskedView", "Image loaded?");

        if (dataSource.isFinished()) {
            Log.e("RNMaskedView", "Image loaded!!!!");

            CloseableReference<CloseableImage> imageRef = dataSource.getResult();
            mMaskImage = createDrawable(imageRef);
        }
    }

    public void onFailure(DataSource<CloseableReference<CloseableImage>> dataSource) {
        Log.e("RNMaskedView", "Failed to load image");
    }

    public void onCancellation(DataSource<CloseableReference<CloseableImage>> dataSource) {
        Log.e("RNMaskedView", "Image cancelled");
    }

    public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
        Log.e("RNMaskedView", "Progress update");
    }

    private Drawable createDrawable(CloseableReference<CloseableImage> image) {
        Preconditions.checkState(CloseableReference.isValid(image));
        CloseableImage closeableImage = image.get();

        if (closeableImage instanceof CloseableStaticBitmap) {
            CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) closeableImage;
            BitmapDrawable bitmapDrawable = new BitmapDrawable(
                    mResources,
                    closeableStaticBitmap.getUnderlyingBitmap());
            if (closeableStaticBitmap.getRotationAngle() == 0 ||
                    closeableStaticBitmap.getRotationAngle() == EncodedImage.UNKNOWN_ROTATION_ANGLE) {
                return bitmapDrawable;
            } else {
                return new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle());
            }
        }
        else if (closeableImage instanceof CloseableAnimatedImage) {
            Log.e("RNMaskedView", "Attempted to use animated mask image, not supported");
        }
        else {
            throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
        }

        return null;
    }

}
