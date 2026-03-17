package com.budgetpal.budgetppaal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.R;
import com.budgetpal.budgetppaal.models.GroupExpense;

import java.text.DecimalFormat;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<GroupExpense> expenseList;
    private DecimalFormat format = new DecimalFormat("#,##0.00");

    public ExpenseAdapter(List<GroupExpense> expenseList) {
        this.expenseList = expenseList;
    }

    // NEW: Update method for personal expenses (models.Expense)
    public void updatePersonalExpenses(List<com.budgetpal.budgetppaal.models.Expense> personalExpenses) {
        // Convert models.Expense to GroupExpense if needed
        // Or create a separate list
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupExpense expense = expenseList.get(position);

        // Set expense details
        holder.tvExpenseDescription.setText(expense.getDescription());
        holder.tvExpenseAmount.setText("TND" + format.format(expense.getAmount()));
        holder.tvExpensePayer.setText("Paid by: " + expense.getPayerName());
        holder.tvExpenseCategory.setText(expense.getCategory());
        holder.tvExpenseDate.setText(expense.getDate());

        // Set status
        if (expense.isSettled()) {
            holder.tvExpenseStatus.setText("Settled");
            holder.tvExpenseStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvExpenseStatus.setText("Pending");
            holder.tvExpenseStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseDescription, tvExpenseAmount, tvExpensePayer;
        TextView tvExpenseCategory, tvExpenseDate, tvExpenseStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpensePayer = itemView.findViewById(R.id.tvExpensePayer);
            tvExpenseCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvExpenseStatus = itemView.findViewById(R.id.tvExpenseStatus);
        }
    }
}