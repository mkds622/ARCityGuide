package com.project.meetu.arcityguide;

import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.app.DialogFragment;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.support.v13.app.FragmentCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.Manifest;
/**
 * Created by Meetu on 17-02-2017.
 */

public class ARFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback{
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
            ORIENTATIONS.append(Surface.ROTATION_0, 90);
            ORIENTATIONS.append(Surface.ROTATION_90, 0);
            ORIENTATIONS.append(Surface.ROTATION_180, 270);
            ORIENTATIONS.append(Surface.ROTATION_270, 180);
            }
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String TAG = "CameraBasicActivity";
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
            }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
            }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
            }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }

            };

    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private File mFile;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

    @Override
    public void onImageAvailable(ImageReader reader) {
            //mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            }

            };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
            }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
            activity.finish();
            }
            }

            };
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    //private int mState = STATE_PREVIEW;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private int mSensorOrientation;


    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

    private void process(CaptureResult result) {

            }};
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
            int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

            // Collect the supported resolutions that are at least as big as the preview Surface
            List<Size> bigEnough = new ArrayList<>();
            // Collect the supported resolutions that are smaller than the preview Surface
            List<Size> notBigEnough = new ArrayList<>();
            int w = aspectRatio.getWidth();
            int h = aspectRatio.getHeight();
            for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
            option.getHeight() == option.getWidth() * h / w) {
            if (option.getWidth() >= textureViewWidth &&
            option.getHeight() >= textureViewHeight) {
            bigEnough.add(option);
            } else {
            notBigEnough.add(option);
            }
            }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
            } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
            } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
            }
            }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ar, container, false);
            }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
            mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
            }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
            super.onResume();
            startBackgroundThread();

            // When the screen is turned off and turned back on, the SurfaceTexture is already
            // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
            // a camera and start preview from here (otherwise, we wait until the surface is ready in
            // the SurfaceTextureListener).
            if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
            }

    @Override
    public void onPause() {
            closeCamera();
            stopBackgroundThread();
            super.onPause();
            }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getActivity().getFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
    @NonNull int[] grantResults) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getActivity().getFragmentManager(), FRAGMENT_DIALOG);
            }
            } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            }
    public static ARFragment newInstance() {
            return new ARFragment();
            }
    private void setUpCameraOutputs(int width, int height) {
            Activity activity = getActivity();
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
            for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics
            = manager.getCameraCharacteristics(cameraId);

            // We don't use a front facing camera in this sample.
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
            continue;
            }

            StreamConfigurationMap map = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
            continue;
            }

            // For still image captures, we use the largest available size.
            Size largest = Collections.max(
            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
            new CompareSizesByArea());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
            ImageFormat.JPEG, /*maxImages*/2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
            if (mSensorOrientation == 90 || mSensorOrientation == 270) {
            swappedDimensions = true;
            }
            break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
            if (mSensorOrientation == 0 || mSensorOrientation == 180) {
            swappedDimensions = true;
            }
            break;
    default:
            Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
            rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
            maxPreviewHeight, largest);

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(
            mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
            mTextureView.setAspectRatio(
            mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            // Check if the flash is supported.
            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;

            mCameraId = cameraId;
            return;
            }
            } catch (CameraAccessException e) {
            e.printStackTrace();
            } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
                ErrorDialog.newInstance(getString(R.string.camera_error)).show(getActivity().getFragmentManager(), FRAGMENT_DIALOG);
            }
            }

    /*
     * Opens the camera specified by {@link Camera2BasicFragment#mCameraId}.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCamera(int width, int height) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
            }
            setUpCameraOutputs(width, height);
            configureTransform(width, height);
            Activity activity = getActivity();
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
            e.printStackTrace();
            } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
            }

    /**
     * Closes the current {@link android.hardware.camera2.CameraDevice}.
     */
    private void closeCamera() {
            try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
            }
            if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
            }
            } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
            } finally {
            mCameraOpenCloseLock.release();
            }
            }

    /**
     * Starts a background thread and its {@link android.os.Handler}.
     */
    private void startBackgroundThread() {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            }

    /**
     * Stops the background thread and its {@link android.os.Handler}.
     */
    private void stopBackgroundThread() {
            mBackgroundThread.quitSafely();
            try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
            } catch (InterruptedException e) {
            e.printStackTrace();
            }
            }

    /**
     * Creates a new {@link android.hardware.camera2.CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
            try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
            = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
            new CameraCaptureSession.StateCallback() {

    @Override
    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            // The camera is already closed
            if (null == mCameraDevice) {
            return;
            }

            // When the session is ready, we start displaying the preview.
            mCaptureSession = cameraCaptureSession;
            try {
            // Auto focus should be continuous for camera preview.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // Flash is automatically enabled when necessary.
            setAutoFlash(mPreviewRequestBuilder);

            // Finally, we start displaying the camera preview.
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
            e.printStackTrace();
            }
            }

    @Override
    public void onConfigureFailed(
    @NonNull CameraCaptureSession cameraCaptureSession) {
            //showToast("Failed");
            }
            }, null
            );
            } catch (CameraAccessException e) {
            e.printStackTrace();
            }
            }
    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
            if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }
            }
    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
            Activity activity = getActivity();
            if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
            }
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
            RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();
            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
            (float) viewHeight / mPreviewSize.getHeight(),
            (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
            }
            mTextureView.setTransform(matrix);
            }

    /**
     * Initiate a still image capture.
     */
        /*
        private void takePicture() {
            lockFocus();
        }*/
    private int getOrientation(int rotation) {
            // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
            // We have to take that into account and rotate JPEG properly.
            // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
            // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
            return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
            }
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(
                                    new String[]{android.Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }
}

