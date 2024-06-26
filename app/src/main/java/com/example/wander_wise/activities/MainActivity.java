package com.example.wander_wise.activities;

import static com.example.wander_wise.entities.ConstantsCatalog.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wander_wise.R;
import com.example.wander_wise.components.TutorialOverlay;
import com.example.wander_wise.fragments.MapsFragment;
import com.example.wander_wise.objects.Utils;
import com.example.wander_wise.services.MenuClickListener;
import com.example.wander_wise.services.PreferencesManager;

import java.util.Objects;

public class MainActivity extends BaseActivity implements MenuClickListener {
    private FragmentManager fragmentManager;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();


        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MapsFragment(), "MapsFragment")
                .addToBackStack(null)
                .commit();
    }

    private boolean isTutorialSeen() {
        preferencesManager = PreferencesManager.getInstance(this);
        return preferencesManager.isTutorialSeen();
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean permissionsGranted() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMenuItemClick(Fragment selectedFragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String fragmentTag = selectedFragment.getClass().getSimpleName();
        Fragment existingFragment = fragmentManager.findFragmentByTag(fragmentTag);
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (!Utils.isNotNull(currentFragment) || !Objects.equals(currentFragment.getTag(), fragmentTag)) {
            if (Utils.isNotNull(existingFragment)) {
                transaction.replace(R.id.fragment_container, existingFragment, fragmentTag);
            } else {
                transaction.replace(R.id.fragment_container, selectedFragment, fragmentTag);
            }
            transaction.addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissionsGranted()) {
                if (!isTutorialSeen()) {
                    TutorialOverlay tutorialOverlay = new TutorialOverlay(this);
                    addContentView(tutorialOverlay, new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));
                }
            }
        }
    }

}