package br.edu.puccampinas.pi3_es_2024_time_25

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore


data class Address(
    val country: String = "",
    val number: String = "",
    val city: String = "",
    val street: String = "",
    val neighborhood: String = "",
    val state: String = ""
)

data class Coordinates(
    val latitude: Double = 0.0, val longitude: Double = 0.0
)

data class RentalOption(
    val price: Int = 0, val time: Int = 0
)

data class Unit(
    val lockersQuantity: Int = 0,
    val address: Address = Address(),
    val coordinates: Coordinates = Coordinates(),
    val name: String = "",
    val description: String = "",
    val lockers: List<String> = emptyList(),
    val rentalOptions: List<RentalOption> = emptyList()
)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var btnGoToMaps: ImageButton
    private var locationPermissionGranted = false
    private var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    private var isMapReady = false
    private var isLoadedUnitLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        btnGoToMaps = findViewById(R.id.btn_go_to_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getUserLocation()
    }

    private fun getUserLocation() {

        try {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = location
                        if (isMapReady && isLoadedUnitLocation) {
                            centerMapOnUserLocation()
                        }
                    }
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        } catch (e: Exception) {
            println("LOCK Error getting user location: $e")
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    getUserLocation()
                    // ...
                }
                println("LOCK Permission granted")
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
//                R.raw.night_map_style
//                R.raw.augerbine_map_style
                R.raw.dark_map_style
            )
        )
        getUnits()
        isMapReady = true
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-23.5505, -46.6333)))
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            btnGoToMaps.visibility = View.VISIBLE

            btnGoToMaps.setOnClickListener {
                openGoogleMaps(marker.position)
            }
            marker.showInfoWindow()
            true
        }
        mMap.setOnMapClickListener {
            btnGoToMaps.visibility = View.INVISIBLE
        }

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            @SuppressLint("InflateParams")
            override fun getInfoWindow(marker: Marker): View? {
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val v = inflater.inflate(R.layout.custom_info_window, null)

                val title = v.findViewById<TextView>(R.id.title)
                title.text = marker.title

                val snippet = v.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet

                return v
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })
    }

    private fun isWithin10Km(
        userLat: Double, userLng: Double, unitLat: Double, unitLng: Double
    ): Boolean {
        val userLocation = Location("userLocation")
        userLocation.latitude = userLat
        userLocation.longitude = userLng

        val unitLocation = Location("unitLocation")
        unitLocation.latitude = unitLat
        unitLocation.longitude = unitLng

        val distanceInMeters = userLocation.distanceTo(unitLocation)
        distanceInMeters / 1000

        return distanceInMeters <= 100
    }

    private fun getUnits() {
        try {
            db.collection("rental_units").get().addOnSuccessListener { result ->
                result.forEach { documentSnapshot ->
                    val unit = documentSnapshot.toObject(Unit::class.java)
                    val marker: MarkerOptions = MarkerOptions().position(
                        LatLng(
                            unit.coordinates.latitude, unit.coordinates.longitude
                        )
                    ).title(unit.name).snippet(unit.description)
                    mMap.addMarker(marker)
                }
                centerMapOnUserLocation()
                isLoadedUnitLocation = true

            }.addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
            }
        } catch (e: Exception) {
            println("LOCK Error getting units: $e")
        }
    }

    private fun openGoogleMaps(location: LatLng) {
        val gmmIntentUri =
            Uri.parse("google.navigation:q=${location.latitude}, ${location.longitude}&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun centerMapOnUserLocation() {
        println("Centering map on user location $userLocation")
        try {
            if (userLocation != null) {
                val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16.0f))
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                println("User location is null")
            }
        } catch (e: Exception) {
            println("Error centering map on user location: $e")
        }
    }
}