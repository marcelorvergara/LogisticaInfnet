package android.inflabnet.logisticainfnet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.mostrar_arquivos.*
import kotlinx.android.synthetic.main.mostrar_arquivos.view.*
import kotlinx.android.synthetic.main.mostrar_arquivos.view.rcRecycleV
import java.io.*
import java.nio.file.Files
import java.nio.file.Files.isDirectory
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var listFile: Array<String>
    //arquivos
    private val filepath = "MyFileStorage"
    internal var myExternalFile: File?=null
    private val isExternalStorageReadOnly: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)
    }
    private val isExternalStorageAvailable: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED.equals(extStorageState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        pegalocalBtn.setOnClickListener {
            getLastLocation()
        }

        gravaarqBtn.setOnClickListener {
            //nome do arquivo
            //calendário
            val c = Calendar.getInstance()
            val ano = c.get(Calendar.YEAR)
            val mes = c.get(Calendar.MONTH)
            val dia = c.get(Calendar.DAY_OF_MONTH)
            val hora = c.get(Calendar.HOUR_OF_DAY)

            val fileName = "${dia}" + "${mes}" + "${ano}" + "-${hora}"
            val fileData = "Latitude ${txtLatitude.text}" + " Longitude ${txtLongitude.text}"
            myExternalFile = File(getExternalFilesDir(filepath), fileName)
            val path = getExternalFilesDir(filepath)
            listFile = path!!.list()!!
            var fileExist: Boolean = false
            listFile.forEach {
                Log.i("Teste", "Arquivo antigo ${it} - Arquivo novo ${fileName}")
                if (it == fileName) {
                    fileExist = true
                    }
                }
            if(fileExist){
                try {
                    val fw = FileWriter(myExternalFile!!, true)
                    fw.write("\n${fileData}")
                    fw.close()
                }catch (e: IOException){
                    Toast.makeText(this,"Não foi possível abrir arquivo existente",Toast.LENGTH_SHORT).show()
                }
            }else {

                try {
                    val fileOutPutStream = FileOutputStream(myExternalFile!!)
                    fileOutPutStream.write(fileData.toByteArray())
                    fileOutPutStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(applicationContext, "Dados Gravados em ${path}", Toast.LENGTH_SHORT).show()
       }
        if (!isExternalStorageAvailable || isExternalStorageReadOnly) {
            gravaarqBtn.isEnabled = false
        }

        listarBtn.setOnClickListener {
            val path = getExternalFilesDir(filepath)
            listFile = path!!.list()!!
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.mostrar_arquivos, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Lista de Arquivos")
            val  mAlertDialog = mBuilder.show()
            val linearLayoutManager = LinearLayoutManager(applicationContext)
            mDialogView.rcRecycleV.layoutManager = linearLayoutManager
            mDialogView.rcRecycleV.scrollToPosition(listFile!!.size)
            mDialogView.rcRecycleV.adapter = FilesAdapter(listFile){
                Toast.makeText(applicationContext,it,Toast.LENGTH_SHORT).show()
                val inputStream: InputStream = File("${path}/${it}").inputStream()
                val lineList = mutableListOf<String>()
                inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

                mDialogView.lstArquivos.adapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_list_item_1,
                    lineList
                )

            }

            mDialogView.btnVoltar.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }




    @SuppressLint("SemPermissão")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        txtLatitude.text = location.latitude.toString()
                        txtLongitude.text = location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("SemPermissão")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            txtLatitude.text = mLastLocation.latitude.toString()
            txtLongitude.text = mLastLocation.longitude.toString()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }
}
