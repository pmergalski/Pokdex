package com.example.pawelm.pokdex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PokemonDetailsActivity extends AppCompatActivity {

    public static final String PREFERENCES = "favourites";
    private ImageButton add;
    private SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_details);


        add = findViewById(R.id.add_to_favourites);
        preferences = getSharedPreferences(PREFERENCES,MODE_PRIVATE);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        final String name = extras.getString("name", "-");
        final int[] id = {extras.getInt("id", -1)};
        if(id[0] !=-1)
            loadImage(id[0]);

        Ion.with(this)
                .load("http://pokeapi.co/api/v2/pokemon/" + name)
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if(result != null)
                    {
                    if (result.get("forms") != null) {
                        {
                            TextView pokemonName = findViewById(R.id.pokemon_name);
                            pokemonName.setText(name);
                            add.setVisibility(View.VISIBLE);
                            if(preferences.contains(name))
                                add.setImageResource(R.drawable.favourite);
                            else
                                add.setImageResource(R.drawable.not_favourite);

                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toggleFavourite(id[0], name);
                                }
                            });

                            if (id[0] == -1) {
                                JsonArray forms = result.get("forms").getAsJsonArray();
                                JsonObject form = forms.get(0).getAsJsonObject();
                                String url = form.get("url").getAsString();
                                String prefix = "https://pokeapi.co/api/v2/pokemon-form/";
                                String pokeId = url.substring(url.indexOf(prefix) + prefix.length());
                                pokeId = pokeId.replaceAll("/", "");
                                final int pokemonId = Integer.parseInt(pokeId);
                                id[0] = pokemonId;
                                loadImage(id[0]);
                            }

                            JsonArray types = result.getAsJsonArray("types");

                            StringBuilder pokemonTypes = new StringBuilder();
                            for (int i = 0; i < types.size(); i++) {
                                String type = types.get(i).getAsJsonObject().get("type").getAsJsonObject().get("name").getAsString();
                                pokemonTypes.append(type).append(" ");
                            }
                            TextView t = findViewById(R.id.types);
                            t.setText(pokemonTypes.toString());
                        }
                    }
                }
                else {
                    Picasso.get()
                            .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/0.png")
                            .into((ImageView) findViewById(R.id.pokemon_image));
                    TextView text = findViewById(R.id.pokemon_name);
                    text.setText("Pokemon named " + name + " not found");
                    add.setVisibility(View.GONE);
                }

            }
        });

        Ion.with(this).load("https://pokeapi.co/api/v2/pokemon-species/" + name)
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if(result != null && result.get("evolution_chain") != null)
                {
                    String url = result.get("evolution_chain").getAsJsonObject().get("url").getAsString();
                    Ion.with(getApplicationContext()).load(url)
                            .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(result != null)
                            {
                                JsonObject chain = result.get("chain").getAsJsonObject();
                                String first = chain.get("species").getAsJsonObject().get("url").getAsString();

                                String prefix = "https://pokeapi.co/api/v2/pokemon-species/";
                                String pokeId = first.substring(first.indexOf(prefix) + prefix.length());
                                pokeId = pokeId.replaceAll("/", "");
                                int id = (Integer.parseInt(pokeId));
                                Picasso.get()
                                        .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + "png")
                                        .into((ImageView)findViewById(R.id.first_evolution));
                                JsonArray array = chain.get("evolves_to").getAsJsonArray();
                                JsonObject next = array.get(0).getAsJsonObject();

                                String nextSpecies = next.get("species").getAsJsonObject().get("url").getAsString();
                                prefix = "https://pokeapi.co/api/v2/pokemon-species/";
                                pokeId = nextSpecies.substring(nextSpecies.indexOf(prefix) + prefix.length());
                                pokeId = pokeId.replaceAll("/", "");
                                int nextId = (Integer.parseInt(pokeId));
                                Picasso.get()
                                        .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + nextId + "png")
                                        .into((ImageView)findViewById(R.id.second_evolution));
                                array = chain.get("evolves_to").getAsJsonArray();
                                next = array.get(0).getAsJsonObject();

                                nextSpecies = next.get("species").getAsJsonObject().get("url").getAsString();
                                prefix = "https://pokeapi.co/api/v2/pokemon-species/";
                                pokeId = nextSpecies.substring(nextSpecies.indexOf(prefix) + prefix.length());
                                pokeId = pokeId.replaceAll("/", "");
                                int lastId = (Integer.parseInt(pokeId));
                                Picasso.get()
                                        .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + lastId + "png")
                                        .into((ImageView)findViewById(R.id.third_evolution));
                            }
                        }
                    });
                }
            }
        });
    }

    private void toggleFavourite(int id, String name){
        if(preferences.contains(name)) {
            preferences.edit().remove(name).apply();
            add.setImageResource(R.drawable.not_favourite);
        }
        else
        {
            preferences.edit().putInt(name,id).apply();
            add.setImageResource(R.drawable.favourite);
        }
    }

    private void loadImage(final int pokemonId){
        Picasso.get()
                .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other-sprites/official-artwork/" + pokemonId + ".png")
                .into((ImageView) findViewById(R.id.pokemon_image), new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get()
                                .load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + pokemonId + ".png")
                                .into((ImageView) findViewById(R.id.pokemon_image));
                    }
                });
    }

}
