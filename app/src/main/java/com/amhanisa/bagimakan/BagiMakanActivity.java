package com.amhanisa.bagimakan;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amhanisa.bagimakan.Model.Makanan;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class BagiMakanActivity extends AppCompatActivity {

    public int PICK_IMAGE_REQUEST = 1;
    public int PICK_ADDRESS_REQUEST = 999;

    private EditText inputNamaMakanan;
    private EditText inputDeskripsiMakanan;
    private EditText inputJumlahMakanan;
    private EditText inputLokasi;
    private Button btnChoosePhoto;
    private ImageView imageMakanan;
    private ProgressBar progressBagiMakan;
    private ImageButton btnMaps;

    private Uri imageUri = null;
    private LatLng latlng;

    private StorageReference storageReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bagi_makan);

        inputNamaMakanan = findViewById(R.id.inputNamaMakanan);
        inputDeskripsiMakanan = findViewById(R.id.inputDeskripsiMakanan);
        inputJumlahMakanan = findViewById(R.id.inputJumlahMakanan);
        inputLokasi = findViewById(R.id.inputLokasi);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        imageMakanan = findViewById(R.id.imageMakanan);
        progressBagiMakan = findViewById(R.id.progressBarBagiMakan);
        btnMaps = findViewById(R.id.btnMaps);

        inputJumlahMakanan.setFilters(new InputFilter[]{new InputFilterMinMax(1, 99999)});

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent maps = new Intent(BagiMakanActivity.this, MapsActivity.class);
                startActivityForResult(maps, PICK_ADDRESS_REQUEST);
            }
        });

        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bagi_makanan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_bagi_makanan:
                bagiMakanan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            //tampilkan image ke imageviewer
            Picasso.get().load(imageUri).into(imageMakanan);
        } else if (requestCode == PICK_ADDRESS_REQUEST && resultCode == RESULT_OK) {
            inputLokasi.setText(data.getStringExtra("address"));
            latlng = new LatLng(data.getDoubleExtra("lat", 0),
                    data.getDoubleExtra("lng", 0));
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void bagiMakanan() {

        if(!validasiInput()){
            return;
        }

        //set filename
        storageReference = FirebaseStorage.getInstance().getReference("makanan");
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

        //upload image
        uploadTask = fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //get download url dari uploaded image
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                final Uri imageUri = uri;

                                //get kontak
                                db.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {

                                            //create POJO dari makanan
                                            Makanan makanan = new Makanan(inputNamaMakanan.getText().toString(),
                                                    inputDeskripsiMakanan.getText().toString(),
                                                    Integer.parseInt(inputJumlahMakanan.getText().toString()),
                                                    inputLokasi.getText().toString(),
                                                    latlng.latitude,
                                                    latlng.longitude,
                                                    imageUri.toString(),
                                                    user.getDisplayName(),
                                                    user.getUid(),
                                                    new Date(),
                                                    documentSnapshot.getString("kontak"));

                                            //add data to database
                                            db.collection("makanan")
                                                    .add(makanan)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            //redirect ke home
                                                            Toast.makeText(BagiMakanActivity.this, "Berhasil Berbagi Makan", Toast.LENGTH_LONG).show();
                                                            finish();
                                                        }
                                                    });

                                        }
                                    }
                                });


                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BagiMakanActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressBagiMakan.setProgress((int) progress);
                    }
                });
    }

    private Boolean validasiInput() {
        String nama = inputNamaMakanan.getText().toString();
        String deskripsi = inputDeskripsiMakanan.getText().toString();
        String jumlah = inputJumlahMakanan.getText().toString();
        String lokasi = inputLokasi.getText().toString();

        //validasi input
        if (TextUtils.isEmpty(nama)) {
            inputNamaMakanan.setError("Please enter nama makanan");
        } else if (TextUtils.isEmpty(deskripsi)) {
            inputDeskripsiMakanan.setError("Please enter deskripsi makanan");
        } else if (TextUtils.isEmpty(jumlah)) {
            inputJumlahMakanan.setError("Please enter jumlah makanan");
        } else if (TextUtils.isEmpty(lokasi)) {
            inputLokasi.setError("Please enter lokasi");
        } else if (imageUri == null) {
            Toast.makeText(BagiMakanActivity.this, "No File selected", Toast.LENGTH_LONG).show();
        } else if (uploadTask != null && uploadTask.isInProgress()) {
            Log.e("ERRORRRR", "WOOOY");
            Toast.makeText(BagiMakanActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

}

