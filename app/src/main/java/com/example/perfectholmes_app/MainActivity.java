package com.example.perfectholmes_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Marker clickedMarker;
    private CircleOverlay circleOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // 목포의 좌표를 사용하여 지도 초기화
        LatLng initialPosition = new LatLng(34.7915, 126.3922);
        naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(initialPosition));

        // 지도를 클릭할 때 이벤트 처리
        naverMap.setOnMapClickListener((point, coord) -> {
            if (clickedMarker != null) {
                clickedMarker.setMap(null); // 이전 마커 삭제
                clickedMarker = null;
                if (circleOverlay != null) {
                    circleOverlay.setMap(null); // 이전 원 삭제
                    circleOverlay = null;
                }
            } else {
                // 클릭된 위치에 마커 추가
                clickedMarker = new Marker();
                clickedMarker.setPosition(new LatLng(coord.latitude, coord.longitude));
                clickedMarker.setMap(naverMap);

                // 클릭된 위치를 중심으로 반경 1km의 원 추가
                circleOverlay = new CircleOverlay();
                circleOverlay.setCenter(new LatLng(coord.latitude, coord.longitude));
                circleOverlay.setRadius(1000); // 반경 1km
                circleOverlay.setColor(Color.argb(50, 0, 0, 255)); // 반투명한 파란색
                circleOverlay.setMap(naverMap);
            }
        });
    }
}
