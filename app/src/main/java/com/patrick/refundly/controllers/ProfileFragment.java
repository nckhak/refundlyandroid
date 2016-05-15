package com.patrick.refundly.controllers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.patrick.refundly.R;
import com.patrick.refundly.models.User;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    TextView mName, mEmail, mPhone;
    Button applyBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mName=(TextView)v.findViewById(R.id.profileName);
        mEmail=(TextView)v.findViewById(R.id.profileEmail);
        mPhone=(TextView)v.findViewById(R.id.profilePhonenumber);
        applyBtn=(Button)v.findViewById(R.id.applyBtn);
        if(!Controller.controller.getUser().getRole().equals("C")){
            applyBtn.setOnClickListener(this);
        }else{
            applyBtn.setEnabled(false);
            applyBtn.setVisibility(View.INVISIBLE);
        }


        if(Controller.controller.getUser() != null){
            updateProfileFields(Controller.controller.getUser());
        }

        return v;
    }

    private void updateProfileFields(User user){
        System.out.println(user.getUserName());
        System.out.println(user.getEmail());
        System.out.println(user.getPhoneNumber());
        mName.setText(user.getUserName());
        mEmail.setText(user.getEmail());
        mPhone.setText(user.getPhoneNumber());
    }

    @Override
    public void onClick(View v) {
        if(v==applyBtn){
            Intent i = new Intent(getActivity(), CollectorApplication.class);
            startActivity(i);
        }
    }
}
