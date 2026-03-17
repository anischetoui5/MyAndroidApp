package com.budgetpal.budgetppaal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.R;
import com.budgetpal.budgetppaal.models.GroupMember;
import java.text.DecimalFormat;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<GroupMember> memberList;
    private DecimalFormat format = new DecimalFormat("#,##0.00");

    public MemberAdapter(List<GroupMember> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember member = memberList.get(position);

        // Set member name and email
        holder.tvMemberName.setText(member.getUserName());
        if (holder.tvMemberEmail != null) {
            holder.tvMemberEmail.setText(member.getEmail());
        }

        // Set amount spent
        holder.tvMemberSpent.setText("Spent: TND" + format.format(member.getTotalSpent()));

        // Set balance status
        if (member.isOwedMoney()) {
            holder.tvMemberBalance.setText("Owes: TND" + format.format(member.getAbsoluteBalance()));
            holder.tvMemberBalance.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
            holder.tvMemberBalance.setBackgroundResource(R.drawable.balance_background_green);
        } else if (member.owesMoney()) {
            holder.tvMemberBalance.setText("Owed: TND" + format.format(member.getAbsoluteBalance()));
            holder.tvMemberBalance.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_red_dark));
            holder.tvMemberBalance.setBackgroundResource(R.drawable.balance_background_red);
        } else {
            holder.tvMemberBalance.setText("Settled");
            holder.tvMemberBalance.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.darker_gray));
            holder.tvMemberBalance.setBackgroundResource(R.drawable.balance_background_gray);
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberEmail, tvMemberSpent, tvMemberBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvMemberSpent = itemView.findViewById(R.id.tvMemberSpent);
            tvMemberBalance = itemView.findViewById(R.id.tvMemberBalance);
        }
    }
}