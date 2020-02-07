package com.romellfudi.ussd.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.romellfudi.permission.PermissionService;
import com.romellfudi.ussd.R;
import com.romellfudi.ussdlibrary.OverlayShowingService;
import com.romellfudi.ussdlibrary.SplashLoadingService;
import com.romellfudi.ussdlibrary.USSDApi;
import com.romellfudi.ussdlibrary.USSDController;
import com.romellfudi.ussdlibrary.USSDController.CallbackInvoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Use Case for Test Windows
 *
 * @author Romell Domínguez
 * @version 1.1.b 27/09/2018
 * @since 1.0.a
 */
public class MainFragment extends Fragment {

    private TextView result;
    private EditText phone;
    private Button btn1, btn2, btn3, btn4;
    private HashMap<String, HashSet<String>> map;
    private USSDApi ussdApi;
    private MainActivity menuActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
        ussdApi = USSDController.getInstance(getActivity());
        menuActivity = (MainActivity) getActivity();
        new PermissionService(getActivity()).request(callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_op1, container, false);
        result = view.findViewById(R.id.result);
        phone = view.findViewById(R.id.phone);
        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);
        btn3 = view.findViewById(R.id.btn3);
        btn4 = view.findViewById(R.id.btn4);
        setHasOptionsMenu(false);

        btn1.setOnClickListener(v -> {
            String phoneNumber = phone.getText().toString().trim();
            ussdApi = USSDController.getInstance(getActivity());
            result.setText("");
            ussdApi.callUSSDInvoke(phoneNumber, map, new CallbackInvoke() {
                @Override
                public void responseInvoke(String message) {
                    Log.d("APP", message);
                    result.append("\n-\n" + message);
                    // first option list - select option 1
                    ussdApi.send("1", message15 -> {
                        Log.d("APP", message15);
                        result.append("\n-\n" + message15);
                        // second option list - select option 1
                        ussdApi.send("1", message14 -> {
                            Log.d("APP", message14);
                            result.append("\n-\n" + message14);
                        });
                    });
//                        ussdApi.cancel();
                }

                @Override
                public void over(String message) {
                    Log.d("APP", message);
                    result.append("\n-\n" + message);
                }
            });
        });

        btn2.setOnClickListener(v -> {
            if (USSDController.verifyOverLay(getActivity())) {
                final Intent svc = new Intent(getActivity(), OverlayShowingService.class);
                svc.putExtra(OverlayShowingService.EXTRA, "PROCESANDO");
                getActivity().startService(svc);
                Log.d("APP", "START OVERLAY DIALOG");
                String phoneNumber = phone.getText().toString().trim();
                ussdApi = USSDController.getInstance(getActivity());
                result.setText("");
                ussdApi.callUSSDOverlayInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
                    @Override
                    public void responseInvoke(String message) {
                        Log.d("APP", message);
                        result.append("\n-\n" + message);
                        // first option list - select option 1
                        ussdApi.send("1", message13 -> {
                            Log.d("APP", message13);
                            result.append("\n-\n" + message13);
                            // second option list - select option 1
                            ussdApi.send("1", message131 -> {
                                Log.d("APP", message131);
                                result.append("\n-\n" + message131);
                                getActivity().stopService(svc);
                                Log.d("APP", "STOP OVERLAY DIALOG");
                                Log.d("APP", "successful");
                            });
                        });
//                            ussdApi.cancel();
                    }

                    @Override
                    public void over(String message) {
                        Log.d("APP", message);
                        result.append("\n-\n" + message);
                        getActivity().stopService(svc);
                        Log.d("APP", "STOP OVERLAY DIALOG");
                    }
                });
            }
        });

        btn4.setOnClickListener(v -> {
            if (USSDController.verifyOverLay(getActivity())) {
                final Intent svc = new Intent(getActivity(), SplashLoadingService.class);
                getActivity().startService(svc);
                Log.d("APP", "START SPLASH DIALOG");
                String phoneNumber = phone.getText().toString().trim();
                result.setText("");
                ussdApi.callUSSDOverlayInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
                    @Override
                    public void responseInvoke(String message) {
                        Log.d("APP", message);
                        result.append("\n-\n" + message);
                        // first option list - select option 1
                        ussdApi.send("1", message12 -> {
                            Log.d("APP", message12);
                            result.append("\n-\n" + message12);
                            // second option list - select option 1
                            ussdApi.send("1", message1 -> {
                                Log.d("APP", message1);
                                result.append("\n-\n" + message1);
                                getActivity().stopService(svc);
                                Log.d("APP", "STOP SPLASH DIALOG");
                                Log.d("APP", "successful");
                            });
                        });
                        ussdApi.cancel();
                    }

                    @Override
                    public void over(String message) {
                        Log.d("APP", message);
                        result.append("\n-\n" + message);
                        getActivity().stopService(svc);
                        Log.d("APP", "STOP OVERLAY DIALOG");
                    }
                });
            }
        });

        btn3.setOnClickListener(v -> USSDController.verifyAccesibilityAccess(getActivity()));

        return view;
    }

    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onResponse(ArrayList<String> refusePermissions) {
            if (refusePermissions != null && refusePermissions.size()>0) {
                Toast.makeText(getContext(),
                        getString(R.string.refuse_permissions),
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }
}

