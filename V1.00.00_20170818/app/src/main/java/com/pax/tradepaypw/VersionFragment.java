package com.pax.tradepaypw;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pax.jemv.demo.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class VersionFragment extends Fragment {


    public VersionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_version, container, false);
        return view;
    }

}
