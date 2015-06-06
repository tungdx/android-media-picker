package vn.tungdx.mediapicker.utils;

import android.os.FileObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Same {@link FileObserver} but support recursive in folder.
 */
public class RecursiveFileObserver extends FileObserver {
    /**
     * Listener for event create file.
     */
    public interface OnFileCreatedListener {
        public void onFileCreate(File file);
    }

    private OnFileCreatedListener mFileCreatedListener;

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;

    public RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    public void setFileCreatedListener(OnFileCreatedListener listener) {
        this.mFileCreatedListener = listener;
    }

    @Override
    public void startWatching() {
        if (mObservers != null)
            return;
        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.empty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (files == null)
                continue;
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory() && !files[i].getName().equals(".")
                        && !files[i].getName().equals("..")) {
                    stack.push(files[i].getPath());
                }
            }
        }
        for (int i = 0; i < mObservers.size(); i++)
            mObservers.get(i).startWatching();
    }

    @Override
    public void stopWatching() {
        if (mObservers == null)
            return;

        for (int i = 0; i < mObservers.size(); ++i)
            mObservers.get(i).stopWatching();

        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String path) {
        if (event == CREATE) {
            if (mFileCreatedListener != null) {
                mFileCreatedListener.onFileCreate(new File(path));
            }
        }
    }

    private class SingleFileObserver extends FileObserver {
        private String mPath;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }

    }
}