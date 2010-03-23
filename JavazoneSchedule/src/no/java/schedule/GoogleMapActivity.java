package no.java.schedule;

import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * Created by IntelliJ IDEA.
 * User: erlend
 * Date: Mar 17, 2010
 * Time: 10:51:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleMapActivity extends MapActivity {

    MapController mc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemaps);

        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mc = mapView.getController();

        mc.setCenter(new GeoPoint(59912972,10757733));
        mc.setZoom(17);
        
    }


    //@Override

    protected boolean isRouteDisplayed() {
        return false;
    }


}
