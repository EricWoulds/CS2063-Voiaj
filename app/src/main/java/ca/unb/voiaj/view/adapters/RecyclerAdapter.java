package ca.unb.voiaj.view.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ca.unb.voiaj.R;

import ca.unb.voiaj.service.Place;
import ca.unb.voiaj.service.JsonDBUtils;
import ca.unb.voiaj.viewmodel.PlaceViewModel;

import java.util.ArrayList;
import java.text.SimpleDateFormat;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ArrayList<Place> places;
    private ArrayList<Place> placesCopy;
    private Context context;
    private ItemClickListener mClickListener;
    private PlaceViewModel viewModel;
    private Fragment callingFragment;

    public RecyclerAdapter(Context context, Fragment fragment) {
        this.context = context;
        this.callingFragment = fragment;
        viewModel = new ViewModelProvider(fragment).get(PlaceViewModel.class);
        JsonDBUtils jsonDBUtils = new JsonDBUtils(context);
        places = jsonDBUtils.getPlaces();
        placesCopy = new ArrayList<>();
        placesCopy.addAll(places);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_place_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        Place place = places.get(position);

        viewHolder.placeName.setText(place.getName());

        viewHolder.placeAddress.setText(place.getFormattedAddress());
        if(place.isVisited()){
            viewHolder.placeVisited.setText("Visited");
        }
        else{
            viewHolder.placeVisited.setText("");
        }
        viewHolder.placeName.setTag(place.getId());

    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView placeName;
        TextView placeAddress;
        TextView placeVisited;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_item_name);
            placeAddress = itemView.findViewById(R.id.place_item_address);
            placeVisited = itemView.findViewById(R.id.place_item_visited);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
        @Override
        public void onClick(View view){
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return places.size();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public void filter(String searchText) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        places.clear();
        if(searchText.isEmpty()){
            places.addAll(placesCopy);
        }else{
            searchText = searchText.toLowerCase();
            for(int i = 0; i < placesCopy.size(); i++){
                Place place = placesCopy.get(i);
                if(place.getName().toLowerCase().contains(searchText)
                        || place.getFormattedAddress().toString().toLowerCase().contains(searchText)){
                    places.add(placesCopy.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }
}
