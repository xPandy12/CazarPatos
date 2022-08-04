package com.marcosalazar.cazarpatos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RankingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        /*var jugadores = arrayListOf<Jugador>()
        jugadores.add(Jugador("Jugador1",10))
        jugadores.add(Jugador("Jugador2",6))
        jugadores.add(Jugador("Jugador3",3))
        jugadores.add(Jugador("Jugador4",9))
        jugadores.sortByDescending { it.patosCazados }

        val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.layoutManager = LinearLayoutManager(this);
        recyclerViewRanking.adapter = RankingAdapter(jugadores);
        recyclerViewRanking.setHasFixedSize(true);*/
        consultarPuntajeJugadoresRTDB()
    }
    fun consultarPuntajeJugadores() {
        val db = Firebase.firestore
        db.collection("ranking")
            .orderBy("patosCazados", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d(EXTRA_LOGIN, "Success getting documents")
                var jugadores = ArrayList<Jugador>()
                for (document in result) {
                    val jugador = document.toObject(Jugador::class.java)
                    //val jugador = document.toObject<Jugador>()
                    jugadores.add(jugador)
                }
                //Poblar en RecyclerView información usando mi adaptador
                val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking);
                recyclerViewRanking.layoutManager = LinearLayoutManager(this);
                recyclerViewRanking.adapter = RankingAdapter(jugadores);
                recyclerViewRanking.setHasFixedSize(true);
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error getting documents.", exception)
                Toast.makeText(this, "Error al obtener datos de jugadores", Toast.LENGTH_LONG)
                    .show()
            }
    }

    fun consultarPuntajeJugadoresRTDB() {
        val database = Firebase.database.reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.exists() &&
                    dataSnapshot.child("ranking").exists()&&
                    dataSnapshot.child("ranking").childrenCount>0
                ){
                    var jugadores = ArrayList<Jugador>()
                    for (player  in dataSnapshot.child("ranking").children){

                        jugadores.add(player.getValue<Jugador>() as Jugador)
                    }
                    //Poblar en RecyclerView información usando mi adaptador
                    val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking);
                    recyclerViewRanking.layoutManager = LinearLayoutManager(this@RankingActivity);
                    recyclerViewRanking.adapter = RankingAdapter(jugadores);
                    recyclerViewRanking.setHasFixedSize(true);
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(EXTRA_LOGIN, "loadPost:onCancelled", databaseError.toException())
            }
        }
        database.addValueEventListener(postListener)
    }

}