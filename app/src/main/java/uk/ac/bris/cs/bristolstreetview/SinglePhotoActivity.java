package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.android.volley.toolbox.Volley;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.xmp.XmpDirectory;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
//import javax.inject.Inject;

public class SinglePhotoActivity extends AppCompatActivity implements CameraConnectorObserver{

    private static final String TAG = "SinglePhotoActivity";

    private Button mTakePhotoButton;
    private Button mUpdateInfoButton;
    private Button mUpdateStateButton;
    private Button mMuteButton;
    private Button mFullVolumeButton;
    private ImageView mResponseImageView;

    private CameraConnector mCameraConnector;
    private DownloadManager mDownloadManager;

//    @Inject
    private CameraInfo mCameraInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo);

        findAllViews();
        setAllOnClickListeners();

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add((Manifest.permission.READ_EXTERNAL_STORAGE));
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        checkPermissions(permissions);

        mCameraConnector = new ConcreteCameraConnector(Volley.newRequestQueue(this), "http://192.168.1.1");
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mCameraConnector.registerObserver(this);

        mCameraConnector.updateCameraInfo();



    }

    private void findAllViews() {
        mTakePhotoButton = findViewById(R.id.take_photo_button);
        mUpdateInfoButton = findViewById(R.id.update_info_button);
        mUpdateStateButton = findViewById(R.id.update_state_button);
        mMuteButton = findViewById(R.id.mute_button);
        mFullVolumeButton = findViewById(R.id.full_volume_button);

        mResponseImageView = findViewById(R.id.response_image_view);
    }

    private void setAllOnClickListeners() {
        mTakePhotoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Take photo pressed");
            mCameraConnector.sendTakePhotoRequest();
        });

        mUpdateInfoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update info pressed");
            mCameraConnector.updateCameraInfo();
        });

        mUpdateStateButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update state pressed");
            mCameraConnector.updateCameraState();
        });

        mMuteButton.setOnClickListener((view) -> {
            Log.v(TAG, "Mute pressed");
            mCameraConnector.setShutterVolume(0);
        });

        mFullVolumeButton.setOnClickListener((view) -> {
            Log.v(TAG, "Full volume pressed");
            mCameraConnector.setShutterVolume(100);
        });
    }

    private void checkPermissions(List<String> permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, permission + " NOT granted");
                ActivityCompat.requestPermissions(this, new String[] {permission}, 0);
            } else {
                Log.v(TAG, permission + " granted");
            }
        }
    }

    @Override
    public void onCameraInfoUpdated(CameraInfo newCameraInfo) {
        mCameraInfo = newCameraInfo;

        Log.v(TAG, "CALLED ON INFO UPDATED");
        Log.i(TAG, "Serial Number: " + newCameraInfo.getSerialNumber());
        Log.i(TAG, "Firmware Version: " + newCameraInfo.getFirmwareVersion());
        Log.i(TAG, "GPS Present? : " + newCameraInfo.getGpsPresent());
        Log.i(TAG, "Gyro Present? : " + newCameraInfo.getGyroPresent());
        Log.i(TAG, "Model: " + newCameraInfo.getModel());
    }

    @Override
    public void onCameraStateUpdated(CameraState newCameraState) {
        Log.i(TAG, "fingerprint: " + newCameraState.getFingerprint());
        Log.i(TAG, "session ID: " + newCameraState.getState().getSessionId());
        Log.i(TAG, "battery level: " + newCameraState.getState().getBatteryLevel());
    }


    @Override
    public void onTakePhotoInProgress(CameraOutput output) {
        Log.i(TAG, "onTakePhotoInProgress: id: " + output.getId());
        Log.i(TAG, "onTakePhotoInProgress: name: " + output.getName());
        Log.i(TAG, "onTakePhotoInProgress: completion: " + output.getProgress().getCompletion());
        Log.i(TAG, "onTakePhotoInProgress: state: " + output.getState());
    }

    @Override
    public void onTakePhotoError(CameraOutput output) {
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getCode());
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getMessage());
    }

    @Override
    public void onTakePhotoDone(CameraOutput output) {
        String url = output.getResults().getFileUrl();
        Log.i(TAG, "onTakePhotoDone: " + url);
        displayImage(url);
//        downloadImage(output.getResults().getFileUrl(), output.getId());
        mCameraConnector.requestPhotoAsBytes(url);
    }

    @Override
    public void onPhotoAsBytesDownloaded(byte[] photo) {
        Log.i(TAG, "onPhotoAsBytesDownloaded: Something worked!");
        saveBytesAsImage(photo);
    }

    private void displayImage(String url) {
        Picasso
                .get()
                .load(url)
                .resize(500, 500)
                .into(mResponseImageView);
    }

    private void saveBytesAsImage(byte[] bytes) {
        Log.d(TAG, "saveBytesAsImage: HERE");
        String filename = getFilename();
        long lengthOfFile = bytes.length;
        Log.d(TAG, "saveBytesAsImage: length of byte array: " + lengthOfFile);
        int n = 0;
        while (n < 100) {
            Log.d(TAG, "saveBytesAsImage: byte: " + bytes[n]);
            n++;
        }
        try {
            InputStream input = new ByteArrayInputStream(bytes);
//            File path = ;
            File path = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Ricoh");
            boolean done = path.exists() || path.mkdirs();

            if (done) {
                processInputStream(input, filename, path);
                input.close();
                File file = new File(path, filename);
                readImageMetadata(file);
                updateImageMetadata(file);
                readImageMetadata(new File(path, appendFilename(filename)));
            } else {
                Log.e(TAG, "saveBytesAsImage: FAILED TO CREATE A DIRECTORY");
            }
        } catch (IOException | ImageProcessingException | XMPException e) {
            e.printStackTrace();
        }

    }

    private void processInputStream(InputStream input, String filename, File path) throws IOException, ImageProcessingException, XMPException {
        File file = new File(path, filename);
        Log.d(TAG, "saveBytesAsImage: path " + path);
        Log.d(TAG, "saveBytesAsImage: filename " + filename);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        byte data[] = new byte[1024];

        long total = 0;

        int count = input.read(data);

        while (count != -1) {
            total = total + count;
            output.write(data, 0, count);
            count = input.read(data);
        }

        output.flush();
        output.close();


    }


    private void readImageMetadata(File file) throws ImageProcessingException, IOException, XMPException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                Log.i(TAG, "readImageMetadata: " + directory.getName() + " " + tag.getTagName() + " " + tag.getDescription());
            }

            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    Log.e(TAG, "readImageMetadata: Metadata error: " + error);
                }
            }

            if (directory.getName().equals("XMP")) {
                Log.i(TAG, "readImageMetadata: XMP DETECTED");
                XmpDirectory xmpDirectory = (XmpDirectory) directory;
                XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
                XMPIterator iterator = xmpMeta.iterator();
                while (iterator.hasNext()) {
                    XMPPropertyInfo info = (XMPPropertyInfo) iterator.next();
                    Objects.requireNonNull(info);
                    Log.i(TAG, "readImageMetadata: XMP: " + info.getPath() + " " + info.getValue());
                }
            }
        }
    }


    private void updateImageMetadata(File file) throws ImageProcessingException, IOException, XMPException {
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file.getParent(), appendFilename(file.getName()))));
            TiffOutputSet outputSet = new TiffOutputSet();

//            File newFile = new File(file.getParent(), "123.jpg");
//            BufferedInputStream bis= new BufferedInputStream(new FileInputStream(newFile));

//            InputStream in = new FileInputStream(file);
//            Log.d(TAG, "updateImageMetadata: available " + in.available());
//            int i1 = in.read();
//            int i2 = in.read();
//            int i3 = in.read();

//            while (true) {
//                int i = in.read();
//                Log.i(TAG, "updateImageMetadata: " + i);
//            }

//            Log.d(TAG, "updateImageMetadata: NewFile: " + i1 + "   " + i2 + "   " + i3);

            final IImageMetadata imageMetadata = Imaging.getMetadata(file);
            final JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(file);
            if (metadata != null) {
                TiffImageMetadata exif = metadata.getExif();
                if (exif != null) {
                    outputSet = exif.getOutputSet();
                }

                final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE, new RationalNumber(3, 1));

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_GAMMA);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_GAMMA, new RationalNumber(100, 1));

                UUID uuid = UUID.randomUUID();
                String uuidString = uuid.toString().replace("-", "");
                Log.d(TAG, "updateImageMetadata: uuid: " + uuidString);

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID, uuidString);
                new ExifRewriter().updateExifMetadataLossless(file, out, outputSet);

            }
        } catch (ImageReadException | ImageWriteException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    String getFilename() {
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(new Date());
        String filename = mCameraInfo.getSerialNumber() + " " + date.replace(":", "-");
        filename = filename + ".jpg";
//        Log.d(TAG, "getFilename: DATE: " + date);
        Log.d(TAG, "getFilename: FILENAME: " + filename);
        return filename;
    }

    String appendFilename(String filename) {
        String newFilename = filename.replace(".jpg", "_E.jpg");
        Log.d(TAG, "appendFilename: OLD: " + filename);
        Log.d(TAG, "appendFilename: NEW: " + newFilename);
        return newFilename;
    }

    private void logTagValue(final JpegImageMetadata metadata, final TagInfo tagInfo) {
        TiffField field = metadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            Log.e(TAG, "logTagValue: TAG NOT FOUND: " + tagInfo);
        } else {
            Log.i(TAG, "logTagValue: TAG: " + tagInfo.name + ": " + field.getValueDescription());
        }
    }

/*
    private void downloadImage(String url, String id) {
        Log.d(TAG, "downloadImage: url " + url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        Log.d(TAG, "downloadImage: uri " + Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        request.setTitle("Downloading photo");
//        request.setDescription("Bristol StreetView is downloading an image...");
//        request.setVisibleInDownloadsUi(true);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, "photo.jpg");
        long refid = mDownloadManager.enqueue(request);
    }
*/

    private void downloadImage(String url, String id) {
        Target target = new ImageDownloaderTarget(url);
        Picasso
                .get()
                .load(url)
                .into(target);
    }

    /*private void sendStringGetRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl + "/osc/info",
                (response) -> mResponseTextView.setText(response),
                (response) -> mResponseTextView.setText("That didn't work :-(")
        );
        mQueue.add(stringRequest);
    }

    private void sendJsonPostRequest() {
        String url = mUrl + "/osc/state";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                (response) -> {
            mResponseTextView.setText(response.toString());
                    try {
                        String fingerprint = response.getString("fingerprint");
                        JSONObject state = response.getJSONObject("state");
                        Number batteryLevel = state.getDouble("batteryLevel");
                        Log.v("TAG", ">>>> " + fingerprint);
                        Log.v("TAG", ">>>> BATTERY LEVEL: " + batteryLevel);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (response) -> mResponseTextView.setText("That didn't work :-(")
        );
        mQueue.add(request);
    }
*/




}
