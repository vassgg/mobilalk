package com.example.flightticketbooking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FlightItemAdapter extends RecyclerView.Adapter<FlightItemAdapter.ViewHolder> {
    private static final String LOG_TAG = FlightItemAdapter.class.getName();
    private ArrayList<FlightItem> mFlightItems;
    private ArrayList<FlightItem> mFlightItemsAll;
    private Context mContext;
    FlightItemAdapter(Context context, ArrayList<FlightItem> itemsData){
        this.mFlightItems = itemsData;
        this.mFlightItemsAll = itemsData;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.flight_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlightItemAdapter.ViewHolder holder, int position) {
        FlightItem currentItem = mFlightItems.get(position);

        holder.bindTo(currentItem);

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide);
        holder.itemView.startAnimation(animation);
    }

    public void filter(String from, String to, String departDate, String returnDate, boolean isRetour){
        ArrayList<FlightItem> filtered = new ArrayList<>();
        for(FlightItem item: mFlightItemsAll){
            if (item.getFrom().equals(from) && item.getTo().equals(to) && item.getDate().startsWith(departDate) && item.getBookedBy().isEmpty()) {
                filtered.add(item);
            } else if (isRetour && item.getFrom().equals(to) && item.getTo().equals(from) && item.getDate().startsWith(returnDate) && item.getBookedBy().isEmpty()){
                filtered.add(item);
            }
        }
        mFlightItems = filtered;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFlightItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
    private TextView mDate;
    private TextView mFrom;
    private TextView mTo;
    private TextView mPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.flightItemDate);
            mFrom = itemView.findViewById(R.id.flightItemFrom);
            mTo = itemView.findViewById(R.id.flightItemTo);
            mPrice = itemView.findViewById(R.id.flightItemPrice);
        }

        public void bindTo(FlightItem currentItem) {
            mDate.setText(currentItem.getDate());
            mFrom.setText(currentItem.getFrom());
            mTo.setText(currentItem.getTo());
            mPrice.setText(currentItem.getPrice());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore mStore = FirebaseFirestore.getInstance();
            CollectionReference mUsers = mStore.collection("Users");

            if (mContext.getClass().getName().equals(TicketListActivity.class.getName())) {
                itemView.findViewById(R.id.flightItemBtn).setOnClickListener(v -> {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_button);
                    v.startAnimation(animation);

                    ((TicketListActivity)mContext).reserveItem(currentItem);
                });
            } else if (mContext.getClass().getName().equals(BookingsActivity.class.getName())) {
                Button flightItemBtn = itemView.findViewById(R.id.flightItemBtn);
                flightItemBtn.setText(R.string.revoke);

                flightItemBtn.setOnClickListener(v -> {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_button);
                    v.startAnimation(animation);

                    ((BookingsActivity)mContext).revokeItem(currentItem);
                });
            }

            itemView.findViewById(R.id.cardDetails).setOnClickListener(view -> {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_button);
                view.startAnimation(animation);

                if (user != null) {
                    mUsers.whereEqualTo("uid", user.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        User queriedUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        String firstName = queriedUser.getFirstName();
                        String lastName = queriedUser.getLastName();

                        Intent intent = new Intent(mContext, FlightDetailsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("secretKey", 55);
                        bundle.putString("date", mDate.getText().toString());
                        bundle.putString("from", mFrom.getText().toString());
                        bundle.putString("to", mTo.getText().toString());
                        bundle.putString("price", mPrice.getText().toString());
                        bundle.putString("firstName", firstName);
                        bundle.putString("lastName", lastName);
                        bundle.putString("fromWhere", mContext.getClass().getName());
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    });
                }
            });
        }
    }
}
