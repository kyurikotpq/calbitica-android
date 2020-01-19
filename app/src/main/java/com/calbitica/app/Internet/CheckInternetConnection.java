package com.calbitica.app.Internet;

import android.app.Application;

public class CheckInternetConnection extends Application {
    private static CheckInternetConnection mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    // Getter
    public static synchronized CheckInternetConnection getInstance() {
        return mInstance;
    }

    // Setter
    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
