package com.example.travelblog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.com.travelblog.repository.BlogRepository;
import com.com.travelblog.repository.DataFromNetworkCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.travelblog.adapter.MainAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private BlogRepository repository;

    private static final int SORT_TITLE = 0;
    private static final int SORT_DATE = 1;

    private int currentSort = SORT_DATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new BlogRepository(getApplicationContext());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sort) {
                    onSortClicked();
                }
                return false;
        });

        adapter = new MainAdapter(blog ->
                BlogDetailsActivity.startBlogDetailsActivity(this, blog));


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromNetwork();
            }
        });

        MenuItem searchItem = toolbar.getMenu().findItem(R.id.search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });


        loadDataFromDatabase();
        loadDataFromNetwork();
    }

    private void loadDataFromDatabase() {
        repository.loadDataFromDatabase(blogList -> runOnUiThread(() -> {
            adapter.setData(blogList);
            sortData();
        }));
        Log.d("MainActivity", "loadDataFromDatabase() returned: ");
    }

    private void loadDataFromNetwork() {
        refreshLayout.setRefreshing(true);

        repository.loadDataFromNetwork(new DataFromNetworkCallback() {
            @Override
            public void onSuccess(List<Blog> blogList) {
                runOnUiThread(() -> {
                    adapter.setData(blogList);
                    sortData();
                    refreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    showErrorSnackbar();
                });
            }
        });
        Log.d("MainActivity", "loadDataFromNetwork() returned: ");
    }


    private void showErrorSnackbar() {
        View rootView = findViewById(android.R.id.content);
        final Snackbar snackbar = Snackbar.make(rootView,
                "Error during loading blog articles", Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_orange_light));
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromNetwork();
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void onSortClicked() {
        String[] items = {"Title", "Date"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sort order")
                .setSingleChoiceItems(items, currentSort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        currentSort = which;
                        sortData();
                    }
                }).show();
    }

    private void sortData() {
        if (currentSort == SORT_TITLE) {
            adapter.sortByTitle();
        } else if (currentSort == SORT_DATE) {
            adapter.sortByDate();
        }
    }


}
