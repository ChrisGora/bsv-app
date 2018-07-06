package uk.ac.bris.cs.bristolstreetview;

import android.app.Activity;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class SinglePhotoActivityTest {

@Mock
CameraInfo mockCameraInfo;

@InjectMocks
private SinglePhotoActivity activity;

@Rule
public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getFilename() {
        when(mockCameraInfo.getSerialNumber()).thenReturn("12345");
        activity.getFilename();
    }
}