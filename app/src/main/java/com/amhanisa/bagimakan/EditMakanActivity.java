package com.amhanisa.bagimakan;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amhanisa.bagimakan.Adapter.InputImageAdapter;
import com.amhanisa.bagimakan.Model.Makanan;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class EditMakanActivity extends AppCompatActivity {

    public int PICK_IMAGE_REQUEST = 1;
    public int PICK_ADDRESS_REQUEST = 999;

    private EditText inputNamaMakanan;
    private EditText inputDeskripsiMakanan;
    private EditText inputJumlahMakanan;
    private EditText inputSatuanMakanan;
    private EditText inputLokasi;
    private Button btnChoosePhoto;
    private ProgressBar progressBagiMakan;
    private ImageButton btnMaps;

    private RecyclerView inputImage;
    private InputImageAdapter imageAdapter;

    private List<Uri> imageList;
    Task<List<Object>> uploadTasks;
    private LatLng latlng;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Makanan makanan;

    Integer progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bagi_makan);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputNamaMakanan = findViewById(R.id.inputNamaMakanan);
        inputDeskripsiMakanan = findViewById(R.id.inputDeskripsiMakanan);
        inputJumlahMakanan = findViewById(R.id.inputJumlahMakanan);
        inputSatuanMakanan = findViewById(R.id.inputSatuanMakanan);
        inputLokasi = findViewById(R.id.inputLokasi);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        progressBagiMakan = findViewById(R.id.progressBarBagiMakan);
        progressBagiMakan.setVisibility(GONE);
        btnMaps = findViewById(R.id.btnMaps);

        imageList = new ArrayList<>();

        imageAdapter = new InputImageAdapter(imageList);

        inputImage = findViewById(R.id.recyclerViewInputImage);
        inputImage.setNestedScrollingEnabled(false);
        inputImage.setLayoutManager(new LinearLayoutManager(this));
        inputImage.setAdapter(imageAdapter);

        inputJumlahMakanan.setFilters(new InputFilter[]{new InputFilterMinMax(1, 99999)});

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent maps = new Intent(EditMakanActivity.this, MapsActivity.class);
                startActivityForResult(maps, PICK_ADDRESS_REQUEST);
            }
        });

       btnChoosePhoto.setVisibility(GONE);
        setupData();
    }

    public void setupData() {
        final Intent getIntent = getIntent();

        if (getIntent.hasExtra("makananId")) {
            db.collection("makanan").document(getIntent.getStringExtra("makananId")).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            makanan = documentSnapshot.toObject(Makanan.class);

                            makanan.setKey(documentSnapshot.getId());
                            inputNamaMakanan.setText(makanan.getName());
                            inputDeskripsiMakanan.setText(makanan.getDeskripsi());
                            inputJumlahMakanan.setText(makanan.getJumlah().toString());
                            inputSatuanMakanan.setText(makanan.getSatuan());
                            inputLokasi.setText(makanan.getLokasi());

                            latlng = new LatLng(makanan.getLat(), makanan.getLng());

                            db.collection("makanan").document(makanan.getKey()).collection("image")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot image : queryDocumentSnapshots) {

                                                imageList.add(Uri.parse(image.getString("imageUri")));
                                            }
                                            imageAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_makanan, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_edit_makanan:
                updateMakan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getClipData() != null) {
            int totalItemSelected = data.getClipData().getItemCount();
            for (int i = 0; i < totalItemSelected; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                imageList.add(imageUri);
            }
            imageAdapter.notifyDataSetChanged();

        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            imageList.add(data.getData());
            imageAdapter.notifyDataSetChanged();

        } else if (requestCode == PICK_ADDRESS_REQUEST && resultCode == RESULT_OK) {
            inputLokasi.setText(data.getStringExtra("address"));
            makanan.setLat(data.getDoubleExtra("lat", 0));
            makanan.setLng(data.getDoubleExtra("lng", 0));
        }
    }

    private void updateMakan() {

        if (!validasiInput()) {
            return;
        }

        //update data
        makanan.setName(inputNamaMakanan.getText().toString());
        makanan.setDeskripsi(inputDeskripsiMakanan.getText().toString());
        makanan.setJumlah(Integer.parseInt(inputJumlahMakanan.getText().toString()));
        makanan.setSatuan(inputSatuanMakanan.getText().toString());
        makanan.setLokasi(inputLokasi.getText().toString());

        db.collection("makanan").document(makanan.getKey())
                .set(makanan, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditMakanActivity.this, "Berhasil update data", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });


        //TODO bikin sistem update foto...
    }

    private Boolean validasiInput() {
        String nama = inputNamaMakanan.getText().toString();
        String deskripsi = inputDeskripsiMakanan.getText().toString();
        String jumlah = inputJumlahMakanan.getText().toString();
        String satuan = inputSatuanMakanan.getText().toString();
        String lokasi = inputLokasi.getText().toString();

        //validasi input
        if (TextUtils.isEmpty(nama)) {
            inputNamaMakanan.setError("Please enter nama makanan");
        } else if (TextUtils.isEmpty(deskripsi)) {
            inputDeskripsiMakanan.setError("Please enter deskripsi makanan");
        } else if (TextUtils.isEmpty(jumlah)) {
            inputJumlahMakanan.setError("Please enter jumlah makanan");
        } else if (TextUtils.isEmpty(satuan)) {
            inputSatuanMakanan.setError("Please enter satuan makanan");
        } else if (TextUtils.isEmpty(lokasi)) {
            inputLokasi.setError("Please enter lokasi");
        } else if (imageList.isEmpty()) {
            Toast.makeText(EditMakanActivity.this, "No File selected", Toast.LENGTH_LONG).show();
        } else if (uploadTasks != null) {
            Log.e("ERRORRRR", "WOOOY");
            Toast.makeText(EditMakanActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

}

