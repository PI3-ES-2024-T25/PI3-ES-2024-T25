package br.edu.puccampinas.pi3_es_2024_time_25

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalTime


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
    val id: String = "", val price: Double = 0.0, val time: Int = 0
)

data class Manager(
    val id: String = "",
    val name: String = "",
)

data class Unit(
    val id: String = "",
    val lockersQuantity: Int = 0,
    val address: Address = Address(),
    val coordinates: Coordinates = Coordinates(),
    val name: String = "",
    val description: String = "",
    val lockers: List<String> = emptyList(),
    val rentalOptions: List<RentalOption> = emptyList(),
    val lockersAvailable: List<String> = emptyList(),
    val manager: Manager = Manager()
)

data class RentData(
    val unit: Unit = Unit(),
)

data class LastRent(
    val rentId: String = "", val rentData: RentData = RentData(), val verified: Boolean = false
)

data class UserLastRent(val lastRent: LastRent = LastRent())

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var db: FirebaseFirestore
    private var permissionsRequestAccessFineLocation = 0
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

        try {
            db = FirebaseFirestore.getInstance()

            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            setButtonSelectUnitToRent()
            checkUserLoggedIn()

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            binding.btnGoToMaps2.visibility = View.INVISIBLE
            binding.btnSignout.setOnClickListener {
                signOut()
            }
        } catch (e: Exception) {
            println("LOCK Error on create: $e")
        }
    }

    override fun onResume() {
        super.onResume()
        if (isUserLogged) {
            checkUserHasRentalInRunning()
            checkUserCreditCard()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            permissionsRequestAccessFineLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mMap.isMyLocationEnabled = true
                    centerMapOnUserLocation()
                } else {
                    // A permissão foi negada. Mostre uma explicação para o usuário e solicite novamente a permissão.
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        // A permissão foi negada permanentemente. Direcione o usuário para as configurações do aplicativo.
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:$packageName")
                            )
                        )
                    } else {
                        // A permissão foi negada. Mostre uma explicação para o usuário e solicite novamente a permissão.
                        AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                            .setTitle("Permissão de localização necessária")
                            .setMessage("Esta aplicação requer a permissão de localização para mostrar a sua localização no mapa.")
                            .setPositiveButton("OK") { _, _ ->
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    permissionsRequestAccessFineLocation
                                )
                            }.show()
                    }
                }
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
            if (isInOpeningHours()) {
                setupButton(marker)
            }
            true
        }
        mMap.setOnMapClickListener {
            binding.btnGoToMaps2.visibility = View.INVISIBLE
            setButtonSelectUnitToRent()
        }
    }

    private fun sendToRentLockerActivity(unit: Unit) {
        val intent = Intent(this, RentalOptionsActivity::class.java)
        val gson = Gson()
        val unitJson = gson.toJson(unit)
        intent.putExtra("unit", unitJson)
        startActivity(intent)
    }

    private fun showRentInRunningDialog(userLastRent: UserLastRent) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setTitle("Você possui uma locação em andamento")
        builder.setMessage("Você deve efetivar ou cancelar o aluguel atual antes de alugar outro armário.")

        builder.setPositiveButton("Efetivar") { _, _ ->
            // Ação a ser executada quando o botão positivo é pressionado
            sendToQRCodeGeneratorActivity(
                QRCodeGeneratorActivity.QrCodeData(
                    userLastRent.lastRent.rentId, userLastRent.lastRent.rentData.unit.manager.name
                )
            )
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun sendToQRCodeGeneratorActivity(rentData: QRCodeGeneratorActivity.QrCodeData) {
        val intent = Intent(this, QRCodeGeneratorActivity::class.java)
        val gson = Gson()
        val rentDataJson = gson.toJson(rentData)
        intent.putExtra("rentData", rentDataJson)
        startActivity(intent)
    }

    private fun setupButton(marker: Marker) {

        if (isUserLogged && haveUserCreditCard) {
            binding.btnRentLocker.text = getString(R.string.button_rent_locker)
            binding.btnRentLocker.setOnClickListener {
                val unit = this.markerUnitMap[marker]
                if (unit != null) {
                    if (isUserCloseToUnit(unit)) {
                        GlobalScope.launch {
                            val hasLockerAvailable = checkUnitHasLockerAvailable(unit)

                            println("LOCK hasLockerAvailable: $hasLockerAvailable")
                            if (hasLockerAvailable) {
                                sendToRentLockerActivity(unit)
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Não há armários disponíveis",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
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
            binding.btnRentLocker.text = getString(R.string.button_add_credit_card)
            binding.btnRentLocker.setOnClickListener {
                val intent = Intent(this, AddCreditCardActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.btnRentLocker.text = getString(R.string.button_login_to_rent)
            binding.btnRentLocker.setOnClickListener {
                val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
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
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isCompassEnabled = false
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
            val builder = LatLngBounds.builder()
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
                        builder.include(marker.position)
                        markerUnitMap[marker] = unit
                    }
                }
                isLoadedUnitLocations = true

                if (isMapReady && isLoadedUnitLocations) {
                    val bounds = builder.build()
                    val padding = 100
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mMap.moveCamera(cameraUpdate)
                }

                getUserLocation()
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

    // função para pegar a localização do usuário
    private fun getUserLocation() {
        // checar se a permissão de localização foi concedida
        // se não foi, solicitar a permissão
        // se foi, habilitar a localização no mapa
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionsRequestAccessFineLocation
            )
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // chamar a função para centralizar o mapa com a posição do usuário
                if (location != null) {
                    userLocation = location
                    centerMapOnUserLocation()
                }
            }
        }
    }

    // função para centralizar o mapa com a posição do usuário
    private fun centerMapOnUserLocation() {
        if (!::userLocation.isInitialized) {
            return
        }
        val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16.0f))
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // verifica se o usuario possui uma locação em andamento
    private fun checkUserHasRentalInRunning() {
        // Verifica se o usuário tem uma locação em andamento
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->

                if (document != null) {
                    if (document.contains("lastRent")) {
                        val userlastRent = document.toObject(UserLastRent::class.java)
                        if (userlastRent != null && !userlastRent.lastRent.verified) {
                            showRentInRunningDialog(userlastRent)
                        }
                    }
                }
                println("DocumentSnapshot data: ${document.data}")

            }
        }
    }

    private fun checkUserCreditCard() {
        // Verifica se o usuário tem um cartão de crédito cadastrado
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val docRef = db.collection("users").document(user.uid).collection("creditCard")
            docRef.get().addOnSuccessListener { result ->
                if (result != null) {
                    haveUserCreditCard = result.size() > 0
                }
            }.addOnFailureListener { exception ->
                println("get failed with $exception")
            }.addOnCompleteListener {
                if (!haveUserCreditCard) {
                    setBtnToAddCreditCard()
                    val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
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
    }

    private fun checkUserLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        isUserLogged = user != null

        if (isUserLogged) {
            checkUserCreditCard()
        } else {
            binding.btnSignout.visibility = View.INVISIBLE
            setBtnToLogin()
        }
    }

    private fun setBtnToAddCreditCard() {
        binding.btnRentLocker.text = getString(R.string.button_add_credit_card)
        binding.btnRentLocker.setOnClickListener {
            val intent = Intent(this, AddCreditCardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setBtnToLogin() {
        binding.btnRentLocker.text = getString(R.string.button_login_to_rent)
        binding.btnRentLocker.setOnClickListener {

            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
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

    private suspend fun checkUnitHasLockerAvailable(unit: Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val document = db.collection("rental_units").document(unit.id).get().await()
                if (document != null) {
                    println("LOCK DocumentSnapshot data: ${document.data}")
                    val unitObject = document.toObject(Unit::class.java)
                    println("LOCK unit: $unitObject")
                    if (unitObject != null) {
                        println("LOCK unitObject.lockersAvailable: ${unitObject.lockersAvailable.size}")
                        unitObject.lockersAvailable.isNotEmpty()
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                println("LOCK Error checking locker availability: $e")
                false
            }
        }
    }

    private fun setButtonSelectUnitToRent() {
        binding.btnRentLocker.text = getString(R.string.select_locker)
        binding.btnRentLocker.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
            builder.setTitle("Selecione um armário")
            builder.setMessage("Para alugar um armário, selecione um armário no mapa.")
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun isInOpeningHours(): Boolean {
        val now = LocalTime.now()
        val startTime = LocalTime.of(7, 0)
        val endTime = LocalTime.of(18, 0)
        val conditionResult = now.isBefore(endTime) && now.isAfter(startTime)
        if (conditionResult) {
            Toast.makeText(
                applicationContext, "Fora do horário de funcionamento", Toast.LENGTH_SHORT
            ).show()
        }

        return conditionResult
    }

}