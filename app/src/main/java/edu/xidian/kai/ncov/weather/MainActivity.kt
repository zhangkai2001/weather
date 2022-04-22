package edu.xidian.kai.ncov.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.qweather.sdk.bean.air.AirNowBean
import com.qweather.sdk.bean.base.Code
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.bean.base.Unit
import com.qweather.sdk.bean.history.HistoricalAirBean
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.HeConfig
import com.qweather.sdk.view.QWeather
import com.qweather.sdk.view.QWeather.OnResultWeatherNowListener
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import edu.xidian.kai.ncov.weather.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), TencentLocationListener{

    lateinit var mLocationManager: TencentLocationManager
    lateinit var mLocationRequest:TencentLocationRequest
    lateinit var province:String
    lateinit var city:String
    lateinit var district:String
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HeConfig.init("HE2204141324121430","6f88f9f5170c4a6b8350abb13adf8a95")
        HeConfig.switchToDevService()
        mLocationManager = TencentLocationManager.getInstance(this)
        mLocationRequest=TencentLocationRequest.create()
        TencentLocationManager.setUserAgreePrivacy(true)



        when(
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ){
           PackageManager.PERMISSION_GRANTED->
           {
               Toast.makeText(this,"granted permission",Toast.LENGTH_LONG).show()
               mLocationManager.requestLocationUpdates(mLocationRequest,this, Looper.getMainLooper())

           }
           PackageManager.PERMISSION_DENIED->
           {
               Toast.makeText(this,"granted denied",Toast.LENGTH_LONG).show()
               requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)

           }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==0){
            requestPermissions(permissions,0)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onLocationChanged(location: TencentLocation?, error: Int, reason: String?) {
        if (location != null) {
            //Toast.makeText(this,location.province+location.city+location.district,Toast.LENGTH_LONG).show()
            Toast.makeText(this,"error "+error,Toast.LENGTH_LONG).show()
            province=location.province
            city=location.city
            district=location.district
            binding.addressTextview.text=province+city+district
            var address=location.longitude.toString()+','+location.latitude.toString()
            //Toast.makeText(this,address,Toast.LENGTH_LONG).show()
            QWeather.getWeatherNow(
                this@MainActivity,
                address,
                Lang.ZH_HANS,
                Unit.METRIC,
                object : OnResultWeatherNowListener {
                    override fun onError(e: Throwable) {
                        Log.i("weather1", "getWeather onError: $e")
                    }

                    override fun onSuccess(weatherBean: WeatherNowBean) {
                        Log.i("weather1", "getWeather onSuccess: " + Gson().toJson(weatherBean))
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        Log.i("weather1", weatherBean.now.text)
                        if (Code.OK === weatherBean.code) {
                            val now = weatherBean.now
                        } else {
                            //在此查看返回数据失败的原因
                            val code: Code = weatherBean.code
                            Log.i("weather1", "failed code: $code")
                        }
                    }
                })
            QWeather.getAirNow(
                this@MainActivity,
                address,
                Lang.ZH_HANS,
                object :QWeather.OnResultAirNowListener{
                    override fun onError(e: Throwable?) {
                        Log.i("weather1", "getAir onError: $e")
                    }
                    override fun onSuccess(airBean: AirNowBean) {
                        Log.i("weather1", "getWeather onSuccess: " + Gson().toJson(airBean))
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        Log.i("weather1", airBean.now.aqi)
                        if (Code.OK===airBean.code){
                            val now=airBean.now
                        }
                        else{
                            val code: Code = airBean.code
                            Log.i("weather1", "failed code: $code")
                        }

                    }
                }
            )
            mLocationManager.removeUpdates(this)
        }
    }

    override fun onStatusUpdate(name: String?, status: Int, desc: String?) {
        Toast.makeText(this,desc,Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        mLocationManager.removeUpdates(this)
        super.onDestroy()

    }


}