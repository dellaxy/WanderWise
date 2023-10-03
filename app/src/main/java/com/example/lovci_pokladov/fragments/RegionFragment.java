package com.example.lovci_pokladov.fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.example.lovci_pokladov.models.ConstantsCatalog.SLOVAKIA_LOCATION;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lovci_pokladov.R;
import com.example.lovci_pokladov.models.ConstantsCatalog.ColorPalette;
import com.example.lovci_pokladov.models.Region;
import com.example.lovci_pokladov.objects.GeoJSONLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private GeoJSONLoader geoJSONLoader;
    private TextView regionNameTextView;
    private Map<Integer, Polygon> polygonMap;
    private int selectedRegionId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_regions, container, false);

        regionNameTextView = view.findViewById(R.id.regionNameText);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geoJSONLoader = new GeoJSONLoader(requireContext());
        polygonMap = new HashMap<>();
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_clean);
        mMap.setMapStyle(style);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SLOVAKIA_LOCATION, 6.5f));
        displayAllRegionsOnMap();
        getSelectedRegion();
    }

    private void getSelectedRegion(){
        SharedPreferences preferences = getActivity().getSharedPreferences("MapPreferences", MODE_PRIVATE);
        int selectedRegion = preferences.getInt("selectedRegion", -1);
        if (selectedRegion != -1) {
            Polygon selectedPolygon = polygonMap.get(selectedRegion);
            if (selectedPolygon != null) {
                setSelectedRegion(selectedPolygon);
            }
        }
    }

    private void displayAllRegionsOnMap() {
        List<Region> regions = geoJSONLoader.getRegions();
        if (regions != null) {
            for (Region region : regions) {
                Bundle regionInfo = new Bundle();
                regionInfo.putInt("regionId", region.getId());
                regionInfo.putString("regionName", region.getName());

                PolygonOptions regionBoundary = geoJSONLoader.getRegionPolygon(region.getId());
                regionBoundary.fillColor(ColorPalette.PRIMARY.getColor(128));
                regionBoundary.strokeColor(ColorPalette.PRIMARY.getColor(255));
                regionBoundary.strokeWidth(8);

                Polygon regionPolygon = mMap.addPolygon(regionBoundary);
                regionPolygon.setClickable(true);
                regionPolygon.setTag(regionInfo);

                polygonMap.put(region.getId(), regionPolygon);

                mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
                    @Override
                    public void onPolygonClick(@NonNull Polygon clickedPolygon) {
                        setSelectedRegion(clickedPolygon);
                    }
                });
            }
        }
    }

    private void setSelectedRegion(Polygon selectedPolygon){
        Bundle regionInfo = (Bundle) selectedPolygon.getTag();
        if(selectedRegionId == -1) {
            changePolygonColor(selectedPolygon, ColorPalette.PRIMARY.getColor(220));
        } else if (selectedRegionId == regionInfo.getInt("regionId")) {
            changePolygonColor(selectedPolygon, ColorPalette.PRIMARY.getColor(128));
            selectedRegionId = -1;
            this.regionNameTextView.setText("Slovakia");
            return;
        } else {
            Polygon previousSelectedPolygon = polygonMap.get(selectedRegionId);
            changePolygonColor(previousSelectedPolygon, ColorPalette.PRIMARY.getColor(128));
            changePolygonColor(selectedPolygon, ColorPalette.PRIMARY.getColor(220));
        }
        this.regionNameTextView.setText(regionInfo.getString("regionName"));
        selectedRegionId = regionInfo.getInt("regionId");
    }

    private void changePolygonColor(Polygon polygon, int color) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), polygon.getFillColor(), color);
        colorAnimator.setDuration(300);

        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedColor = (int) animator.getAnimatedValue();
                polygon.setFillColor(animatedColor);
                polygon.setStrokeColor(Color.argb(255, Color.red(animatedColor), Color.green(animatedColor), Color.blue(animatedColor)));
            }
        });

        colorAnimator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = requireActivity().getSharedPreferences("MapPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selectedRegion", selectedRegionId);
        editor.apply();
        selectedRegionId = -1;
    }


}
