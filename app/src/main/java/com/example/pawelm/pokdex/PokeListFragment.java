package com.example.pawelm.pokdex;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.example.pawelm.pokdex.PokemonDetailsActivity.PREFERENCES;


/**
 * A simple {@link Fragment} subclass.
 */
public class PokeListFragment extends Fragment {

    final String[] url = {"http://pokeapi.co/api"};
    final String[] newUrl = {"http://pokeapi.co/api/v2/pokemon/?limit=15"};
    View v;


    public PokeListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_poke_list_fragment, container, false);

        v = rootView;
        if(getActivity() instanceof SearchActivity)
        {
            ScrollView scroll = rootView.findViewById(R.id.scrollView);
            scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    ScrollView scrollView = (ScrollView) v;
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollY));

                    if (diff < 10)
                        loadData(rootView);
                }
            });
        }
        return rootView;
    }

    private void loadData(final View v)
    {
        if(getActivity() instanceof FavouritesActivity)
        {
            SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES,MODE_PRIVATE);
            Map<String,Integer> pokemons = (Map<String,Integer>) preferences.getAll();
            GridLayout gl = getActivity().findViewById(R.id.grid_layout);
            gl.removeAllViews();

            for(Map.Entry<String,Integer> pokemon : pokemons.entrySet())
                addButton(pokemon.getValue(),pokemon.getKey(),v);
        }
        else {
            if(isOnline()) {
                if(url != null) {
                    if (newUrl[0].equals(url[0]))
                        return;
                    url[0] = newUrl[0];
                    Ion.with(this)
                            .load(url[0])
                            .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null && result.get("results") != null) {
                                JsonArray results = result.getAsJsonArray("results");
                                for (int i = 0; i < 15; i++) {
                                    JsonObject pokemon = results.get(i).getAsJsonObject();
                                    String url = pokemon.get("url").getAsString();
                                    String prefix = "https://pokeapi.co/api/v2/pokemon/";
                                    String pokeId = url.substring(url.indexOf(prefix) + prefix.length());
                                    pokeId = pokeId.replaceAll("/", "");
                                    int pokemonId = Integer.parseInt(pokeId);
                                    String name = pokemon.get("name").getAsString();

                                    addButton(pokemonId, name, v);
                                }
                                newUrl[0] = result.get("next").getAsString();
                            }
                        }
                    });
                }
            }
            else
            {
                Intent intent = new Intent(getActivity(),NoInternetConnectionActivity.class);
                startActivity(intent);
            }
        }
    }

    private void displayImage(ImageView im, View v, int id) {
        Button but = v.findViewById(id);
        Drawable img = im.getDrawable();
        but.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
    }

    private void addButton(final int pokemonId, String name, final View v){
        final Button but = new Button(getActivity());
        final ImageView im = new ImageView(getActivity());
        int imId = View.generateViewId();
        im.setId(imId);
        final int id = View.generateViewId();
        but.setId(id);
        but.setText(name);

        Typeface tf = ResourcesCompat.getFont(getActivity(),R.font.pokemon_solid);
        but.setTypeface(tf);

        but.setPadding(8,8,8,8);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f);
        param.width = 0;
        but.setLayoutParams(param);

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    Button b = (Button) v;
                    Intent intent = new Intent(getActivity(), PokemonDetailsActivity.class);
                    intent.putExtra("name", b.getText().toString());
                    intent.putExtra("id", pokemonId);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(getActivity(),NoInternetConnectionActivity.class);
                    startActivity(intent);
                }

            }
        });
        GridLayout gl = v.findViewById(R.id.grid_layout);
        gl.addView(but);

        String Url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"+pokemonId+".png";
        Picasso.get().load(Url).resize(150,150).into(im, new Callback() {
            @Override
            public void onSuccess() {
                displayImage(im,v,id);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private boolean isOnline() {
        return ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null &&
                ((ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(v);
    }
}
