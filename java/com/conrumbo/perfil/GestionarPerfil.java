package com.conrumbo.perfil;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.conrumbo.gestion.Buscador;
import com.conrumbo.gestion.Creacion;
import com.conrumbo.R;
import com.conrumbo.gestion.IniciarSesion;
import com.conrumbo.modelo.ModeloPerfil;
import com.conrumbo.rutas.RutasPropiasFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import android.net.Uri;


public class GestionarPerfil extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DrawerLayout.DrawerListener{

    //datos necesarios
    private String correo;
    private String uid;
    private ModeloPerfil mp;

    //elementos de perfil
    private Perfil perfil = new Perfil();

    //datos necesarios para las fotos
    private Uri archivo;
    private com.makeramen.roundedimageview.RoundedImageView foto_cabecera;    //imagen de la cabecera del menú
    private com.makeramen.roundedimageview.RoundedImageView imagen_iv;        //imagen del perfil

    //datos para el tab
    private TabLayout tab;              //tab
    private DrawerLayout drawer;        //menú drawer

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //mostramos la pantalla de iniciar sesión
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perf_general);

        //asignamos el correo y el uid
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            correo = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mp = new ModeloPerfil(GestionarPerfil.this, uid);

            //asignamos los imageView correspondientes de la foto de perfil y la cabecera
            imagen_iv = findViewById(R.id.imagen_perfil);
            foto_cabecera = findViewById(R.id.imagen_cabecera);

            //obtenemos el perfil: los datos y la imagen
            obtenerPerfil();
            obtenerImagenPerfil();

            //se inicializa el navigation view, el navigation bottom y el tab
            inicializarNavigationView();
            inicializarNavigationBottom();
            inicializarTab();
        }
        else{
            startActivity(new Intent(this, IniciarSesion.class)); finish();
        }
    }


    /* DATOS DEL PERFIL */
    //obtener el nombre del usuario
    private void obtenerPerfil(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("usuarios").document(uid).get().addOnCompleteListener(task -> {
            //si la consulta se ha realizado
            if (task.isSuccessful()) {
                //obtenemos el nombre del usuario y llamamos a establecerPerfil
                if (task.getResult().exists()) {
                    perfil.setPerfil(task.getResult().getData());
                    actualizarPerfil();
                }
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.perfil_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //actualizar los datos en la vista
    private void actualizarPerfil(){
        //obtenemos el nombre y lo actualizamos
        TextView nomTV = findViewById(R.id.nombre_perfil);
        if(perfil.getNombre() == null) { nomTV.setVisibility(View.GONE); }
        else{ nomTV.setText(perfil.getNombre()); }

        //obtenemos la descripción y lo actualizamos
        TextView descTV = findViewById(R.id.descripcion_perfil);
        if(perfil.getDescripcion() == null){ descTV.setVisibility(View.GONE); }
        else{ descTV.setText(perfil.getDescripcion()); }

        //obtenemos el enlace y lo actualizamos
        TextView enlTV = findViewById(R.id.enlace_perfil);
        if(perfil.getEnlace() == null){ enlTV.setVisibility(View.GONE); }
        else{ enlTV.setText(perfil.getEnlace()); }
    }

    //obtener la foto de perfil
    private void obtenerImagenPerfil(){
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("fotos_perfil/" + uid + ".jpg");
        sr.getBytes(2048*2048)
                .addOnSuccessListener(bytes -> {
                    perfil.setImagen(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    actualizarFotoPerfil();
                })
                .addOnFailureListener(e -> perfil.setImagen(null));
    }

    //actualizar en la vista la imagen de perfil
    private void actualizarFotoPerfil(){
        //obtenemos la imagen y la actualizamos
        if(perfil.getImagen() != null) {
            imagen_iv = findViewById(R.id.imagen_perfil);
            imagen_iv.setImageBitmap(perfil.getImagen());
        }
    }


    /* INICIALIZAR NAVIGATION VIEW */
    private void inicializarNavigationView(){
        //configuramos la barra de arriba
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("");
        setSupportActionBar(tb);

        //se integra el menú con la barra de arriba
        drawer = findViewById(R.id.drawer_layout_perfil);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tb, R.string.nav_app_bar_open_drawer_description,R.string.nav_app_bar_navigate_up_description);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_perfil);
        navigationView.setNavigationItemSelectedListener(this);
        drawer.addDrawerListener(this);
    }


    /* INICIALIZAR EL NAVIGATION BOTTOM */
    private void inicializarNavigationBottom(){
        BottomNavigationView bnv = findViewById(R.id.bottom_bar);
        bnv.setOnItemSelectedListener(item -> { barra_nav(item.getItemId()); return true; });
    }

    //para la barra de navegación
    private void barra_nav(int id){
        if(id == R.id.buscador){ startActivity(new Intent(this, Buscador.class)); }
        else if(id == R.id.creacion){
            startActivity(new Intent(this, Creacion.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }else if(id == R.id.perfil){
            finish();
            startActivity(getIntent());
        }
    }


    /* INICIALIZAR EL TAB */
    private void inicializarTab(){
        //inicializamos el tab
        tab = findViewById(R.id.tab);
        ViewPager2 viewpager2 = findViewById(R.id.view_pager2);

        FragmentManager fm = getSupportFragmentManager();
        viewpager2.setAdapter(new FragmentStateAdapter(fm, getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment;

                if(position == 0){ fragment = new RutasPropiasFragment(); }
                else{ fragment = new FavoritosPerfilFragment(); }

                return fragment;
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { viewpager2.setCurrentItem(tab.getPosition()); }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tab.selectTab(tab.getTabAt(position));
            }
        });
    }


    /* FUNCIONES PARA EL NAVIGATION DRAWER */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) { drawer.closeDrawer(GravityCompat.START); }
        else { super.onBackPressed(); }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

        //se actualiza el correo
        TextView correo_cabecera = findViewById(R.id.correo_cabecera);
        correo_cabecera.setText(correo);

        //si hay imagen, se actualiza la imagen
        if(perfil.getImagen() != null) {
            foto_cabecera = findViewById(R.id.imagen_cabecera);
            foto_cabecera.setImageBitmap(perfil.getImagen());
        }
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) { }

    @Override
    public void onDrawerStateChanged(int newState) { }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.modificar_datos){         modificarDatosPersonales(); }
        else if(id == R.id.modificar_correo) {  modificarCorreo(); }
        else if(id == R.id.modificar_imagen){   seleccionarImagen(); }
        else if(id == R.id.eliminar_datos) {    eliminarDatos(); }
        else if(id == R.id.cerrar_sesion){     mp.cerrarSesion(); }
        else if(id == R.id.eliminar_cuenta){    eliminarCuenta(); }

        if(id != R.id.eliminar_datos) { drawer.closeDrawer(GravityCompat.START); }
        return false;
    }

    /* modificar datos personales */
    private void modificarDatosPersonales(){
        ModificarDatosPersonalesFragment newFragment = new ModificarDatosPersonalesFragment();
        newFragment.setArguments(perfil.getBundle());
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    /* modificar correo */
    private void modificarCorreo(){
        ModificarCorreoFragment newFragment = new ModificarCorreoFragment();
        newFragment.setArguments(perfil.getBundle());
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    /*modificar imagen*/
    ActivityResultLauncher<Intent> fotografia_perfil = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        //se obtiene el uri de los datos
                        archivo = data.getData();
                        try {
                            //se obtiene el bitmap
                            perfil.setImagen(MediaStore.Images.Media.getBitmap(getContentResolver(), archivo));

                            //registrar la foto en la bd
                            mp.registrarFoto(archivo);
                            imagen_iv.setImageBitmap(perfil.getImagen());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fotografia_perfil.launch(Intent.createChooser(intent,
                getResources().getText(R.string.seleccionar_imagen)));
    }

    /* eliminar datos */
    private void eliminarDatos(){
        View bt = findViewById(R.id.eliminar_datos);
        PopupMenu popup = new PopupMenu(GestionarPerfil.this, bt);
        popup.inflate(R.menu.perf_nd_eliminar);
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if(id == R.id.nombre_eliminar) { mp.eliminarNombre(perfil.getNombre()); }
            else if(id == R.id.descripcion_eliminar){ mp.eliminarDescripcion(perfil.getDescripcion()); }
            else if(id == R.id.enlace_eliminar) { mp.eliminarEnlace(perfil.getEnlace()); }
            else if(id == R.id.foto_eliminar){ mp.eliminarFoto(perfil.getImagen()); }

            return true;
        });
    }

    /* eliminar cuenta */
    private void eliminarCuenta() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle(R.string.eliminar_cuenta);
        adb.setMessage(getResources().getText(R.string.eliminar_cuenta_mensaje));
        adb.setPositiveButton(getResources().getText(R.string.eliminar), (dialog, which) -> mp.eliminarCuentaUsuario(perfil.getImagen()));
        adb.setNegativeButton(getResources().getText(R.string.cancelar), (dialog, which) -> drawer.closeDrawer(GravityCompat.START));

        AlertDialog dialogo = adb.create();
        dialogo.show();
    }
}