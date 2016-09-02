package com.seuqarius.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

import com.seuqarius.sample.domain.User;
import com.seuqarius.squarewheels.storage.sharedpreference.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User user1 = new User();
        user1.setUsername("xdsbzd01");
        user1.setPassword("jfdfd");
        user1.setAge(17);
        TextUtils
        User user2 = new User();
        user2.setUsername("xdsbzd02");
        user2.setPassword("jfdfd*");
        user2.setAge(20);

        User user = SharedPreferenceManager.getInStance().getModel(getBaseContext(), User.class);
        user.setUsername("xdsbzd");
        user.setPassword("jfdfd");
        user.setAge(14);
        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(user2);
        user.setFriend(users);
//        Log.wtf(TAG, "onCreate: ", );
//        Log.d("dsf", user.toString());
    }
}
