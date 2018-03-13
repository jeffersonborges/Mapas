package br.com.jborges.mapas

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.content.DialogInterface
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.LocationRequest


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    val REQUEST_GPS = 0
    var mLocationRequest: LocationRequest? = null

    override fun onConnected(p0: Bundle?) {
        checkPermission()

        val minhaLocalizacao = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient)

        if (minhaLocalizacao != null)
            adicionarMarcador(minhaLocalizacao.latitude,
                    minhaLocalizacao.longitude,
                    "Não somos Shakira mas estoy aqui")
    }


    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("", "Permissão para gravar negada")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessária a permissao para GPS")
                        .setTitle("Permissao Requerida")

                builder.setPositiveButton("OK") { dialog, id ->
                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_GPS)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissão negada pelo usuário")
                } else {
                    Log.i("TAG", "Permissao concedida pelo usuario")
                }
                return
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("TAG", "SUSPENSO")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TAG", "Erro de conexão")
    }


    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    @Synchronized
    fun callConnection() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient.connect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener {

            mMap.clear()

            val geocoder = Geocoder(this)
            var address: List<Address>?

            address = geocoder
                    .getFromLocationName(etEndereco.text.toString(), 1)
            if (address.isNotEmpty()) {
                val location = address[0]
                adicionarMarcador(location.latitude, location.longitude, "Endereço pesquisado")
            } else {
                var alert = AlertDialog.Builder(this).create()
                alert.setTitle("Ops!!! Deu ruim!!!")
                alert.setMessage("Endereço não encontrado!")

                alert.setCancelable(false)

                alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { dialogInterface, inteiro ->
                    alert.dismiss()
                })

                alert.show()

            }
        }
    }

    fun adicionarMarcador(latitude: Double, longitude: Double, title: String) {
        //val sydney = LatLng(-34.0, 151.0)
        val Fiap = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions()
                .position(Fiap)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Fiap, 16f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callConnection()
    }

    public override fun onStart() {
        super.onStart()
        Log.d("TAG", "onStart fired ..............")
        mGoogleApiClient.connect()
    }

    public override fun onStop() {
        super.onStop()
        Log.d("TAG", "onStop fired ..............")
        mGoogleApiClient.disconnect()
        Log.d("TAG", "isConnected ...............: " + mGoogleApiClient.isConnected)
    }
}
