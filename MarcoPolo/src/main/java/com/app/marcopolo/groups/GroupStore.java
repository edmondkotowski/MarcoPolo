package com.app.marcopolo.groups;

import android.content.Context;
import com.app.marcopolo.FileNameProvider;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cdockter on 5/17/2014.
 */
public class GroupStore {
    private final Context _context;
    private final FileNameProvider _fileNameProvider;

    public GroupStore(final Context context, final FileNameProvider fileNameProvider) {
        _context = context;
        _fileNameProvider = fileNameProvider;
    }

    public List<String> getGroupNames() {
        List<String> result = new ArrayList<>();
        String[] files = _context.fileList();
        for (String file : files) {
            if(_fileNameProvider.isFile(file)) {
                String groupName = _fileNameProvider.getRawName(file);
                result.add(groupName);
            }
        }
        return result;
    }

    public FriendGroup getGroup(String name) throws IOException, ClassNotFoundException {
        String fileName = _fileNameProvider.getFileName(name);
        FileInputStream file = _context.openFileInput(fileName);
        ObjectInputStream reader = new ObjectInputStream(file);
        FriendGroup group = (FriendGroup) reader.readObject();
        return group;
    }

    public void put(FriendGroup group) throws IOException {
        String fileName = _fileNameProvider.getFileName(group.getDisplayName());
        FileOutputStream file = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream writer = new ObjectOutputStream(file);
        writer.writeObject(group);
    }
}
