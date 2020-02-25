package android.vergara.guiainfnet

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.Comparator
import kotlin.text.StringBuilder

class MainActivity : AppCompatActivity() {

    var placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS)

    lateinit var placesClient: PlacesClient

    internal var placeId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        requestPermission()
        initPlaces()
        setupPlacesAutocomplete()
        setupCurrentPlace()

    }

    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(Arrays.asList(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION

            ))
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity,"You should accept Permission", Toast.LENGTH_LONG).show()
                }
            }).check()

    }

    private fun setupCurrentPlace(){
        val request = FindCurrentPlaceRequest.builder(placeFields).build()

        btn_get_current_place.setOnClickListener{
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return@setOnClickListener;
            }
            }

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val response = task.result

                    response!!.placeLikelihoods.sortWith(Comparator
                    {
                        placeChildhood,t1 ->
                        placeChildhood.likelihood.toDouble().compareTo(t1.likelihood.toDouble())

                    })
                    Collections.reverse(response.placeLikelihoods)
                    placeId = response.placeLikelihoods[0].place.id!!
                    val likehoods = StringBuilder("")
                    edt_address.setText(StringBuilder(response.placeLikelihoods[0].place.address!!))

                    for(placeLikelihood in response.placeLikelihoods){
                        likehoods.append(String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.place.name,
                            placeLikelihood.likelihood))
                            .append("\n")
                    }
                    edt_place_likelihoods.setText(likehoods.toString())
                }
                else{
                    Toast.makeText(this,"Place not found!", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupPlacesAutocomplete() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment_place) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(placeFields)

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                Toast.makeText(this@MainActivity, "" + p0.address, Toast.LENGTH_LONG).show()
            }

            override fun onError(p0: Status) {
                Toast.makeText(this@MainActivity, "" + p0.statusMessage, Toast.LENGTH_LONG).show()
            }
        })
    }
}
