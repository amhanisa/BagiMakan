package com.example.bagimakan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bagimakan.Adapter.RequestAdapter;
import com.example.bagimakan.Model.Makanan;
import com.example.bagimakan.Model.Request;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class DetailMakananActivity extends AppCompatActivity implements MintaDialog.MintaDialogListener, BagiDialog.BagiDialogListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser user;

    private ImageView imageView;
    private TextView namaMakanan;
    private TextView lokasiMakanan;
    private TextView jumlahMakanan;
    private TextView dateMakanan;
    private TextView userName;

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;

    private Button btnDelete;
    private Button btnMinta;

    private String MAKANAN_KEY;
    private String MAKANAN_USER_ID;
    private Makanan makanan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_makanan);

        Intent intent = getIntent();

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        user = firebaseAuth.getInstance().getCurrentUser();

        namaMakanan = findViewById(R.id.txtNamaMakananDetail);
        jumlahMakanan = findViewById(R.id.txtJumlahMakananDetail);
        lokasiMakanan = findViewById(R.id.txtLokasiMakananDetail);
        dateMakanan = findViewById(R.id.txtDateMakananDetail);
        imageView = findViewById(R.id.imageMakananDetail);
        userName = findViewById(R.id.txtUserNameDetail);
        btnDelete = findViewById(R.id.btnDeleteMakanan);
        btnMinta = findViewById(R.id.btnMintaMakanan);

        if (intent.hasExtra("key")) {
            MAKANAN_KEY = intent.getStringExtra("key");
            MAKANAN_USER_ID = intent.getStringExtra("userId");
        } else {
            finish();
        }

        //get detail makanan
        DocumentReference docRef = db.collection("makanan").document(MAKANAN_KEY);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                makanan = documentSnapshot.toObject(Makanan.class);
                makanan.setKey(documentSnapshot.getId());

                if (checkUserPemilikMakanan(makanan.getUserId())) {
                    btnMinta.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);

                } else {
                    btnDelete.setVisibility(View.GONE);
                    btnMinta.setVisibility(View.VISIBLE);
                }

                namaMakanan.setText(makanan.getName());
                jumlahMakanan.setText("Jumlah " + makanan.getJumlah().toString());
                lokasiMakanan.setText("Lokasi " + makanan.getLokasi());
                long timeInMillis = makanan.getDate().getTime();
                dateMakanan.setText(DateUtils.getRelativeTimeSpanString(timeInMillis));
                userName.setText(makanan.getUserName());
                Picasso.get().load(makanan.getImageUrl()).fit().centerCrop().into(imageView);
            }
        });

        setupRequestMakanan();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hapusMakanan();
            }
        });

        btnMinta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MintaDialog dialog = new MintaDialog();
                dialog.show(getSupportFragmentManager(), "Dialog Minta Makan");
            }
        });

    }

    private void setupRequestMakanan() {
        Query query = db.collection("makanan")
                .document(MAKANAN_KEY)
                .collection("request")
                .orderBy("jumlah", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Request> options = new FirestoreRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();

        requestAdapter = new RequestAdapter(options);

        recyclerView = findViewById(R.id.recyclerViewRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(requestAdapter);

        //add listener on request recycle view
        requestAdapter.setOnRequestClicked(new RequestAdapter.onRequestClickListener() {
            @Override
            public void onRequestClicked(DocumentSnapshot documentSnapshot, int position) {
                if (checkUserPemilikMakanan(MAKANAN_USER_ID)) {

                    Log.e("asd", "ASDASD");
                    BagiDialog dialog = BagiDialog.newInstance(documentSnapshot.getId(), documentSnapshot.getString("userName"), documentSnapshot.getLong("jumlah").toString());
                    dialog.show(getSupportFragmentManager(), "Bagi Makan Dialog");

                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestAdapter.stopListening();
    }

    private Boolean checkUserPemilikMakanan(String MakananUserId) {
        return (user.getUid().equals(MakananUserId));
    }

    @Override
    public void mintaMakan(String jumlahMinta) {
        String userId = user.getUid();
        String userName = user.getDisplayName();
        Request request = new Request(userId, userName, Integer.parseInt(jumlahMinta), "requested");

        db.collection("makanan")
                .document(MAKANAN_KEY)
                .collection("request")
                .add(request)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DetailMakananActivity.this, "Permintaan anda sedang diproses", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailMakananActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void bagiMakan(String key, Boolean bagi) {
        String status;
        if (bagi) {
            status = "Disetujui";
        } else {
            status = "Ditolak";
        }

        Map<String, Object> data = new HashMap<>();
        data.put("status", status);

        db.collection("makanan")
                .document(MAKANAN_KEY)
                .collection("request")
                .document(key)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(DetailMakananActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void hapusMakanan() {
        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(makanan.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("makanan")
                        .document(MAKANAN_KEY).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DetailMakananActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailMakananActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
