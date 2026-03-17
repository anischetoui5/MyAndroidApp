package com.budgetpal.budgetppaal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetpal.budgetppaal.Adapters.CategoryAdapter;
import com.budgetpal.budgetppaal.Controllers.CategoryController;
import com.budgetpal.budgetppaal.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerViewCategories;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etCategoryName, etCategoryBudget;
    private Button btnAddCategory;

    private CategoryAdapter categoryAdapter;
    private CategoryController categoryController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        initializeViews(view);

        // Adapter
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Controller
        categoryController = new CategoryController(requireContext());
        categoryController.setListener(new CategoryController.CategoryListener() {
            @Override
            public void onCategoriesLoaded(List<com.budgetpal.budgetppaal.Category> categories) {
                handleCategoriesLoaded(convert(categories));
            }

            @Override
            public void onCategoryAdded(com.budgetpal.budgetppaal.Category c) {
                Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
                loadCategories();
            }

            @Override
            public void onCategoryUpdated(com.budgetpal.budgetppaal.Category c) {
                Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
                loadCategories();
            }

            @Override
            public void onCategoryDeleted(String id) {
                Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                loadCategories();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Adapter actions (EDIT / DELETE)
        categoryAdapter.setOnCategoryActionListener(new CategoryAdapter.OnCategoryActionListener() {

            @Override
            public void onCategoryClick(int position) {}

            @Override
            public void onCategoryLongClick(int position) {}

            @Override
            public void onEditClick(int position) {
                Category c = categoryAdapter.getCategory(position);
                if (c != null) showEditDialog(c);
            }

            @Override
            public void onDeleteClick(int position) {
                Category c = categoryAdapter.getCategory(position);
                if (c != null) showDeleteDialog(c);
            }
        });

        btnAddCategory.setOnClickListener(v -> addCategory());

        loadCategories();
        return view;
    }

    // -------------------- HELPERS --------------------

    private void initializeViews(View v) {
        recyclerViewCategories = v.findViewById(R.id.recyclerViewCategories);
        progressBar = v.findViewById(R.id.progressBar);
        tvEmptyState = v.findViewById(R.id.tvEmptyState);
        etCategoryName = v.findViewById(R.id.etCategoryName);
        etCategoryBudget = v.findViewById(R.id.etCategoryBudget);
        btnAddCategory = v.findViewById(R.id.btnAddCategory);
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoryController.loadCategories();
    }

    private void addCategory() {
        String name = etCategoryName.getText().toString().trim();
        String budgetStr = etCategoryBudget.getText().toString().trim();

        if (name.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);
            categoryController.addCategory(name, budget);
            etCategoryName.setText("");
            etCategoryBudget.setText("");
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCategoriesLoaded(List<Category> categories) {
        progressBar.setVisibility(View.GONE);

        if (categories == null || categories.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewCategories.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewCategories.setVisibility(View.VISIBLE);
            categoryAdapter.updateCategories(categories);
        }
    }

    // -------------------- EDIT --------------------

    private void showEditDialog(Category category) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_category, null);

        EditText etName = view.findViewById(R.id.etEditCategoryName);
        EditText etBudget = view.findViewById(R.id.etEditCategoryBudget);
        Button btnSave = view.findViewById(R.id.btnSaveCategory);

        etName.setText(category.getName());
        etBudget.setText(String.valueOf(category.getBudgetLimit()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBudgetStr = etBudget.getText().toString().trim();

            if (newName.isEmpty() || newBudgetStr.isEmpty()) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newBudget = Double.parseDouble(newBudgetStr);
                progressBar.setVisibility(View.VISIBLE);
                categoryController.updateCategory(
                        String.valueOf(category.getCategoryId()),
                        newName,
                        newBudget
                );
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid budget", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // -------------------- DELETE --------------------

    private void showDeleteDialog(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Delete \"" + category.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    categoryController.deleteCategory(
                            String.valueOf(category.getCategoryId())
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // -------------------- CONVERTER --------------------

    private List<Category> convert(List<com.budgetpal.budgetppaal.Category> oldList) {
        List<Category> list = new ArrayList<>();
        for (com.budgetpal.budgetppaal.Category c : oldList) {
            list.add(new Category(
                    c.getCategoryId(),
                    c.getUserId(),
                    c.getName(),
                    "",
                    "#FF5722",
                    c.getBudget(),
                    c.getSpentAmount(),
                    false
            ));
        }
        return list;
    }
}
