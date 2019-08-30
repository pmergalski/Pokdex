package com.example.pawelm.pokdex;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.Console;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean noConnection = !isOnline();
        if(noConnection) {
            goToNoInternetConnectionActivity();
        }
        else {
            setContentView(R.layout.activity_search);

            Button sbut = findViewById(R.id.search_button);
            sbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Search();
                }
            });

            Button fbut = findViewById(R.id.favourites_button);
            fbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favouritesClick();
                }
            });
        }
    }

    private void favouritesClick() {
        Intent intent = new Intent(this,FavouritesActivity.class);
        startActivity(intent);
    }

    private void Search() {
        if (isOnline()) {
            EditText input = findViewById(R.id.editText);
            Intent intent = new Intent(this, PokemonDetailsActivity.class);
            intent.putExtra("name", input.getText().toString());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, NoInternetConnectionActivity.class);
            startActivity(intent);
        }
    }
    private boolean isOnline() {
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null &&
                ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected();
    }

    private void goToNoInternetConnectionActivity(){
        Intent intent = new Intent(this, NoInternetConnectionActivity.class);
        startActivity(intent);
    }

}
