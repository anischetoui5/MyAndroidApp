package com.budgetpal.budgetppaal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.R;
import com.budgetpal.budgetppaal.models.Category;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categoryList;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    // ✅ new listener supports click + long click + edit + delete
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onCategoryClick(int position);
        void onCategoryLongClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public CategoryAdapter() {
        this.categoryList = new ArrayList<>();
    }

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = (categoryList != null) ? categoryList : new ArrayList<>();
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        categoryList.clear();
        if (newCategories != null) categoryList.addAll(newCategories);
        notifyDataSetChanged();
    }

    public void addCategory(Category category) {
        if (category == null) return;
        categoryList.add(0, category);
        notifyItemInserted(0);
    }

    public void clearCategories() {
        categoryList.clear();
        notifyDataSetChanged();
    }

    public Category getCategory(int position) {
        if (position >= 0 && position < categoryList.size()) {
            return categoryList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());

        holder.tvCategoryBudget.setText(String.format("Budget: TND%s",
                decimalFormat.format(category.getBudgetLimit())));
        holder.tvSpentAmount.setText(String.format("Spent: TND%s",
                decimalFormat.format(category.getCurrentSpent())));
        holder.tvRemainingBudget.setText(String.format("Remaining: TND%s",
                decimalFormat.format(category.getRemaining())));

        // Progress bar
        int progress = (int) category.getSpentPercentage();
        holder.progressBar.setProgress(Math.min(progress, 100));

        if (progress < 70) {
            holder.progressBar.setProgressTintList(
                    holder.itemView.getContext().getResources()
                            .getColorStateList(android.R.color.holo_green_dark)
            );
        } else if (progress < 95) {
            holder.progressBar.setProgressTintList(
                    holder.itemView.getContext().getResources()
                            .getColorStateList(android.R.color.holo_orange_dark)
            );
        } else {
            holder.progressBar.setProgressTintList(
                    holder.itemView.getContext().getResources()
                            .getColorStateList(android.R.color.holo_red_dark)
            );
        }

        // ✅ Card click
        holder.cardView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onCategoryClick(pos);
            }
        });

        // ✅ Card long click
        holder.cardView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onCategoryLongClick(pos);
            }
            return true;
        });

        // ✅ Edit button
        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onEditClick(pos);
            }
        });

        // ✅ Delete button
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategoryName, tvCategoryBudget, tvSpentAmount, tvRemainingBudget;
        ProgressBar progressBar;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardViewCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryBudget = itemView.findViewById(R.id.tvCategoryBudget);
            tvSpentAmount = itemView.findViewById(R.id.tvSpentAmount);
            tvRemainingBudget = itemView.findViewById(R.id.tvRemainingBudget);
            progressBar = itemView.findViewById(R.id.progressBarCategory);

            // ✅ these must exist in item_category.xml
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
