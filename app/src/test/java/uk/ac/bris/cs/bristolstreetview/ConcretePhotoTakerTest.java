package uk.ac.bris.cs.bristolstreetview;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class ConcretePhotoTakerTest {

    @Test
    public void timestampTest() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        String monthSubstring;
        if (month >= 10) {
            monthSubstring = "";
        } else {
            monthSubstring = "0";
        }

        String timestamp = year + ":" + monthSubstring + month + ":" +day + " " + hours + ":" + minutes;

        System.out.println(timestamp);

    }
}