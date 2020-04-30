package com.example.bagimakan;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.bagimakan.Model.Request;

public class BagiDialog extends AppCompatDialogFragment {

    private TextView txtUsername;
    private TextView txtJumlah;

    private BagiDialogListener listener;

    public static BagiDialog newInstance(String key, String userName, String jumlah) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putString("userName", userName);
        args.putString("jumlah", jumlah);
        BagiDialog fragment = new BagiDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String key = getArguments().getString("key");
        String userName = getArguments().getString("userName");
        String jumlah = getArguments().getString("jumlah");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_bagi, null);

        builder.setView(view)
                .setTitle("Anda yakin berbagi?")
                .setNegativeButton("Tolak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.bagiMakan(key, false);
                    }
                })
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.bagiMakan(key, true
                        );
                    }
                });


        txtUsername = view.findViewById(R.id.txtUsername_DialogBagi);
        txtJumlah = view.findViewById(R.id.txtJumlah_DialogBagi);

        txtUsername.setText(userName);
        txtJumlah.setText(jumlah);


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (BagiDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    public interface BagiDialogListener {
        void bagiMakan(String key, Boolean bagi);
    }

}
