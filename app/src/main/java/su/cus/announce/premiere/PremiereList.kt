package su.cus.announce.premiere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import su.cus.announce.API.MoviesRepository.ItemMoviesList
import su.cus.announce.API.MoviesRepository.MoviesPremiere
import su.cus.announce.API.RetrofitClient
import su.cus.announce.DataCache
import su.cus.announce.DescriptionActivity.DescriptionActivity
import su.cus.announce.R
import su.cus.announce.databinding.ListPremiereBinding


class PremiereList : ComponentActivity(), OnItemsClickListener {

//    private lateinit var apiService: MoviesApiService
    private lateinit var binding: ListPremiereBinding
    lateinit var recyclerView: RecyclerView
    val cache = DataCache(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListPremiereBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager  = LinearLayoutManager(this)
//        recyclerView.adapter = PremiereListAdapter()
        this.loadMovies()
    }



    private fun loadMovies() {
        val filename = "movies.cache"
        val cachedData = cache.readFromCache(filename)
        if (cachedData == null) {
            RetrofitClient.instance.getMovies(2023, "JANUARY").enqueue(object :
                Callback<MoviesPremiere> {
                override fun onResponse(
                    call: Call<MoviesPremiere>,
                    response: Response<MoviesPremiere>
                ) {
//                Log.d("response", "Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        val moviesList = response.body()?.items ?: emptyList()
                        cache.writeToCache(filename, Json.encodeToString(moviesList))
                        recyclerView.adapter = PremiereListAdapter(moviesList, this@PremiereList)
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@PremiereList, "Failed to load movies: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MoviesPremiere>, t: Throwable) {
                    Log.d("response", "Response code: $t")
                    Toast.makeText(this@PremiereList, "Failed to load movies: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Parse cached data and use it
            val moviesList = Json.decodeFromString<List<ItemMoviesList>>(cachedData)

            recyclerView.adapter = PremiereListAdapter(moviesList, this)

        }

    }

    override fun onItemsClick(movieId: String) {
        // Обработка клика по элементу
        // Например, запуск новой Activity с деталями фильма

        val intent = Intent(this, DescriptionActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        startActivity(intent)
    }


}



