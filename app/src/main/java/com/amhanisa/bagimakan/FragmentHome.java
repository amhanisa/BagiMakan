package com.amhanisa.bagimakan;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amhanisa.bagimakan.Adapter.MakananAdapter;
import com.amhanisa.bagimakan.Model.Makanan;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {

    private RecyclerView recyclerView;
    private MakananAdapter makananAdapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupRecyclerView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        Query query = db.collection("makanan").orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Makanan> options = new FirestoreRecyclerOptions.Builder<Makanan>()
                .setQuery(query, Makanan.class)
                .build();

        makananAdapter = new MakananAdapter(options);

        recyclerView = view.findViewById(R.id.recyclerViewMakanan_Home);
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

        makananAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(positionStart);
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
