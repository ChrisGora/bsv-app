package uk.ac.bris.cs.bristolstreetview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

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
import org.joda.time.DateTime;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConcretePhotoTaker implements CameraConnectorObserver, PhotoTaker {

    private static final String TAG = "PhotoTaker";

    private List<PhotoTakerObserver> mObservers;

    private CameraConnector mCameraConnector;
    private CameraInfo mCameraInfo;

    private Context mContext;

    ConcretePhotoTaker(Context context) {

        mContext = context;

        mObservers = new ArrayList<>();

        mCameraConnector = new ConcreteCameraConnector(Volley.newRequestQueue(context), "http://192.168.1.1");
        mCameraConnector.registerObserver(this);
        mCameraConnector.updateCameraInfo();
    }

    @Override
    public void updateCameraInfo() {
        mCameraConnector.updateCameraInfo();
    }

    @Override
    public void updateCameraState() {
        mCameraConnector.updateCameraState();
    }

    @Override
    public void setShutterVolume(int volume) {
        mCameraConnector.setShutterVolume(volume);
    }

    @Override
    public void sendTakePhotoRequest(PhotoRequest photoRequest) {
        mCameraConnector.sendTakePhotoRequest(photoRequest);
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
    public void onTakePhotoInProgress(PhotoRequest photoRequest, CameraOutput output) {
        Log.i(TAG, "onTakePhotoInProgress: id: " + output.getId());
        Log.i(TAG, "onTakePhotoInProgress: name: " + output.getName());
        Log.i(TAG, "onTakePhotoInProgress: completion: " + output.getProgress().getCompletion());
        Log.i(TAG, "onTakePhotoInProgress: state: " + output.getState());
    }

    @Override
    public void onTakePhotoError(PhotoRequest photoRequest, CameraOutput output) {
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getCode());
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getMessage());
    }

    @Override
    public void onTakePhotoDone(PhotoRequest photoRequest, CameraOutput output) {
        String url = output.getResults().getFileUrl();
        photoRequest.setCameraUrl(url);
        onPhotoTakenAll(photoRequest);
        Log.i(TAG, "onTakePhotoDone: " + url);
        mCameraConnector.requestDownloadPhotoAsBytes(photoRequest);
    }

    @Override
    public void onPhotoAsBytesDownloaded(PhotoRequest photoRequest, byte[] photo) {
        Log.i(TAG, "onPhotoAsBytesDownloaded: Something worked!");
        saveBytesAsImage(photoRequest, photo);
    }

    private void saveBytesAsImage(PhotoRequest photoRequest, byte[] bytes) {
        Log.d(TAG, "saveBytesAsImage: HERE");
//        long instant = new Date().getTime();
        String filename = getFilename(photoRequest);
        long lengthOfFile = bytes.length;
        Log.d(TAG, "saveBytesAsImage: length of byte array: " + lengthOfFile);
        try {
            InputStream input = new ByteArrayInputStream(bytes);
            File path = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Ricoh");
            boolean done = path.exists() || path.mkdirs();

            if (done) {
                processInputStream(input, filename, path);
                input.close();
                File file = new File(path, filename);
                readImageMetadata(file);
                updateImageMetadata(file, photoRequest);
                readImageMetadata(new File(path, appendFilename(filename)));
                String fullPath = path + File.separator + appendFilename(filename);
                photoRequest.setDevicePath(fullPath);
                onPhotoSavedAndProcessedAll(photoRequest);

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


    private void updateImageMetadata(File file, PhotoRequest photoRequest) throws ImageProcessingException, IOException, XMPException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file.getParent(), appendFilename(file.getName()))))) {

            TiffOutputSet outputSet = new TiffOutputSet();
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


//                AndroidThreeTen.init(mContext);

                DateTime time = photoRequest.getTime();

//                LocalDateTime time = LocalDateTime.now();
                String timeString = time.toString().replace("-", ":").replace("T", " ").substring(0, 19);
                Log.d(TAG, "updateImageMetadata: TIMESTRING: " + timeString);

                UUID uuid = UUID.randomUUID();
                String uuidString = uuid.toString().replace("-", "");
                Log.d(TAG, "updateImageMetadata: uuid: " + uuidString);

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID, uuidString);

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, timeString);

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, timeString);

                exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
                exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, timeString);

                double longitude = photoRequest.getLocation().getLongitude();
                double latitude = photoRequest.getLocation().getLatitude();
//                double longitude = 10;
//                double latitude = 20 ;

                if (longitude != 0 && latitude != 0) {
                    outputSet.setGPSInDegrees(longitude, latitude);


                    new ExifRewriter().updateExifMetadataLossless(file, out, outputSet);
                }

                // TODO: 24/07/18 Get data from other sensors as well

            }
        } catch (ImageReadException | ImageWriteException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    String getFilename(PhotoRequest photoRequest) {
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(new Date(photoRequest.getTime().toInstant().getMillis()));
        String filename = mCameraInfo.getSerialNumber() + " " + date.replace(":", "-");
        filename = filename + ".jpg";
//        Log.d(TAG, "getFilename: DATE: " + date);
        Log.d(TAG, "getFilename: FILENAME: " + filename);
        return filename;
    }

    private String appendFilename(String filename) {
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


    @Override
    public void registerObserver(PhotoTakerObserver observer) {
        if (mObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
        else mObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    @Override
    public void removeObserver(PhotoTakerObserver observer) {
        if (mObservers.contains(Objects.requireNonNull(observer, "Observer to deregister was null")))
            mObservers.remove(observer);
        else throw new IllegalArgumentException("The observer to be deregistered isn't already registered");
    }

    private void onPhotoTakenAll(PhotoRequest photoRequest) {
        for (PhotoTakerObserver observer : mObservers) {
            observer.onPhotoTaken(photoRequest);
        }
    }

    private void onPhotoSavedAndProcessedAll(PhotoRequest photoRequest) {
        for (PhotoTakerObserver observer : mObservers) {
            observer.onPhotoSavedAndProcessed(photoRequest);
        }
    }

}
