package com.javiercolv.gmapstest.views;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.javiercolv.gmapstest.R;
import com.javiercolv.gmapstest.model.Person;
import com.javiercolv.gmapstest.services.TaskParser;
import com.javiercolv.gmapstest.services.TaskRequestDirections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    private List<LatLng> listPoints;

    private ImageButton btnRun;
    private ImageButton btnPause;
    private ImageButton btnInfo;

    List<Marker> markers = new ArrayList<>();
    List<Polyline> grayPolys = new ArrayList<>();
    List<Polyline> blackPolys = new ArrayList<>();
    List<PolylineOptions> grayPolysOptions = new ArrayList<>();
    List<PolylineOptions> blackPolysOptions = new ArrayList<>();
    List<List<LatLng>> ListPolylineList = new ArrayList<>();

    List<Person> clientes;
    List<Person> conductores;

    private int numConductores;
    private int numClientes;
    //private List<LatLng> polylineList;
    //private Polyline grayPolyline;
    //private Polyline blackPolyline;

    private DrawerLayout drawerLayout;
    private ListView menuLateral;

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    //variables for animation
    private Marker personMarker;
    private Handler handler;
    private float v;
    private double latitude, longitude;
    private int index, next;

    private boolean exec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("onCreate", "inicializando app");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        String[] valores = getResources().getStringArray(R.array.drawer_ptions);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuLateral = (ListView) findViewById(R.id.drawer_list_options);
        View cabecera = getLayoutInflater().inflate(R.layout.header_menu, null);
        menuLateral.addHeaderView(cabecera);
        ArrayAdapter<String> adapternav = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, valores);
        menuLateral.setAdapter(adapternav);

        menuLateral.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //System.out.println(String.valueOf("Posicion: "+position));
                Intent intent = null;
                switch (position) {
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                }
                getSupportLoaderManager();
                menuLateral.setItemChecked(position, true);
                drawerLayout.closeDrawer(menuLateral);
            }
        });

        /**HABILITAR ICONO DE LA APLICACION PARA EL MENU DE NAVEGACION**/
        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout.addDrawerListener(toggle);

        listPoints = new ArrayList<>();
        clientes = new ArrayList<>();
        conductores = new ArrayList<>();

        btnRun = (ImageButton) findViewById(R.id.btn_play);
        btnPause = (ImageButton) findViewById(R.id.btn_pause);
        exec = true;
        switchBtns();
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exec=true;
                switchBtns();
                if (numConductores == 0) {
                    inicializarDatosDialog();
                } else {
                    clientes.clear();
                    conductores.clear();
                    initializePersons();
                    mMap.clear();
                    for (Person person : clientes) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(person.getUbication());
                        mMap.addMarker(markerOptions);
                    }
                }
                for (Person person : clientes) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(person.getUbication());
                    mMap.addMarker(markerOptions);
                    startClientsAnimation(person.getUbication(), person.getDestiny());
                }

            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exec = false;
                switchBtns();
            }
        });
    }

    private void switchBtns() {
        if (exec){
            btnPause.setClickable(true);
            btnPause.setImageResource(R.drawable.ic_pause);
            btnRun.setClickable(false);
            btnRun.setImageResource(R.drawable.ic_play_disabled);
        }else {
            btnPause.setClickable(false);
            btnPause.setImageResource(R.drawable.ic_pause_disabled);
            btnRun.setClickable(true);
            btnRun.setImageResource(R.drawable.ic_play);
        }
    }

    private void initializePersons() {
        Log.d("initializePersons", "inicializando datos de clientes y conductores");
        clientes.clear();
        conductores.clear();
        double latOrigen = mMap.getMyLocation().getLatitude();
        double lngOrigen = mMap.getMyLocation().getLongitude();

        for (int i = 0; i < numClientes; i++) {
            double latO, latD;
            double lngO, lngD;
            int posneg = (int) (Math.random() * 10);
            Log.d("Random value", String.valueOf(posneg));
            if (posneg <= 5) {
                latO = (Math.random() * -0.1) + latOrigen;
                lngO = (Math.random() * -0.1) + lngOrigen;
                latD = (Math.random() * 0.1) + latOrigen;
                lngD = (Math.random() * 0.1) + lngOrigen;
            } else {
                latO = (Math.random() * 0.1) + latOrigen;
                lngO = (Math.random() * 0.1) + lngOrigen;
                latD = (Math.random() * -0.1) + latOrigen;
                lngD = (Math.random() * -0.1) + lngOrigen;
            }
            clientes.add(new Person("Cliente" + i, "Numero" + i, new LatLng(latO, lngO), new LatLng(latD, lngD)));
            //listPoints.add(new LatLng(lat,lng));
            //Log.d("Random Ubication",String.valueOf(latO)+","+String.valueOf(lng));
        }
    }

    private void inicializarDatosDialog() {
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("Ingresa el numero de clientes");
        d.setCancelable(false);
        d.setContentView(R.layout.dialogo_configuracion);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final EditText txtNumClientes = (EditText) d.findViewById(R.id.txtNumClientes);
        final EditText txtNumConductores = (EditText) d.findViewById(R.id.txtNumConductores);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    numClientes = Integer.parseInt(txtNumClientes.getText().toString());
                    numConductores = Integer.parseInt(txtNumConductores.getText().toString());
                    initializePersons();

                    d.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();

    }

    private void startClientsAnimation(final LatLng origen, final LatLng destino) {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                        List<LatLng> polylineList = new ArrayList<>();
                        String url = getRequestUrl(origen, destino);
                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                        TaskParser taskParser = new TaskParser();
                        String routes = null;
                        try {
                            routes = taskRequestDirections.execute(url).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            return;
                        }
                        Log.d("Routes", routes);
                        List<List<HashMap<String, String>>> listRoutes = null;
                        try {
                            listRoutes = taskParser.execute(routes).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            return;
                        }
                        PolylineOptions polylineOptions = new PolylineOptions();
                        PolylineOptions blackPolylineOptions = new PolylineOptions();
                        for (List<HashMap<String, String>> path : listRoutes) {
                            for (HashMap<String, String> point : path) {
                                double lat = Double.parseDouble(point.get("lat"));
                                double lon = Double.parseDouble(point.get("lon"));
                                polylineList.add(new LatLng(lat, lon));
                            }
                        }
                        ListPolylineList.add(polylineList);
                        grayPolysOptions.add(polylineOptions);
                        blackPolysOptions.add(blackPolylineOptions);
                        if (polylineList != null) {
                            polylineOptions.addAll(polylineList);
                            polylineOptions.width(15);
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.geodesic(true);
                            final Polyline grayPolyline = mMap.addPolyline(polylineOptions);
                            grayPolys.add(grayPolyline);
                            blackPolylineOptions.addAll(polylineList);
                            blackPolylineOptions.width(15);
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.geodesic(true);
                            final Polyline blackPolyline = mMap.addPolyline(blackPolylineOptions);
                            blackPolys.add(blackPolyline);

                            final Marker mPersonMarker = mMap.addMarker(new MarkerOptions()
                                    .position(origen)
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_person))
                            );
                            markers.add(mPersonMarker);
                            final ValueAnimator polylineanimator = ValueAnimator.ofInt(0, 500);
                            polylineanimator.setDuration(((int)Math.random()*5000)+5000);
                            polylineanimator.setInterpolator(new LinearInterpolator());
                            polylineanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {

                                    List<LatLng> points = grayPolyline.getPoints();
                                    int percentValue = (int) animation.getAnimatedValue();
                                    int size = points.size();
                                    int newPoints = (int) (size * (percentValue / 500.0f));
                                    List<LatLng> p = points.subList(0, newPoints);
                                    blackPolyline.setPoints(p);
                                    if (p.size() > 0)
                                        mPersonMarker.setPosition(p.get(p.size() - 1));
                                    if (p.size() == points.size()) {
                                        // Toast.makeText(getApplicationContext(), "Haz llegado a tu destino", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            polylineanimator.addPauseListener(new Animator.AnimatorPauseListener() {
                                @Override
                                public void onAnimationPause(Animator animation) {
                                    if (exec) {
                                        polylineanimator.pause();
                                    } else {
                                        polylineanimator.resume();
                                    }
                                }

                                @Override
                                public void onAnimationResume(Animator animation) {

                                }
                            });
                            polylineanimator.start();
                            Log.d("Animator execution",String.valueOf(exec));
                            if (!exec) {
                                polylineanimator.pause();
                            } else {
                                polylineanimator.resume();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_LONG).show();
                            ;
                        }

            }
        }, 5000);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("On Map Ready", "se inicia Mapa");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
                if (listPoints.size() == 3) {
                    listPoints.clear();
                    markers.clear();
                    grayPolys.clear();
                    blackPolys.clear();
                    grayPolysOptions.clear();
                    blackPolysOptions.clear();
                    ListPolylineList.clear();
                    mMap.clear();
                }

                listPoints.add(latLng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if (listPoints.size() >= 2) {
                    //LatLng origen = listPoints.get(0);

                    List<LatLng> polylineList = new ArrayList<>();
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(listPoints.size() - 1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    TaskParser taskParser = new TaskParser();
                    try {
                        String routes = taskRequestDirections.execute(url).get();
                        Log.d("Routes", routes);
                        List<List<HashMap<String, String>>> listRoutes = taskParser.execute(routes).get();
                        PolylineOptions polylineOptions = new PolylineOptions();
                        PolylineOptions blackPolylineOptions = new PolylineOptions();
                        for (List<HashMap<String, String>> path : listRoutes) {
                            for (HashMap<String, String> point : path) {
                                double lat = Double.parseDouble(point.get("lat"));
                                double lon = Double.parseDouble(point.get("lon"));
                                polylineList.add(new LatLng(lat, lon));
                            }
                        }

                        ListPolylineList.add(polylineList);
                        grayPolysOptions.add(polylineOptions);
                        blackPolysOptions.add(blackPolylineOptions);

                        if (polylineList != null) {
                            polylineOptions.addAll(polylineList);
                            polylineOptions.width(15);
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.geodesic(true);
                            final Polyline grayPolyline = mMap.addPolyline(polylineOptions);
                            grayPolys.add(grayPolyline);

                            blackPolylineOptions.addAll(polylineList);
                            blackPolylineOptions.width(15);
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.geodesic(true);
                            final Polyline blackPolyline = mMap.addPolyline(blackPolylineOptions);
                            blackPolys.add(blackPolyline);

                            final Marker mPersonMarker = mMap.addMarker(new MarkerOptions()
                                    .position(listPoints.get(0))
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_person))
                            );

                            markers.add(mPersonMarker);

                            //final int x=i;

                            //animator


                                /*
                                ValueAnimator polylineanimator = ValueAnimator.ofInt(0,500);
                                polylineanimator.setDuration(5000);
                                polylineanimator.setInterpolator(new LinearInterpolator());
                                polylineanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        List<LatLng> points = grayPolys.get(x).getPoints();
                                        int percentValue = (int)animation.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 500.0f));
                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolys.get(x).setPoints(p);
                                        if (p.size()>0)
                                            markers.get(x).setPosition(p.get(p.size()-1));
                                        if (p.size()==points.size()){
                                            Toast.makeText(getApplicationContext(),"Haz llegado a tu destino",Toast.LENGTH_LONG).show();;
                                        }
                                    }
                                });
                                polylineanimator.start();
                                */

                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                ;

                                @Override
                                public void run() {
                                    ValueAnimator polylineanimator = ValueAnimator.ofInt(0, 500);
                                    polylineanimator.setDuration(5000);
                                    polylineanimator.setInterpolator(new LinearInterpolator());
                                    polylineanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            List<LatLng> points = grayPolyline.getPoints();
                                            int percentValue = (int) animation.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size * (percentValue / 500.0f));
                                            List<LatLng> p = points.subList(0, newPoints);
                                            blackPolyline.setPoints(p);
                                            if (p.size() > 0)
                                                mPersonMarker.setPosition(p.get(p.size() - 1));
                                            if (p.size() == points.size()) {
                                                Toast.makeText(getApplicationContext(), "Haz llegado a tu destino", Toast.LENGTH_LONG).show();
                                                ;
                                            }
                                        }
                                    });
                                    polylineanimator.start();
                                }
                            }, 5000);

                                /*
                                handler = new Handler();


                                handler.postDelayed(new Runnable() {
                                    int index = -1;
                                    int next = 1;
                                    LatLng startPosition = new LatLng(1.1,1.2);
                                    LatLng endPosition = new LatLng(1.1,1.2);
                                    List<LatLng> p=new ArrayList<>();
                                    @Override
                                    public void run() {
                                        if (index < polylineList.size()-1){
                                            index ++;
                                            next = index +1;
                                        }
                                        if (index < polylineList.size()-1){
                                            startPosition = polylineList.get(index);
                                            endPosition = polylineList.get(next);
                                        }
                                        if (endPosition.equals(polylineList.get(polylineList.size()-1))){
                                            try {
                                                this.finalize();
                                            } catch (Throwable throwable) {
                                                throwable.printStackTrace();
                                            }
                                        }

                                        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
                                        valueAnimator.setDuration(1000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                v= valueAnimator.getAnimatedFraction();
                                                longitude = v*endPosition.longitude+(1-v)*startPosition.longitude;
                                                latitude = v*endPosition.latitude+(1-v)*startPosition.latitude;
                                                LatLng newPos = new LatLng(latitude,longitude);
                                                markers.get(x).setPosition(newPos);
                                                p.add(newPos);

                                                List<LatLng> points = grayPolys.get(x).getPoints();
                                                float percentValue = (float) animation.getAnimatedValue();
                                                int size = points.size();
                                                int newPoints = (int) (size * (percentValue / 100.0f));
                                                List<LatLng> p = points.subList(0,newPoints);

                                                blackPolys.get(x).setPoints(p);
                                            }
                                        });
                                        valueAnimator.start();
                                        handler.postDelayed(this,1000);
                                    }
                                },1000);
                                */

                        } else {
                            Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_LONG).show();
                            ;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
    }

    private String getRequestUrl(LatLng origen, LatLng destino) {
        String strOrigen = "origin=" + origen.latitude + "," + origen.longitude;
        String strDestino = "destination=" + destino.latitude + "," + destino.longitude;
        String sensor = "sensor=false";
        String mode = "mode=walking";
        String params = String.format("%s&%s&%s&%s", strOrigen, strDestino, sensor, mode);
        String outType = "json";
        String url = String.format("https://maps.googleapis.com/maps/api/directions/%s?%s", outType, params);
        Log.d("Request URL", url);
        return url;
    }

    /**
     * METODOS PARA MANEJAR LA APERTURA Y CIERRE DEL NAVIGATION DRAWER DESDE EL ICONO DE LA APLICACION
     */
    /**************************************************************************************************/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
        }
    }


}
