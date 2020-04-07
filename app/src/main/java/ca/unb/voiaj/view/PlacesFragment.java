package ca.unb.voiaj.view;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import ca.unb.voiaj.R;
import ca.unb.voiaj.databinding.FragmentPlacesBinding;

import ca.unb.voiaj.R;
import ca.unb.voiaj.databinding.FragmentPlacesBinding;
import ca.unb.voiaj.view.adapters.RecyclerAdapter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import ca.unb.voiaj.viewmodel.PlaceViewModel;

public class PlacesFragment extends Fragment {

    private String fragmentName = "Places";
    private RecyclerAdapter adapter;
    private PlaceViewModel placeViewModel;
    private FragmentPlacesBinding placeFragmentBinding;

    public PlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(fragmentName);

        placeViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);
        placeFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_places, container, false);
        placeFragmentBinding.setPlaceViewModel(placeViewModel);
        View view = placeFragmentBinding.getRoot();

        setHasOptionsMenu(true);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new RecyclerAdapter(getContext(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_places, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
}
