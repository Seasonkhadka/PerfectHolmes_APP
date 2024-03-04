package com.example.perfectholmes_app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.perfectholmes_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    private Marker touchMarker;
    private CircleOverlay touchCircle;
    private boolean touchEnabled = false; // 터치 이벤트 활성화 여부
    private Marker searchMarker; // 검색된 위치를 나타내는 마커
    private JSONArray facilitiesArray; // API에서 받은 시설 정보 배열
    private ArrayList<Marker> allMarkers = new ArrayList<>(); // 모든 마커를 저장할 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // FragmentManager 초기화
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        // 지도 비동기적으로 로드
        mapFragment.getMapAsync(this);

        // 검색 EditText 초기화 및 검색 기능 추가
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

        // 액션 버튼 초기화
        FloatingActionButton markerBtn = findViewById(R.id.marker_btn);

        // 액션 버튼 클릭 이벤트 처리
        markerBtn.setOnClickListener(v -> {
            touchEnabled = !touchEnabled; // 터치 이벤트 활성화 상태를 토글
            if (touchEnabled) {
                markerBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green))); // 활성화된 경우 초록색 배경
            } else {
                markerBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red))); // 비활성화된 경우 빨간색 배경

            }
            toggleMarkerAndCircle(); // 수정: toggleMarkerAndCircle 메서드 호출
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        LatLng initialPosition = new LatLng(34.803743, 126.421689);
        naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition));

        naverMap.setOnMapClickListener((point, coord) -> {
            if (touchEnabled) {
                toggleMarkerAndCircleAtLocation(coord.latitude, coord.longitude);
            }
        });

        GetFacilitiesTask getFacilitiesTask = new GetFacilitiesTask();
        getFacilitiesTask.execute();
    }

    private void searchAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address resultAddress = addresses.get(0);
                LatLng resultLatLng = new LatLng(resultAddress.getLatitude(), resultAddress.getLongitude());
                naverMap.moveCamera(CameraUpdate.scrollTo(resultLatLng));
                addSearchMarkerAtLocation(resultLatLng.latitude, resultLatLng.longitude);
                // 검색된 주소가 있을 경우 해당 위치의 마커를 추가합니다.
                addFacilityMarkerAtLocation(address, resultLatLng.latitude, resultLatLng.longitude);
            } else {
                showErrorMessage("입력하신 주소가 없습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void addSearchMarkerAtLocation(double latitude, double longitude) {
        if (searchMarker != null) {
            searchMarker.setMap(null);
            searchMarker = null;
        }
        searchMarker = new Marker();
        searchMarker.setPosition(new LatLng(latitude, longitude));
        searchMarker.setMap(naverMap);
    }

    private void toggleMarkerAndCircleAtLocation(double latitude, double longitude) {
        if (touchMarker != null) {
            touchMarker.setPosition(new LatLng(latitude, longitude));
            touchCircle.setCenter(new LatLng(latitude, longitude));

            // 반경 안에 있는 API 정보에 대한 마커 추가
            addFacilityMarkerInCircle(latitude, longitude);
        }
    }

    private void toggleMarkerAndCircle() {
        if (touchMarker != null) {
            touchMarker.setMap(null);
            touchMarker = null;
            if (touchCircle != null) {
                touchCircle.setMap(null);
                touchCircle = null;
            }
        } else {
            touchMarker = new Marker();
            touchMarker.setPosition(naverMap.getCameraPosition().target);
            touchMarker.setIconTintColor(Color.BLUE); // 액션 버튼으로 생성된 마커는 파란색 아이콘 적용
            touchMarker.setMap(naverMap);

            touchCircle = new CircleOverlay();
            touchCircle.setCenter(naverMap.getCameraPosition().target);
            touchCircle.setRadius(500);
            touchCircle.setColor(Color.argb(50, 255, 0, 0));
            touchCircle.setMap(naverMap);

            // 반경 안에 있는 API 정보에 대한 마커 추가
            addFacilityMarkerInCircle(naverMap.getCameraPosition().target.latitude, naverMap.getCameraPosition().target.longitude);
        }

        // 액션 버튼 비활성화 시 노랑색 마커 제거
        if (!touchEnabled && searchMarker != null) {
            searchMarker.setMap(null);
            searchMarker = null;
        }

        // 액션 버튼 비활성화 시 모든 노랑색 마커 제거
        if (!touchEnabled) {
            clearAllMarkers();
        }
    }

    private void clearAllMarkers() {
        for (Marker marker : allMarkers) {
            marker.setMap(null);
        }
        allMarkers.clear();
    }





    private void addFacilityMarkerAtLocation(String address, double latitude, double longitude) {
        // API에서 받은 주소 목록을 반복하여 모든 마커 추가
        Marker facilityMarker = new Marker();
        facilityMarker.setPosition(new LatLng(latitude, longitude));
        facilityMarker.setIconTintColor(Color.RED); // API에서 받은 주소의 마커는 빨간색 아이콘 적용
        facilityMarker.setMap(naverMap);
        facilityMarker.setCaptionText(address);
        allMarkers.add(facilityMarker); // 생성된 마커를 리스트에 추가
        Log.e("GetFacilitiesTask", "Error fetching facilities from server" +  address);

    }

    private void addFacilityMarkerInCircle(double latitude, double longitude) {
        // API에서 받은 주소 목록을 반복하여 반경 안에 있는 경우에만 마커 추가
        if (touchCircle == null || facilitiesArray == null) {
            return; // 원이 없거나 시설 정보가 없으면 처리 중단
        }

        // 이전에 추가된 마커 제거
        for (Marker marker : allMarkers) {
            marker.setMap(null);
        }
        allMarkers.clear();

        for (int i = 0; i < facilitiesArray.length(); i++) {
            try {
                JSONObject facilityObject = facilitiesArray.getJSONObject(i);
                double facilityLatitude = facilityObject.getDouble("lat");
                double facilityLongitude = facilityObject.getDouble("lng");

                LatLng facilityLatLng = new LatLng(facilityLatitude, facilityLongitude);
                double distance = calculateDistance(new LatLng(latitude, longitude), facilityLatLng);
                if (distance <= 500) {
                    String address = facilityObject.getString("address");
                    Marker facilityMarker = new Marker();
                    facilityMarker.setPosition(facilityLatLng);
                    facilityMarker.setIconTintColor(Color.RED); // API에서 받은 주소의 마커는 빨간색 아이콘 적용
                    facilityMarker.setMap(naverMap);
                    facilityMarker.setCaptionText(address);
                    allMarkers.add(facilityMarker); // 생성된 마커를 리스트에 추가
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        // 지구 반지름 (미터)
        final double EARTH_RADIUS = 6371000;

        double lat1 = Math.toRadians(point1.latitude);
        double lon1 = Math.toRadians(point1.longitude);
        double lat2 = Math.toRadians(point2.latitude);
        double lon2 = Math.toRadians(point2.longitude);

        // 위도 및 경도 간의 차이 계산
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine 공식을 사용하여 거리 계산
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
    }

    private class GetFacilitiesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String serverUrl = "http://43.202.188.79/facility/around?format=json&lat=34.806035&lng=126.418034";
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                Log.e("GetFacilitiesTask", "Error fetching facilities from server", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("GetFacilitiesTask", "Server response: " + result);
                handleFacilitiesResponse(result);
            } else {
                Log.e("GetFacilitiesTask", "Server response is null");
            }
        }
    }

    private void handleFacilitiesResponse(String response) {
        try {
            facilitiesArray = new JSONArray(response);

            // 반경 안에 있는 API 정보에 대한 마커 추가
            if (touchMarker != null && touchCircle != null) {
                double latitude = touchMarker.getPosition().latitude;
                double longitude = touchMarker.getPosition().longitude;
                addFacilityMarkerInCircle(latitude, longitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage());
        }
    }
}
