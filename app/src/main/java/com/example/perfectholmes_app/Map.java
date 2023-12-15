package com.example.perfectholmes_app;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    private Marker searchMarker;
    private Marker touchMarker;
    private CircleOverlay touchCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        // 검색창에서 주소 검색
        EditText editText = findViewById(R.id.editTextTextPersonName);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String address = editText.getText().toString();
                if (!address.isEmpty()) {
                    searchAddress(address);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 초기 지도 설정
        LatLng initialPosition = new LatLng(37.5665, 126.9780); // 초기 위치 (예: 서울)
        naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition));

        // 지도 클릭 이벤트 처리
        naverMap.setOnMapClickListener((point, coord) -> {
            // 터치된 위치에 마커와 반경 500m의 원 추가 또는 제거
            toggleMarkerAndCircleAtLocation(coord.latitude, coord.longitude);
        });
    }

    private void searchAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address resultAddress = addresses.get(0);
                LatLng resultLatLng = new LatLng(resultAddress.getLatitude(), resultAddress.getLongitude());

                // 검색된 주소의 좌표로 지도 이동
                naverMap.moveCamera(CameraUpdate.scrollTo(resultLatLng));

                // 검색된 위치에 마커 추가
                addSearchMarkerAtLocation(resultLatLng.latitude, resultLatLng.longitude);
            } else {
                // 검색 결과가 없는 경우 처리
                showErrorMessage("입력하신 주소가 없습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorMessage(String message) {
        // Toast 메시지로 에러 메시지 표시
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void addSearchMarkerAtLocation(double latitude, double longitude) {
        // 이전에 생성된 검색 마커 제거
        if (searchMarker != null) {
            searchMarker.setMap(null);
            searchMarker = null;
        }

        // 검색된 위치에 새로운 마커 추가
        searchMarker = new Marker();
        searchMarker.setPosition(new LatLng(latitude, longitude));
        searchMarker.setMap(naverMap);
    }

    private void toggleMarkerAndCircleAtLocation(double latitude, double longitude) {
        // 터치된 위치에 마커 및 반경 500m의 원 추가 또는 제거
        if (touchMarker != null) {
            touchMarker.setMap(null);
            touchMarker = null;
            if (touchCircle != null) {
                touchCircle.setMap(null);
                touchCircle = null;
            }
        } else {
            touchMarker = new Marker();
            touchMarker.setPosition(new LatLng(latitude, longitude));
            touchMarker.setMap(naverMap);

            touchCircle = new CircleOverlay();
            touchCircle.setCenter(new LatLng(latitude, longitude));
            touchCircle.setRadius(500); // 반경 500m
            touchCircle.setColor(Color.argb(50, 255, 0, 0)); // 반투명한 빨간색
            touchCircle.setMap(naverMap);
        }
    }
}




//
//
//마커에 지도 표시, 검색 창 없음
//public class Map extends AppCompatActivity implements OnMapReadyCallback {
//
//    private Marker clickedMarker;
//    private CircleOverlay circleOverlay;
//    private NaverMap naverMap;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map);
//
//        FragmentManager fm = getSupportFragmentManager();
//        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
//        if (mapFragment == null) {
//            mapFragment = MapFragment.newInstance();
//            fm.beginTransaction().add(R.id.map, mapFragment).commit();
//        }
//
//        mapFragment.getMapAsync(this);
//
//        // 검색창에서 주소 검색
//        EditText editText = findViewById(R.id.editTextTextPersonName);
//        editText.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                String address = editText.getText().toString();
//                if (!address.isEmpty()) {
//                    searchAddress(address);
//                    return true;
//                }
//            }
//            return false;
//        });
//    }
//
//    @Override
//    public void onMapReady(@NonNull NaverMap naverMap) {
//        // 목포의 좌표를 사용하여 지도 초기화
//        LatLng initialPosition = new LatLng(34.7915, 126.3922);
//        naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(initialPosition));
//
//        // 지도를 클릭할 때 이벤트 처리
//        naverMap.setOnMapClickListener((point, coord) -> {
//            if (clickedMarker != null) {
//                clickedMarker.setMap(null); // 이전 마커 삭제
//                clickedMarker = null;
//                if (circleOverlay != null) {
//                    circleOverlay.setMap(null); // 이전 원 삭제
//                    circleOverlay = null;
//                }
//            } else {
//                // 클릭된 위치에 마커 추가
//                clickedMarker = new Marker();
//                clickedMarker.setPosition(new LatLng(coord.latitude, coord.longitude));
//                clickedMarker.setMap(naverMap);
//
//                // 클릭된 위치를 중심으로 반경 1km의 원 추가
//                circleOverlay = new CircleOverlay();
//                circleOverlay.setCenter(new LatLng(coord.latitude, coord.longitude));
//                circleOverlay.setRadius(1000); // 반경 1km
//                circleOverlay.setColor(Color.argb(50, 0, 0, 255)); // 반투명한 파란색
//                circleOverlay.setMap(naverMap);
//
//                // 마커를 찍은 위치의 경도와 위도 출력
//                double clickedLatitude = coord.latitude;
//                double clickedLongitude = coord.longitude;
//                String coordinates = String.format("위도: %f, 경도: %f", clickedLatitude, clickedLongitude);
//                Log.d("MapClickCoordinates", coordinates);
//            }
//        });
//    }
//
//    private void searchAddress(String address) {
//        // 주소를 좌표로 변환하여 지도에 표시
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocationName(address, 1);
//            if (!addresses.isEmpty()) {
//                Address resultAddress = addresses.get(0);
//                LatLng resultLatLng = new LatLng(resultAddress.getLatitude(), resultAddress.getLongitude());
//
//                // 검색된 주소의 좌표로 지도 이동
//                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(resultLatLng);
//                naverMap.moveCamera(cameraUpdate);
//
//                // 검색된 위치에 마커 및 원 추가 (원하는 경우)
//                if (clickedMarker != null) {
//                    clickedMarker.setMap(null);
//                    clickedMarker = null;
//                    if (circleOverlay != null) {
//                        circleOverlay.setMap(null);
//                        circleOverlay = null;
//                    }
//                }
//                clickedMarker = new Marker();
//                clickedMarker.setPosition(resultLatLng);
//                clickedMarker.setMap(naverMap);
//
//                circleOverlay = new CircleOverlay();
//                circleOverlay.setCenter(resultLatLng);
//                circleOverlay.setRadius(1000); // 반경 1km
//                circleOverlay.setColor(Color.argb(50, 0, 0, 255)); // 반투명한 파란색
//                circleOverlay.setMap(naverMap);
//            } else {
//                // 검색 결과가 없는 경우 처리
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}



