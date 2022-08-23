package com.marcosalazar.cazarpatos

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var textViewUsuario: TextView
    lateinit var textViewContador: TextView
    lateinit var textViewTiempo: TextView
    lateinit var imageViewPato: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: DatabaseReference
    private lateinit var  mAdView: AdView
    var contador = 0
    var anchoPantalla = 0
    var alturaPantalla = 0
    var gameOver = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Inicialización de variables
        textViewUsuario = findViewById(R.id.textViewUsuario)
        textViewContador = findViewById(R.id.textViewContador)
        textViewTiempo = findViewById(R.id.textViewTiempo)
        imageViewPato = findViewById(R.id.imageViewPato)
        mediaPlayer = MediaPlayer.create(this, R.raw.gunshot)
        //database = Firebase.database.reference

        MobileAds.initialize(this) {}

        mAdView = findViewById<AdView>(R.id.adView)
        //mAdView.adUnitId = "ca-app-pub-6551542058079007/8542304344" //TODO: Colocar esto antes de publicar
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //Obtener el usuario de pantalla login
        val extras = intent.extras ?: return
        var usuario = extras.getString(EXTRA_LOGIN) ?:"Unknown"
        usuario = usuario.substringBefore("@")
        textViewUsuario.setText(dividirUsuario(usuario))

        //Determina el ancho y largo de pantalla
        inicializarPantalla()
        //Cuenta regresiva del juego
        inicializarCuentaRegresiva()
        //Evento clic sobre la imagen del pato
        imageViewPato.setOnClickListener {
            if (gameOver) return@setOnClickListener
            contador++
            if (! mediaPlayer!!.isPlaying){
                mediaPlayer?.start()
            }
            textViewContador.setText(contador.toString())
            imageViewPato.setImageResource(R.drawable.duck_clicked)
            //Evento que se ejecuta luego de 500 milisegundos
            Handler().postDelayed(Runnable {
                imageViewPato.setImageResource(R.drawable.duck)
                moverPato()
                mediaPlayer?.pause()
                mediaPlayer?.seekTo(0)
            }, 600)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun inicializarPantalla() {
        // 1. Obtenemos el tamaño de la pantalla del dispositivo
        val display = this.resources.displayMetrics
        anchoPantalla = display.widthPixels
        alturaPantalla = display.heightPixels
    }

    private fun moverPato() {
        val min = imageViewPato.getWidth()/2
        val maximoX = anchoPantalla - imageViewPato.getWidth()
        val maximoY = alturaPantalla - imageViewPato.getHeight()
        // Generamos 2 números aleatorios, para la coordenadas x , y
        val randomX = Random().nextInt(maximoX - min + 1)
        val randomY = Random().nextInt(maximoY - min + 1)

        // Utilizamos los números aleatorios para mover el pato a esa nueva posición
        imageViewPato.setX(randomX.toFloat())
        imageViewPato.setY(randomY.toFloat())
    }
    var contadorTiempo = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val segundosRestantes = millisUntilFinished / 1000
            textViewTiempo.setText("${segundosRestantes}s")
        }
        override fun onFinish() {
            textViewTiempo.setText("0s")
            gameOver = true
            mostrarDialogoGameOver()
            val nombreJugador = textViewUsuario.text.toString()
            val patosCazados = textViewContador.text.toString()
            procesarPuntajePatosCazados(nombreJugador, patosCazados.toInt()) //Firestore
            //procesarPuntajePatosCazadosRTDB(nombreJugador, patosCazados.toInt()) //Realtime Database
        }
    }
    private fun inicializarCuentaRegresiva() {
        contadorTiempo.start()
    }
    private fun mostrarDialogoGameOver() {
        val builder = AlertDialog.Builder(this)
        builder
            .setMessage("Felicidades!!\nHas conseguido cazar $contador patos")
            .setIcon(R.drawable.duck)
            .setTitle("Fin del juego")
            .setPositiveButton("Reiniciar",
                { _, _ ->
                    reiniciarJuego()
                })
            .setNegativeButton("Cerrar",
                { _, _ ->
                    //dialog.dismiss()
                })
        builder.create().show()
    }

    fun reiniciarJuego(){
        contador = 0
        gameOver = false
        contadorTiempo.cancel()
        textViewContador.setText(contador.toString())
        moverPato()
        inicializarCuentaRegresiva()
    }

    override fun onDestroy() {
        //mediaPlayer?.release()
        super.onDestroy()
    }

    override fun onStop() {
        Log.w(EXTRA_LOGIN, "Play canceled")
        contadorTiempo.cancel()
        textViewTiempo.text = "0s"
        gameOver = true
        //mediaPlayer?.stop()
        super.onStop()
    }

    fun jugarOnline(){
        var intentWeb = Intent()
        intentWeb.action = Intent.ACTION_VIEW
        intentWeb.data = Uri.parse("https://duckhuntjs.com/")
        startActivity(intentWeb)
    }


    private fun dividirUsuario(usuario:String):String{
        val list = usuario.split("@")
        return list[0]
    }

    fun procesarPuntajePatosCazados(nombreJugador:String, patosCazados:Int){
        val jugador = Jugador(nombreJugador,patosCazados)
        //Trata de obtener id del documento del ranking específico,
        // si lo obtiene lo actualiza, caso contrario lo crea
        val db = Firebase.firestore
        db.collection("ranking")
            .whereEqualTo("usuario", jugador.usuario)
            .get()
            .addOnSuccessListener { documents ->
                if(documents!= null &&
                    documents.documents != null &&
                    documents.documents.count()>0
                ){
                    val idDocumento = documents.documents.get(0).id
                    actualizarPuntajeJugador(idDocumento, jugador)
                }
                else{
                    ingresarPuntajeJugador(jugador)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error getting documents", exception)
                Toast.makeText(this, "Error al obtener datos de jugador", Toast.LENGTH_LONG).show()
            }
    }

    fun procesarPuntajePatosCazadosRTDB(nombreJugador:String, patosCazados:Int){
        val jugador = Jugador(nombreJugador,patosCazados)
        val nombreJugadorNuevo = nombreJugador.replace(".","_")
        database.child("ranking").child(nombreJugadorNuevo).setValue(jugador)
    }

    fun ingresarPuntajeJugador(jugador:Jugador){
        val db = Firebase.firestore
        db.collection("ranking")
            .add(jugador)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this,"Puntaje usuario ingresado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error adding document", exception)
                Toast.makeText(this,"Error al ingresar el puntaje", Toast.LENGTH_LONG).show()
            }
    }
    fun actualizarPuntajeJugador(idDocumento:String, jugador:Jugador){
        val db = Firebase.firestore
        db.collection("ranking")
            .document(idDocumento)
            //.update(contactoHashMap)
            .set(jugador) //otra forma de actualizar
            .addOnSuccessListener {
                Toast.makeText(this,"Puntaje de usuario actualizado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error updating document", exception)
                Toast.makeText(this,"Error al actualizar el puntaje" , Toast.LENGTH_LONG).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_nuevo_juego -> {
                reiniciarJuego()
                true
            }
            R.id.action_jugar_online -> {
                var intentWeb = Intent()
                intentWeb.action = Intent.ACTION_VIEW
                intentWeb.data = Uri.parse("https://duckhuntjs.com")
                startActivity(intentWeb)
                true
            }
            R.id.action_ranking -> {
                val intencion = Intent(this, RankingActivity::class.java)
                startActivity(intencion)
                true
            }

            R.id.salir -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}
