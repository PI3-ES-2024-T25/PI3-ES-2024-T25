package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val puccH15LatLng = LatLng(-22.834100948091834, -47.052624460116355)
        val puccH15Location: MarkerOptions = MarkerOptions().position(puccH15LatLng).title("Marker in Puccampinas")
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.addMarker(puccH15Location)
        mMap.setOnMarkerClickListener {
            val gmmIntentUri =
             Uri.parse("google.navigation:q=-22.834107787264276, -47.05260184200128&mode=w")

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps")

                // Attempt to start an activity that can handle the Intent
                mapIntent.resolveActivity(packageManager)?.let {
                    startActivity(mapIntent)
                }
            true
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(puccH15LatLng))

    }
}