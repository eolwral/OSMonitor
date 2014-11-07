package com.eolwral.osmonitor.ui;

import com.android.volley.toolbox.NetworkImageView;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.util.HttpUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionStaticMapFragment extends Fragment {

  public static String LONGTIUDE = "Longtiude";
  public static String LATITUDE = "Latitude";
  public static String MESSAGE = "Message";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.ui_connection_static_map, container,
        false);

    String lat = Float.toString(getArguments().getFloat(LATITUDE));
    String lon = Float.toString(getArguments().getFloat(LONGTIUDE));
    String msg = getArguments().getString(MESSAGE);

    // detect map type
    String targetURL = "";
    Settings setting = Settings.getInstance(getActivity());
    if (!setting.getMapType().equals("GoogleMap"))
      targetURL = "http://ojw.dev.openstreetmap.org/StaticMap/?lat=" + lat
          + "&lon=" + lon + "&z=9&mode=Add+icon&" + "mlat0=" + lat + "&mlon0="
          + lon + "&show=1&w=640&h=600";
    else
      targetURL = "https://maps.google.com/maps/api/staticmap?center=" + lat
          + "," + lon + "&zoom=8&markers=" + lat + "," + lon
          + "&size=640x600&sensor=false&scale=1";

    // load image
    NetworkImageView mapView = (NetworkImageView) view
        .findViewById(R.id.id_connection_static_map);
    mapView.setImageUrl(targetURL,
        HttpUtil.getInstance(getActivity().getApplicationContext())
            .getImageLoader());

    // set information
    TextView WhoisInfo = (TextView) view
        .findViewById(R.id.id_connection_static_info);
    WhoisInfo.setText(Html.fromHtml(msg));

    return view;
  }

}
