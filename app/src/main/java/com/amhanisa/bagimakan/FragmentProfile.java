package com.amhanisa.bagimakan;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amhanisa.bagimakan.Adapter.MakananAdapter;
import com.amhanisa.bagimakan.Model.Makanan;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FragmentProfile extends Fragment {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button btnLogout;
    private TextView txtUser;
    private MakananAdapter makananAdapter;
    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    public FragmentProfile() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnLogout = view.findViewById(R.id.btnLogout);
        txtUser = view.findViewById(R.id.txtUser_Profile);

        txtUser.setText("Halo, " + user.getDisplayName() + " " + user.getEmail());

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });

        setupRecyclerView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        Query query = db.collection("makanan")
                .whereEqualTo("userId", user.getUid())
                .orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Makanan> options = new FirestoreRecyclerOptions.Builder<Makanan>()
                .setQuery(query, Makanan.class)
                .build();

        makananAdapter = new MakananAdapter(options);

        recyclerView = view.findViewById(R.id.recyclerViewMakanan_Profile);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(makananAdapter);

        makananAdapter.setOnItemClickListener(new MakananAdapter.onItemClickListener() {
            @Override
            public void onItemClicked(DocumentSnapshot documentSnapshot, int position) {
                Intent detail = new Intent(getContext(), DetailMakananActivity.class);
                detail.putExtra("key", documentSnapshot.getId());
                detail.putExtra("userId", documentSnapshot.getString("userId"));
                startActivity(detail);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        makananAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        makananAdapter.stopListening();
    }

}
