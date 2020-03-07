package com.com.travelblog.repository;

import android.content.Context;
import android.util.Log;

import com.example.travelblog.Blog;
import com.example.travelblog.BlogHttpClient;
import com.travel.database.AppDatabase;
import com.travel.database.BlogDAO;
import com.travel.database.DatabaseProvider;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BlogRepository {
    private BlogHttpClient httpClient;
    private AppDatabase database;
    private Executor executor;

    public BlogRepository(Context context) {
        httpClient = BlogHttpClient.INSTANCE;
        database = DatabaseProvider.getInstance(context.getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
    }

    public void loadDataFromDatabase(DataFromDatabaseCallback callback) {
        executor.execute(() -> callback.onSuccess(database.blogDao().getAll()));
    }

    public void loadDataFromNetwork(DataFromNetworkCallback callback) {
        executor.execute(() -> {
            List<Blog> blogList = httpClient.loadBlogArticles();
            if (blogList == null) {
                callback.onError(); // 1
                Log.d("BlogRepository", "loadDataFromNetwork: ");
            } else {
                BlogDAO blogDAO = database.blogDao();
                blogDAO.deleteAll(); // 2
                blogDAO.insertAll(blogList); // 3
                callback.onSuccess(blogList); // 4
            }
        });
    }
}
