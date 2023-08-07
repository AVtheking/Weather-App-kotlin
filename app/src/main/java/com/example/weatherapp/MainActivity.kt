package com.example.weatherapp

import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private  val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val lastCity = getLastSearchedCity()
        binding.searchView.setQuery(lastCity, false)

        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            fetchWeatherData(lastCity)
            binding.progressBar.visibility = View.GONE

            SearchCity()
        }

    }

    private fun SearchCity() {
        binding.searchView.apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.progressBar.visibility= View.VISIBLE
                        if (query != null) {
                            saveLastSearchedCity(query)
                        }
                        fetchWeatherData(query!!)
                        binding.progressBar.visibility=View.GONE
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true

                }

            })
        }
    }

    suspend fun fetchWeatherData(cityName:String) {
        val response = RetrofitInstance.api.getWeather("$cityName", "9720bbf45ee7589a764736883c10bbb4", "metric")
        try {
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val condition1=responseBody.weather.firstOrNull()?.main?:"unknown"
                    val humidity=responseBody.main.humidity.toString()
                    val windSpeed=responseBody.wind.speed.toString()
                    val sunrise=responseBody.sys.sunrise.toLong()
                    val sunset=responseBody.sys.sunset.toLong()
                    val maxTemp=responseBody.main.temp_max.toString()
                    val minTemp=responseBody.main.temp_min.toString()
                    val sealevel=responseBody.main.pressure.toString()
//                    Log.d("TAG", "$temperature")
                    binding.apply {
                       temp.text = "$temperature °C"
                        weather.text=condition1
                        Max.text="Max:"+maxTemp +"°C"
                        Min.text="Min:"+minTemp +"°C"
                        humidityText.text=humidity+"%"
                        sunsetText.text="${time(sunset)}"
                        sunriseText.text="${time(sunrise)}"
                        windText.text=windSpeed +"m/s"
                        seaText.text=sealevel +"hpa"
                        condition.text=condition1
                        Day.text=dayname(System.currentTimeMillis())
                        Date.text=date()
                        cityLocation.text=cityName

                     changeBackgroundAccToWeatherCondition(condition1)

                    }
                } else {

                    Log.e("TAG", "Response body is null")
                }
            } else {
                Log.e("TAG", "Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Exception: ${e.message}")
        }
    }

    private fun changeBackgroundAccToWeatherCondition(conditions: String) {
        when(conditions)
        {
            "Haze","Partly Clouds","Clouds","OverCast","Mist","Foggy"->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky","Sunny","Clear"->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain"->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard"->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()

    }

    private fun date(): String {
        val sdf=SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun time(timeStamp: Long): String {
        val sdf=SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeStamp*1000))
    }

    private fun dayname(timeStamp: Long):String {
    val sdf=SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun saveLastSearchedCity(cityName: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("lastSearchedCity", cityName)
            apply()
        }

    }
    private fun getLastSearchedCity(): String {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("lastSearchedCity", "Delhi") ?: "Delhi"
    }



}