/**
 * @author Chris_Cui 2014-06-03
 */

package com.champion.mipis.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalMessage implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    public static final int FROM = 0; // received.

    public static final int TO = 1; // sent.
    
    public static final int TEXT = 3; // Text.

    public static final int PIC = 4; // picture.

    public static final int AUDIO = 5; // audio.

    public static final int VIDEO = 6; // video.

    public String msg = null;

    public boolean isReaded = false;

    public long mMills = 0;

    public int personID = 0; // users

    public int fromTo = 0;

    public int type = 0;

    public LocalMessage(String msg, long receivedMills, int personID, int fromTo) {
        this.msg = msg;
        this.mMills = receivedMills;
        this.personID = personID;
        this.fromTo = fromTo;
    }

    @Override
    public String toString() {
        return "Message is : " + msg + ", mMills = " + mMills + ", personID = "
                + personID + ", fromTo = " + ((fromTo == FROM) ? "FROM" : "TO" + ", type = " + type);
    }

    public String getTimeString() {
        Date date = new Date(mMills);
        Date curDate = new Date();

        String time = "";

        if (curDate.getDate() == date.getDate()) {
            time = new SimpleDateFormat("HH:mm:ss").format(date);
        } else {
            time = new SimpleDateFormat("MM-dd").format(new Date());
        }
        return time;

    }

    @Override
    public int compareTo(Object arg0) {
        LocalMessage s = (LocalMessage) arg0;
        return mMills < s.mMills ? 1 : (mMills == s.mMills ? 0 : -1);
    }
}
