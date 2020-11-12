package app.intelehealth.client.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public abstract class NetworkChangeListener extends BroadcastReceiver {


    @Override
    public void onReceive( Context context, Intent intent) {
        String status = NetworkUtils.getConnectivityStatusString(context);
//        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        onNetworkChange(status);

    }
    protected abstract void  onNetworkChange(String status);
}
