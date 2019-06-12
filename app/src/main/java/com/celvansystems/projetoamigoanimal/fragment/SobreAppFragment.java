package com.celvansystems.projetoamigoanimal.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.celvansystems.projetoamigoanimal.R;

public class SobreAppFragment extends Fragment {

    public SobreAppFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sobre_app, container, false);

        inializaComponentes();

        return view;
    }

    private void inializaComponentes() {

    }
}