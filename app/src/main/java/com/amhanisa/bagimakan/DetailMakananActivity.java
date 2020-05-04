package com.amhanisa.bagimakan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amhanisa.bagimakan.Adapter.RequestAdapter;
import com.amhanisa.bagimakan.Adapter.ViewImageAdapter;
import com.amhanisa.bagimakan.Model.Makanan;
import com.amhanisa.bagimakan.Model.Request;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DetailMakananActivity extends AppCompatActivity implements MintaDialog.MintaDialogListener, BagiDialog.BagiDialogListener {

    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser user;

    private ViewPager viewImage;
    private ViewImageAdapter viewAdapter;
    private List<String> imageUri;

    private TextView namaMakanan;
    private TextView deskripsiMakanan;
    private TextView lokasiMakanan;
    private TextView jumlahMakanan;
    private TextView dateMakanan;
    private TextView userName;
    private TextView kontak;

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;

    private Button btnMinta;

    private String MAKANAN_KEY;
    private String MAKANAN_USER_ID;
    private Makanan makanan;

    DocumentReference docRef;
    ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_makanan);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        namaMakanan = findViewById(R.id.txtNamaMakananDetail);
        deskripsiMakanan = findViewById(R.id.txtDeskripsiMakananDetail);
        jumlahMakanan = findViewById(R.id.txtJumlahMakananDetail);
        lokasiMakanan = findViewById(R.id.txtLokasiMakananDetail);
        dateMakanan = findViewById(R.id.txtDateMakananDetail);
        viewImage = findViewById(R.id.viewPagerMakanan);
        userName = findViewById(R.id.txtUserNameDetail);
        kontak = findViewById(R.id.txtKontakDetail);
        btnMinta = findViewById(R.id.btnMintaMakanan);

        onNewIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAdapter.startListening();
//        getDetailMakanan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestAdapter.stopListening();
        registration.remove();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.hasExtra("key")) {
            MAKANAN_KEY = intent.getStringExtra("key");
            MAKANAN_USER_ID = intent.getStringExtra("userId");
            Log.e("NEWINTENT", MAKANAN_KEY.toString());
        } else {
            finish();
        }

        getDetailMakanan();

        setButtonClickListener();

        setupRequestMakanan();
        requestAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (checkUserPemilikMakanan(makanan.getUserId())) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_detail_makanan, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:
                hapusMakanan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getDetailMakanan() {

        //get detail makanan
        docRef = db.collection("makanan").document(MAKANAN_KEY);

        registration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("Detail", "Listener Failed", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    makanan = documentSnapshot.toObject(Makanan.class);
                    makanan.setKey(documentSnapshot.getId());

                    if (checkUserPemilikMakanan(makanan.getUserId())) {
                        btnMinta.setVisibility(View.GONE);
                    } else {
                        btnMinta.setVisibility(View.VISIBLE);
                    }

                    namaMakanan.setText(makanan.getName());
                    deskripsiMakanan.setText(makanan.getDeskripsi());
                    jumlahMakanan.setText("Sisa " + makanan.getJumlah().toString());
                    lokasiMakanan.setText(makanan.getLokasi());
                    long timeInMillis = makanan.getDate().getTime();
                    dateMakanan.setText(DateUtils.getRelativeTimeSpanString(timeInMillis));
                    userName.setText(makanan.getUserName());
                    kontak.setText(makanan.getKontak());

                    db.collection("makanan")
                            .document(MAKANAN_KEY)
                            .collection("image")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    imageUri = new ArrayList<>();
                                    for (QueryDocumentSnapshot hasil : queryDocumentSnapshots) {
                                        imageUri.add(hasil.getString("imageUri"));
                                        Log.e("ASD", hasil.getString("imageUri"));
                                    }

                                    viewAdapter = new ViewImageAdapter(DetailMakananActivity.this, imageUri);
                                    viewImage.setAdapter(viewAdapter);

                                }
                            });

                }
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
                if (checkUserPemilikMakanan(MAKANAN_USER_ID) && documentSnapshot.getString("status").equals("requested")) {
                    Makanan makanan = documentSnapshot.toObject(Makanan.class);
                    BagiDialog dialog = BagiDialog.newInstance(documentSnapshot.getId(), makanan.getUserName(), makanan.getJumlah());
                    dialog.show(getSupportFragmentManager(), "Bagi Makan Dialog");
                }
            }
        });

    }

    private void setButtonClickListener() {
        btnMinta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MintaDialog dialog = MintaDialog.newInstance(makanan.getJumlah());
                dialog.show(getSupportFragmentManager(), "Dialog Minta Makan");
            }
        });

        lokasiMakanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:" + makanan.getLat() + "," + makanan.getLng() + "?q=" + makanan.getLokasi());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }


    private Boolean checkUserPemilikMakanan(String MakananUserId) {
        return (user.getUid().equals(MakananUserId));
    }

    @Override
    public void mintaMakan(String jumlahMinta, String alasan) {

        if (jumlahMinta.isEmpty() || alasan.isEmpty()) {
            Toast.makeText(this, "Isi jumlah dan alasan", Toast.LENGTH_SHORT).show();
        } else {
            String userId = user.getUid();
            String userName = user.getDisplayName();
            Request request = new Request(userId, userName, alasan, Integer.parseInt(jumlahMinta), "requested");

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
    }

    @Override
    public void bagiMakan(String key, Integer jumlah, Boolean bagi) {
        String status;
        if (bagi && makanan.getJumlah() >= jumlah) {
            status = "Disetujui";
        } else if (makanan.getJumlah() < jumlah) {
            status = "Sisa makanan kurang";
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
