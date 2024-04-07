package br.edu.puccampinas.pi3_es_2024_time_25

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var btnGoToMaps: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        btnGoToMaps = findViewById(R.id.btn_go_to_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
//        default center location to São Paulo
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-23.5505, -46.6333)))
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.setMinZoomPreference(10.0f)
        getUnits()

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
//        mMap.setOnCameraMoveListener {
//            btnGoToMaps.visibility = View.INVISIBLE
//        }

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
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
                return null // No default content
            }
        })
    }

    private fun getUnits() {
        println("TAG Getting units")

        db.collection("rental_units").get().addOnSuccessListener { result ->
            var totalLat = 0.0
            var totalLng = 0.0
            var count = 0
            result.forEach { documentSnapshot ->
                val unit = documentSnapshot.toObject(Unit::class.java)
                val marker: MarkerOptions = MarkerOptions().position(
                    LatLng(
                        unit.coordinates.latitude, unit.coordinates.longitude
                    )
                ).title(unit.name).snippet(unit.description)
                mMap.addMarker(marker)

                totalLat += unit.coordinates.latitude
                totalLng += unit.coordinates.longitude
                count++
            }

            if (count > 0) {
                val avgLat = totalLat / count
                val avgLng = totalLng / count
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(avgLat, avgLng)))
                mMap.setMinZoomPreference(14.0f)
            }
        }.addOnFailureListener { exception ->
            Log.w("TAG", "Error getting documents.", exception)
        }
    }

    private fun openGoogleMaps(location: LatLng) {
        val gmmIntentUri =
            Uri.parse("google.navigation:q=${location.latitude}, ${location.longitude}&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

}