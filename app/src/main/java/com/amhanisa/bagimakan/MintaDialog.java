package com.amhanisa.bagimakan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class MintaDialog extends AppCompatDialogFragment {
    private EditText inputMinta;
    private MintaDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_minta, null);

        builder.setView(view)
                .setTitle("Minta Berapa?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String jumlahMinta = inputMinta.getText().toString();
                        listener.mintaMakan(jumlahMinta);
                    }
                });
        inputMinta = view.findViewById(R.id.inputMinta);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            listener = (MintaDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    public interface MintaDialogListener{
        void mintaMakan(String jumlahMinta);
    }
}
