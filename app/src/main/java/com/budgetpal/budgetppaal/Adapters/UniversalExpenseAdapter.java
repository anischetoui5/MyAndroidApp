package com.budgetpal.budgetppaal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UniversalExpenseAdapter extends RecyclerView.Adapter<UniversalExpenseAdapter.ViewHolder> {

    private List<Object> expenseList; // Can hold both types
    private DecimalFormat format = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // Constants for expense types
    private static final int TYPE_GROUP_EXPENSE = 1;
    private static final int TYPE_PERSONAL_EXPENSE = 2;

    public UniversalExpenseAdapter() {
        this.expenseList = new ArrayList<>();
    }

    // Update with personal expenses (models.Expense)
    public void updatePersonalExpenses(List<com.budgetpal.budgetppaal.models.Expense> personalExpenses) {
        expenseList.clear();
        if (personalExpenses != null) {
            expenseList.addAll(personalExpenses);
        }
        notifyDataSetChanged();
    }

    // Update with group expenses (GroupExpense)
    public void updateGroupExpenses(List<com.budgetpal.budgetppaal.models.GroupExpense> groupExpenses) {
        expenseList.clear();
        if (groupExpenses != null) {
            expenseList.addAll(groupExpenses);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = expenseList.get(position);
        if (item instanceof com.budgetpal.budgetppaal.models.GroupExpense) {
            return TYPE_GROUP_EXPENSE;
        } else if (item instanceof com.budgetpal.budgetppaal.models.Expense) {
            return TYPE_PERSONAL_EXPENSE;
        }
        return TYPE_PERSONAL_EXPENSE; // Default
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use different layouts if you want, but for now use the same
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_universal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == TYPE_GROUP_EXPENSE) {
            com.budgetpal.budgetppaal.models.GroupExpense expense =
                    (com.budgetpal.budgetppaal.models.GroupExpense) expenseList.get(position);
            bindGroupExpense(holder, expense);
        } else {
            com.budgetpal.budgetppaal.models.Expense expense =
                    (com.budgetpal.budgetppaal.models.Expense) expenseList.get(position);
            bindPersonalExpense(holder, expense);
        }
    }

    private void bindGroupExpense(ViewHolder holder, com.budgetpal.budgetppaal.models.GroupExpense expense) {
        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText("TND" + format.format(expense.getAmount()));
        holder.tvCategory.setText(expense.getCategory());
        holder.tvDetails.setText("Paid by: " + expense.getPayerName() + " • " + expense.getDate());

        // Status
        if (expense.isSettled()) {
            holder.tvStatus.setText("Settled");
            holder.tvStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_orange_dark));
        }
        holder.tvStatus.setVisibility(View.VISIBLE);
    }

    private void bindPersonalExpense(ViewHolder holder, com.budgetpal.budgetppaal.models.Expense expense) {
        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText("TND" + format.format(expense.getAmount()));
        holder.tvCategory.setText(expense.getCategoryName());

        // Format date
        String dateStr = "No date";
        if (expense.getDate() != null) {
            dateStr = dateFormat.format(expense.getDate());
        }
        holder.tvDetails.setText(dateStr);

        // Hide status for personal expenses
        holder.tvStatus.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvCategory, tvDetails, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDetails = itemView.findViewById(R.id.tvExpenseDetails);
            tvStatus = itemView.findViewById(R.id.tvExpenseStatus);
        }
    }
    // Add this method to your UniversalExpenseAdapter class
    public void addExpense(Object expense) {
        if (expenseList == null) {
            expenseList = new ArrayList<>();
        }
        expenseList.add(expense);
        notifyItemInserted(expenseList.size() - 1);
    }

    // Also add a getter for testing
    public List<Object> getExpenseList() {
        return expenseList;
    }
}