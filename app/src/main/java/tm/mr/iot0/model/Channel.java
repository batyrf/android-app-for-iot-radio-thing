package tm.mr.iot0.model;

import android.support.annotation.NonNull;

/**
 * Created by viridis on 27.04.2018.
 */

public class Channel {

    private String sTitle;
    private String sUri;

    public Channel(String sTitle, String sUri) {
        this.sTitle = sTitle;
        this.sUri = sUri;
    }

    @NonNull
    public String getTitle() {
        return sTitle;
    }

    public void setTitle(@NonNull String sTitle) {
        this.sTitle = sTitle;
    }

    @NonNull
    public String getsUri() {
        return sUri;
    }

    public void setsUri(@NonNull String sUri) {
        this.sUri = sUri;
    }

    @Override
    public String toString() {
        return getTitle() + ":" + getsUri();
    }
}
