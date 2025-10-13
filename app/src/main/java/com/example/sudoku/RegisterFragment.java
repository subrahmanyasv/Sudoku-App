package com.example.sudoku;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextView loginLink = view.findViewById(R.id.show_login);
        loginLink.setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new LoginFragment());
        });

        return view;
    }
}
