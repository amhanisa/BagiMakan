package com.example.bagimakan;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bagimakan.Model.Makanan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentBagiMakan extends Fragment {

    public static final int PICK_IMAGE_REQUEST = 1;

    private EditText inputNamaMakanan;
    private EditText inputJumlahMakanan;
    private EditText inputLokasi;
    private Button btnChoosePhoto;
    private Button btnBagiMakanan;
    private ImageView imageMakanan;
    private ProgressBar progressBagiMakan;

    private Uri imageUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private StorageTask uploadTask;

    public FragmentBagiMakan() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bagimakan, container, false);

        inputNamaMakanan = view.findViewById(R.id.inputNamaMakanan);
        inputJumlahMakanan = view.findViewById(R.id.inputJumlahMakanan);
        inputLokasi = view.findViewById(R.id.inputLokasi);
        btnChoosePhoto = view.findViewById(R.id.btnChoosePhoto);
        btnBagiMakanan = view.findViewById(R.id.btnBagiMakanan);
        imageMakanan = view.findViewById(R.id.imageMakanan);
        progressBagiMakan = view.findViewById(R.id.progressBagiMakan);

        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        btnBagiMakanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nama = inputNamaMakanan.getText().toString();
                String jumlah = inputJumlahMakanan.getText().toString();
                String lokasi = inputLokasi.getText().toString();

                //validasi input
                if (TextUtils.isEmpty(nama)) {
                    inputNamaMakanan.setError("Please enter nama makanan");
                } else if (TextUtils.isEmpty(jumlah)) {
                    inputJumlahMakanan.setError("Please enter jumlah makanan");
                } else if (TextUtils.isEmpty(lokasi)) {
                    inputLokasi.setError("Please enter lokasi");
                } else if (imageUri == null) {
                    Toast.makeText(getContext(), "No File selected", Toast.LENGTH_LONG).show();
                } else{
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Log.e("ERRORRRR", "WOOOY");
                        Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                    } else {
                        //upload image dan add database
                        bagiMakanan();
                    }
                }
            }
        });

        return view;
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
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void bagiMakanan() {

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

                                user = FirebaseAuth.getInstance().getCurrentUser();

                                //create POJO dari makanan
                                Makanan makanan = new Makanan(inputNamaMakanan.getText().toString(),
                                        Integer.parseInt(inputJumlahMakanan.getText().toString()),
                                        inputLokasi.getText().toString(),
                                        uri.toString(),
                                        user.getDisplayName(),
                                        user.getUid(),
                                        new Date());

                                //add data to database
                                db = FirebaseFirestore.getInstance();
                                db.collection("makanan")
                                        .add(makanan)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                //redirect ke home
                                                Toast.makeText(getContext(), "Berhasil Berbagi Makan", Toast.LENGTH_LONG).show();
                                                ((DashboardActivity) getActivity()).replaceFragment(new FragmentHome());
                                            }
                                        });
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressBagiMakan.setProgress((int) progress);
                    }
                });
    }
}
