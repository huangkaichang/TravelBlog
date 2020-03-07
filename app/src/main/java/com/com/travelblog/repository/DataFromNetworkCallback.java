package com.com.travelblog.repository;

import com.example.travelblog.Blog;

import java.util.List;

public interface DataFromNetworkCallback {
    void onSuccess(List<Blog> blogList);
    void onError();
}
