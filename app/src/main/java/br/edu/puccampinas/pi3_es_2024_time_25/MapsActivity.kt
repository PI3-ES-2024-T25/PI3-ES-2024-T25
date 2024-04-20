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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
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
    val id: String = "",
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
    private var locationPermissionGranted = false
    private var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    private var isMapReady: Boolean = false
    private var isLoadedUnitLocations: Boolean = false
    private var isUserLogged: Boolean = false
    private var haveUserCreditCard: Boolean = false
    private lateinit var markerUnitMap: MutableMap<Marker, Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkUserLoggedIn()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getUserLocation()
        binding.btnGoToMaps2.visibility = View.INVISIBLE
        binding.btnSignout.setOnClickListener {
            signOut()
        }
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
                        if (isMapReady && isLoadedUnitLocations) {
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
                }
                println("LOCK Permission granted")
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        setupMap(googleMap)
        setupInfoWindowAdapter()
        getUnits()
        mMap.setOnMarkerClickListener { marker ->
            binding.btnGoToMaps2.visibility = View.VISIBLE
            marker.showInfoWindow()
            binding.btnGoToMaps2.setOnClickListener {
                openGoogleMaps(marker.position)
            }
            if (isUserLogged && haveUserCreditCard) {
                binding.btnRentLocker.text = "Alugar armário"
                binding.btnRentLocker.setOnClickListener {
                    val unit = this.markerUnitMap[marker]
                    if (unit != null) {
                        if (isUserCloseToUnit(unit)) {
                            Toast.makeText(
                                applicationContext, "Você  ${unit.id}", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Você não está próximo ao armário",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else if (isUserLogged && !haveUserCreditCard) {
                binding.btnRentLocker.text = "Adicionar cartão de crédito"
                binding.btnRentLocker.setOnClickListener {
                    // val intent = Intent(this, CreditCardActivity::class.java)
                    //startActivity(intent)
                }
            } else {
                binding.btnRentLocker.text = "Quero alugar um armário"
                binding.btnRentLocker.setOnClickListener {
                    var builder = AlertDialog.Builder(this)
                    builder.setTitle("Para alugar um armário, é necessário fazer login")
                    builder.setMessage("Faça login ou crie uma conta para alugar um armário.")

                    builder.setPositiveButton("OK") { _, _ ->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
            true
        }
        mMap.setOnMapClickListener {
            binding.btnGoToMaps2.visibility = View.INVISIBLE
        }
    }

    private fun setupMap(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.dark_map_style
            )
        )
        isMapReady = true
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-23.5505, -46.6333)))
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
    }

    private fun setupInfoWindowAdapter() {
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

    private fun isWithin50m(
        userLat: Double, userLng: Double, unitLat: Double, unitLng: Double
    ): Boolean {
        val userLocation = Location("userLocation")
        userLocation.latitude = userLat
        userLocation.longitude = userLng

        val unitLocation = Location("unitLocation")
        unitLocation.latitude = unitLat
        unitLocation.longitude = unitLng

        val distanceInMeters = userLocation.distanceTo(unitLocation)
        return distanceInMeters <= 50
    }

    private fun getUnits() {
        try {
            markerUnitMap = mutableMapOf()
            db.collection("rental_units").get().addOnSuccessListener { result ->
                result.forEach { documentSnapshot ->
                    var unit = documentSnapshot.toObject(Unit::class.java)
                    unit = unit.copy(id = documentSnapshot.id)
                    println("Unit: ${unit.id} ${documentSnapshot.id}")
                    val markerOptions: MarkerOptions = MarkerOptions().position(
                        LatLng(
                            unit.coordinates.latitude, unit.coordinates.longitude
                        )
                    ).title(unit.name).snippet(unit.description)
                    val marker = mMap.addMarker(markerOptions)
                    if (marker != null) {
                        markerUnitMap[marker] = unit
                    }
                }
                centerMapOnUserLocation()
                isLoadedUnitLocations = true

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
            val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16.0f))
            mMap.uiSettings.isMyLocationButtonEnabled = false
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
                return
            }
            mMap.isMyLocationEnabled = true
        } catch (e: Exception) {
            println("Error centering map on user location: $e")
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkUserCreditCard() {
        // Verifica se o usuário tem um cartão de crédito cadastrado
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val docRef = db.collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    haveUserCreditCard = document.contains("creditCard")
                } else {
                    println("No such document")
                }
            }.addOnFailureListener { exception ->
                println("get failed with $exception")
            }.addOnCompleteListener {
                if (!haveUserCreditCard) {
                    setBtnToAddCreditCard()
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Adicione uma forma de pagamento")
                    builder.setMessage("Para alugar um armário, é necessário adicionar um cartão de crédito.")

                    builder.setPositiveButton("OK") { _, _ ->
                        // Ação a ser executada quando o botão positivo é pressionado
                        Toast.makeText(applicationContext, "Você pressionou OK", Toast.LENGTH_SHORT)
                            .show()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
        // Se não tiver, exibe um modal para que ele cadastre
        // Se tiver, segue com o aluguel do armário
    }

    private fun checkUserLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        isUserLogged = user != null

        if (isUserLogged) {
            checkUserCreditCard()
        } else {
            setBtnToLogin()
        }
    }

    private fun setBtnToAddCreditCard() {
        binding.btnRentLocker.text = "Adicionar cartão de crédito"
        binding.btnRentLocker.setOnClickListener {
            // val intent = Intent(this, CreditCardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setBtnToLogin() {
        binding.btnRentLocker.text = "Quero alugar um armário"
        binding.btnRentLocker.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Para alugar um armário, é necessário fazer login")
            builder.setMessage("Faça login ou crie uma conta para alugar um armário.")

            builder.setPositiveButton("OK") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun isUserCloseToUnit(unit: Unit): Boolean {
        return isWithin50m(
            userLocation.latitude,
            userLocation.longitude,
            unit.coordinates.latitude,
            unit.coordinates.longitude
        )
    }

    private fun checkRentalInProgress(): Boolean {
        // Verifica se o usuário tem um aluguel em andamento
        Toast.makeText(applicationContext, "Aluguel em andamento", Toast.LENGTH_SHORT).show()
        return false
    }
}