package com.marcosalazar.cazarpatos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        var jugadores = arrayListOf<Jugador>()
        jugadores.add(Jugador("Jugador1",10))
        jugadores.add(Jugador("Jugador2",6))
        jugadores.add(Jugador("Jugador3",3))
        jugadores.add(Jugador("Jugador4",9))
        jugadores.sortByDescending { it.patosCazados }

        val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.layoutManager = LinearLayoutManager(this);
        recyclerViewRanking.adapter = RankingAdapter(jugadores);
        recyclerViewRanking.setHasFixedSize(true);

    }
}