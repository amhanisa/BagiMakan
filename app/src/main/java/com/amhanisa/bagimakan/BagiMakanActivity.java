package com.amhanisa.bagimakan;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.amhanisa.bagimakan.Adapter.InputImageAdapter;
import com.amhanisa.bagimakan.Model.Makanan;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BagiMakanActivity extends AppCompatActivity {

    public int PICK_IMAGE_REQUEST = 1;
    public int PICK_ADDRESS_REQUEST = 999;

    private EditText inputNamaMakanan;
    private EditText inputDeskripsiMakanan;
    private EditText inputJumlahMakanan;
    private EditText inputLokasi;
    private Button btnChoosePhoto;
    private ProgressBar progressBagiMakan;
    private ImageButton btnMaps;

    private RecyclerView inputImage;
    private InputImageAdapter imageAdapter;

    private List<Uri> uploadedList;
    private List<Uri> imageList;
    private String makananId;
    Task<List<Object>> uploadTasks;
    private LatLng latlng;

    private StorageReference storageReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Integer progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bagi_makan);

        inputNamaMakanan = findViewById(R.id.inputNamaMakanan);
        inputDeskripsiMakanan = findViewById(R.id.inputDeskripsiMakanan);
        inputJumlahMakanan = findViewById(R.id.inputJumlahMakanan);
        inputLokasi = findViewById(R.id.inputLokasi);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        progressBagiMakan = findViewById(R.id.progressBarBagiMakan);
        btnMaps = findViewById(R.id.btnMaps);

        imageList = new ArrayList<>();
        uploadedList = new ArrayList<>();

        imageAdapter = new InputImageAdapter(imageList);

        inputImage = findViewById(R.id.recyclerViewInputImage);
        inputImage.setNestedScrollingEnabled(false);
        inputImage.setLayoutManager(new LinearLayoutManager(this));
        inputImage.setAdapter(imageAdapter);

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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                imageList.remove(viewHolder.getAdapterPosition());
                imageAdapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(inputImage);
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
            latlng = new LatLng(data.getDoubleExtra("lat", 0),
                    data.getDoubleExtra("lng", 0));
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    private void bagiMakanan() {

        if (!validasiInput()) {
            return;
        }

        //generate makanan id
        DocumentReference ref = db.collection("makanan").document();
        makananId = ref.getId();

        //bikin tasks buat upload semua gambar
        List<Task<UploadTask.TaskSnapshot>> taskList = new ArrayList<>();

        for (int i = 0; i < imageList.size(); i++) {
            taskList.add(uploadImage(imageList.get(i)));
        }

        //klo semua gambar kelar diupload, bikin database
        uploadTasks = Tasks.whenAllSuccess(taskList).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {

                db.collection("users").document(user.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    //create POJO makanan
                                    Makanan makanan = new Makanan(inputNamaMakanan.getText().toString(),
                                            inputDeskripsiMakanan.getText().toString(),
                                            Integer.parseInt(inputJumlahMakanan.getText().toString()),
                                            inputLokasi.getText().toString(),
                                            latlng.latitude,
                                            latlng.longitude,
                                            uploadedList.get(0).toString(),
                                            user.getDisplayName(),
                                            user.getUid(),
                                            new Date(),
                                            documentSnapshot.getString("kontak"));

                                    //add to database
                                    db.collection("makanan").document(makananId)
                                            .set(makanan)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
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

    private Task<UploadTask.TaskSnapshot> uploadImage(Uri uri) {
        storageReference = FirebaseStorage.getInstance().getReference("makanan/" + makananId);
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        return fileReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final Map<String, String> imageUri = new HashMap<>();
                                imageUri.put("imageUri", uri.toString());
                                uploadedList.add(uri);

                                db.collection("makanan").document(makananId)
                                        .collection("image")
                                        .add(imageUri)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.e("ImageURI", imageUri.toString());
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        progress += (int) (100 / imageList.size() * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressBagiMakan.setProgress(progress);
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
        } else if (imageList.isEmpty()) {
            Toast.makeText(BagiMakanActivity.this, "No File selected", Toast.LENGTH_LONG).show();
        } else if (uploadTasks != null) {
            Log.e("ERRORRRR", "WOOOY");
            Toast.makeText(BagiMakanActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

}

