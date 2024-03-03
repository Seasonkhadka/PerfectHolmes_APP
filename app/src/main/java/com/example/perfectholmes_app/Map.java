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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
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
import java.util.List;
import java.util.Locale;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    private Marker touchMarker;
    private CircleOverlay touchCircle;
    private boolean touchEnabled = false; // 터치 이벤트 활성화 여부
    private Marker searchMarker; // 검색된 위치를 나타내는 마커
    private boolean isInCircle = false; // 원 안에 있는지 여부

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
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        LatLng initialPosition = new LatLng(34.803743, 126.421689); // 초기 위치 (예: 서울)
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
            touchCircle.setRadius(500);
            touchCircle.setColor(Color.argb(50, 255, 0, 0));
            touchCircle.setMap(naverMap);

            isInCircle = isMarkerInCircle(latitude, longitude);
            Log.d("isInCircle", "Is in circle: " + isInCircle);
        }
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
            JSONArray facilitiesArray = new JSONArray(response);

            for (int i = 0; i < facilitiesArray.length(); i++) {
                JSONObject facilityObject = facilitiesArray.getJSONObject(i);
                String address = facilityObject.getString("address");
                Log.d("FacilityAddress", "Facility Address: " + address);

                // 시설 위치에 마커 추가
                double latitude = facilityObject.getDouble("lat");
                double longitude = facilityObject.getDouble("lng");
                addFacilityMarkerAtLocation(address, latitude, longitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage());
        }
    }

    private void addFacilityMarkerAtLocation(String address, double latitude, double longitude) {
        if (isInCircle) {
            Marker facilityMarker = new Marker();
            facilityMarker.setPosition(new LatLng(latitude, longitude));
            facilityMarker.setMap(naverMap);
            facilityMarker.setCaptionText(address);
        }
    }

    private boolean isMarkerInCircle(double latitude, double longitude) {
        if (touchCircle == null) {
            return false;
        }

        LatLng circleCenter = touchCircle.getCenter();
        double circleRadius = touchCircle.getRadius();
        double distanceSquared = Math.pow(latitude - circleCenter.latitude, 2) + Math.pow(longitude - circleCenter.longitude, 2);
        return distanceSquared <= Math.pow(circleRadius, 2);
    }
}





///////



//public class Map extends AppCompatActivity implements OnMapReadyCallback {
//
//    private NaverMap naverMap;
//    private CircleOverlay touchCircle;
//    private List<Marker> schoolMarkers = new ArrayList<>();
//    private Marker touchMarker;
//    private boolean touchEnabled = false;
//    private FloatingActionButton markerBtn;
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
//        markerBtn = findViewById(R.id.marker_btn); // 플로팅 액션 버튼 초기화
//
//        // 액션 버튼 클릭 이벤트 처리
//        markerBtn.setOnClickListener(v -> {
//            touchEnabled = !touchEnabled; // 터치 이벤트 활성화 상태를 토글
//            if (!touchEnabled && touchMarker != null) { // 터치 이벤트 비활성화 상태에서만 실행
//                touchMarker.setMap(null); // 마커 제거
//                if (touchCircle != null) {
//                    touchCircle.setMap(null); // 원 제거
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onMapReady(@NonNull NaverMap naverMap) {
//        this.naverMap = naverMap;
//
//        // 초기 지도 설정
//        LatLng initialPosition = new LatLng(37.5665, 126.9780); // 초기 위치 (예: 서울)
//        naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition));
//
//        // 지도 클릭 이벤트 처리
//        naverMap.setOnMapClickListener((point, coord) -> {
//            // 터치된 위치에 마커와 반경 500m의 원 추가 또는 제거
//            toggleMarkerAndCircleAtLocation(coord.latitude, coord.longitude);
//        });
//
//        // API에서 학교 데이터 가져오기
//        GetSchoolsTask getSchoolsTask = new GetSchoolsTask();
//        getSchoolsTask.execute();
//    }
//
//    private void toggleMarkerAndCircleAtLocation(double latitude, double longitude) {
//        // 터치된 위치에 마커 및 반경 500m의 원 추가 또는 제거
//        if (touchMarker != null) {
//            touchMarker.setMap(null);
//            touchMarker = null;
//            if (touchCircle != null) {
//                touchCircle.setMap(null);
//                touchCircle = null;
//            }
//        } else {
//            touchMarker = new Marker();
//            touchMarker.setPosition(new LatLng(latitude, longitude));
//            touchMarker.setMap(naverMap);
//
//            touchCircle = new CircleOverlay();
//            touchCircle.setCenter(new LatLng(latitude, longitude));
//            touchCircle.setRadius(500); // 반경 500m
//            touchCircle.setColor(Color.argb(50, 255, 0, 0)); // 반투명한 빨간색
//            touchCircle.setMap(naverMap);
//        }
//    }
//
//    private void addMarkerAtLocation(double latitude, double longitude) {
//        // 터치된 위치에 마커 추가
//        if (touchMarker != null) {
//            touchMarker.setMap(null);
//            touchMarker = null;
//        }
//        touchMarker = new Marker();
//        touchMarker.setPosition(new LatLng(latitude, longitude));
//        touchMarker.setMap(naverMap);
//    }
//
//    private void addSchoolMarkerAtLocation(String name, double latitude, double longitude) {
//        // 학교 위치에 마커 추가
//        Marker schoolMarker = new Marker();
//        schoolMarker.setPosition(new LatLng(latitude, longitude));
//        schoolMarker.setMap(naverMap);
//        schoolMarker.setCaptionText(name);
//
//        // 생성된 학교 마커를 리스트에 추가
//        schoolMarkers.add(schoolMarker);
//    }
//
//    private class GetSchoolsTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected String doInBackground(Void... voids) {
//            try {
//                // 서버 엔드포인트 URL 설정
//                String serverUrl = "http://43.202.188.79/facility/around?lat=34.806035&lng=126.418034"; // 서버 URL을 적절히 변경하세요.
//                URL url = new URL(serverUrl);
//
//                // HTTP 연결 설정
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                // 서버 응답 읽기
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//
//                reader.close();
//                connection.disconnect();
//
//                return response.toString();
//            } catch (IOException e) {
//                Log.e("GetSchoolsTask", "Error fetching schools from server", e);
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (result != null) {
//                Log.d("GetSchoolsTask", "Server response: " + result);
//                // 서버 응답을 처리하는 메서드 호출
//                handleSchoolsResponse(result);
//            } else {
//                Log.e("GetSchoolsTask", "Server response is null");
//                // 서버 응답이 null인 경우의 예외 처리 로직을 작성
//            }
//        }
//    }
//
//    private void handleSchoolsResponse(String response) {
//        try {
//            // JSON 문자열을 JSONArray로 변환
//            JSONArray schoolsArray = new JSONArray(response);
//
//            // 각 학교에 대해 마커 표시
//            for (int i = 0; i < schoolsArray.length(); i++) {
//                JSONObject schoolObject = schoolsArray.getJSONObject(i);
//                int id = schoolObject.getInt("id");
//                String createdAt = schoolObject.getString("created_at");
//                String updatedAt = schoolObject.getString("updated_at");
//                String name = schoolObject.getString("name");
//                String address = schoolObject.getString("address");
//                String type = schoolObject.getString("type");
//                double latitude = schoolObject.getDouble("lat");
//                double longitude = schoolObject.getDouble("lng");
//
//                // 학교 위치에 마커 추가
//                addSchoolMarkerAtLocation(name, latitude, longitude);
//
//                // 원 안에 있는 학교인 경우에만 마커 표시
//                if (isMarkerInCircle(latitude, longitude)) {
//                    addMarkerAtLocation(latitude, longitude);
//                }
//
//                // 학교
//                Log.d("SchoolMarker", "School Name: " + name + ", Latitude: " + latitude + ", Longitude: " + longitude);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage()); // 에러 메시지 출력
//        }
//    }
//
//    private boolean isMarkerInCircle(double latitude, double longitude) {
//        // 원 안에 있는지 여부를 검사하는 메서드
//        LatLng circleCenter = touchCircle.getCenter();
//        double circleRadius = touchCircle.getRadius();
//        double distanceSquared = Math.pow(latitude - circleCenter.latitude, 2) + Math.pow(longitude - circleCenter.longitude, 2);
//        return distanceSquared <= Math.pow(circleRadius, 2);
//    }
//}







//
//
//
//import android.graphics.Color;
//import android.location.Address;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.inputmethod.EditorInfo;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.FragmentManager;
//
//import com.naver.maps.geometry.LatLng;
//import com.naver.maps.map.CameraUpdate;
//import com.naver.maps.map.MapFragment;
//import com.naver.maps.map.NaverMap;
//import com.naver.maps.map.OnMapReadyCallback;
//import com.naver.maps.map.overlay.CircleOverlay;
//import com.naver.maps.map.overlay.Marker;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//
//public class Map extends AppCompatActivity implements OnMapReadyCallback {
//
//    private NaverMap naverMap;
//    private Marker searchMarker;
//    private Marker touchMarker;
//    private CircleOverlay touchCircle;
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
//        this.naverMap = naverMap;
//
//        // 초기 지도 설정
//        LatLng initialPosition = new LatLng(37.5665, 126.9780); // 초기 위치 (예: 서울)
//        naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition));
//
//        // 지도 클릭 이벤트 처리
//        naverMap.setOnMapClickListener((point, coord) -> {
//            // 터치된 위치에 마커와 반경 500m의 원 추가 또는 제거
//            toggleMarkerAndCircleAtLocation(coord.latitude, coord.longitude);
//        });
//    }
//
//    private void searchAddress(String address) {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocationName(address, 1);
//            if (!addresses.isEmpty()) {
//                Address resultAddress = addresses.get(0);
//                LatLng resultLatLng = new LatLng(resultAddress.getLatitude(), resultAddress.getLongitude());
//
//                // 검색된 주소의 좌표로 지도 이동
//                naverMap.moveCamera(CameraUpdate.scrollTo(resultLatLng));
//
//                // 검색된 위치에 마커 추가
//                addSearchMarkerAtLocation(resultLatLng.latitude, resultLatLng.longitude);
//            } else {
//                // 검색 결과가 없는 경우 처리
//                showErrorMessage("입력하신 주소가 없습니다.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showErrorMessage(String message) {
//        // Toast 메시지로 에러 메시지 표시
//        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }
//
//    private void addSearchMarkerAtLocation(double latitude, double longitude) {
//        // 이전에 생성된 검색 마커 제거
//        if (searchMarker != null) {
//            searchMarker.setMap(null);
//            searchMarker = null;
//        }
//
//        // 검색된 위치에 새로운 마커 추가
//        searchMarker = new Marker();
//        searchMarker.setPosition(new LatLng(latitude, longitude));
//        searchMarker.setMap(naverMap);
//    }
//
//    private void toggleMarkerAndCircleAtLocation(double latitude, double longitude) {
//        // 터치된 위치에 마커 및 반경 500m의 원 추가 또는 제거
//        if (touchMarker != null) {
//            touchMarker.setMap(null);
//            touchMarker = null;
//            if (touchCircle != null) {
//                touchCircle.setMap(null);
//                touchCircle = null;
//            }
//        } else {
//            touchMarker = new Marker();
//            touchMarker.setPosition(new LatLng(latitude, longitude));
//            touchMarker.setMap(naverMap);
//
//            touchCircle = new CircleOverlay();
//            touchCircle.setCenter(new LatLng(latitude, longitude));
//            touchCircle.setRadius(500); // 반경 500m
//            touchCircle.setColor(Color.argb(50, 255, 0, 0)); // 반투명한 빨간색
//            touchCircle.setMap(naverMap);
//        }
//    }
//}




//---------------------------------------------------------------------------------
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



