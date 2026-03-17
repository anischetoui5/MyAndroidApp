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

public class PersonalExpenseAdapter extends RecyclerView.Adapter<PersonalExpenseAdapter.ViewHolder> {

    private List<com.budgetpal.budgetppaal.models.Expense> expenseList;
    private DecimalFormat format = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public PersonalExpenseAdapter() {
        this.expenseList = new ArrayList<>();
    }

    public PersonalExpenseAdapter(List<com.budgetpal.budgetppaal.models.Expense> expenseList) {
        this.expenseList = expenseList != null ? expenseList : new ArrayList<>();
    }

    // Update method for models.Expense
    public void updateExpenses(List<com.budgetpal.budgetppaal.models.Expense> newExpenses) {
        expenseList.clear();
        if (newExpenses != null) {
            expenseList.addAll(newExpenses);
        }
        notifyDataSetChanged();
    }

    // Add method for models.Expense
    public void addExpense(com.budgetpal.budgetppaal.models.Expense expense) {
        expenseList.add(expense);
        notifyItemInserted(expenseList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.budgetpal.budgetppaal.models.Expense expense = expenseList.get(position);

        // Set expense details
        holder.tvExpenseDescription.setText(expense.getDescription());
        holder.tvExpenseAmount.setText("TND" + format.format(expense.getAmount()));
        holder.tvExpenseCategory.setText(expense.getCategoryName());

        // Format date
        if (expense.getDate() != null) {
            holder.tvExpenseDate.setText(dateFormat.format(expense.getDate()));
        } else {
            holder.tvExpenseDate.setText("No date");
        }

        // Set category icon
        if (expense.getCategoryIcon() != null) {
            holder.tvCategoryIcon.setText(expense.getCategoryIcon());
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseDescription, tvExpenseAmount, tvExpenseCategory;
        TextView tvExpenseDate, tvCategoryIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
        }
    }
}