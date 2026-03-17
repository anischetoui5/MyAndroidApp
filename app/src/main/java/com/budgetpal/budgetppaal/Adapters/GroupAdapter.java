package com.budgetpal.budgetppaal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.R;
import com.budgetpal.budgetppaal.models.Group;

import java.text.DecimalFormat;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private Context context;
    private List<Group> groupList;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public interface OnGroupClickListener {
        void onGroupClick(int position);
        void onInviteClick(int position);
        void onViewDetailsClick(int position);
    }

    private OnGroupClickListener listener;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groupList.get(position);

        holder.tvGroupName.setText(group.getName());
        holder.tvTotalBudget.setText(String.format("Total Budget: TND%s",
                decimalFormat.format(group.getTotalBudget())));
        holder.tvMemberCount.setText(String.format("Members: %d",
                group.getMemberCount()));
        holder.tvPerPerson.setText(String.format("Per Person: TND%s",
                decimalFormat.format(group.getPerPersonShare())));

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(position);
        });

        holder.btnInvite.setOnClickListener(v -> {
            if (listener != null) listener.onInviteClick(position);
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailsClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void updateGroups(List<Group> newGroups) {
        groupList.clear();
        groupList.addAll(newGroups);
        notifyDataSetChanged();
    }

    public void addGroup(Group group) {
        groupList.add(group);
        notifyItemInserted(groupList.size() - 1);
    }

    public void removeGroup(int position) {
        if (position >= 0 && position < groupList.size()) {
            groupList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvGroupName, tvTotalBudget, tvMemberCount, tvPerPerson;
        Button btnInvite, btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewGroup);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvTotalBudget = itemView.findViewById(R.id.tvTotalBudget);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvPerPerson = itemView.findViewById(R.id.tvPerPerson);
            btnInvite = itemView.findViewById(R.id.btnInvite);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
