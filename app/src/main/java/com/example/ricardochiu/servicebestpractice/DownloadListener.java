package com.example.ricardochiu.servicebestpractice;

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCancelled();
}
