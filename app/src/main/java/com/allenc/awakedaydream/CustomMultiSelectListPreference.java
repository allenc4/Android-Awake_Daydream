package com.allenc.awakedaydream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by allenc4 on 2/27/2016.
 */
public class CustomMultiSelectListPreference extends MultiSelectListPreference {

    public CustomMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<Package> packages = new ArrayList<>();
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entryValues = new ArrayList<>();

        final PackageManager pm = Base.getAppContext().getPackageManager();
        // Get a list of installed apps.
        List<ApplicationInfo> t_packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : t_packages) {
            Package tPackage = new Package();
            tPackage.label = pm.getApplicationLabel(packageInfo).toString();
            tPackage.packageName = packageInfo.packageName;
            packages.add(tPackage);
        }

        Collections.sort(packages);
        for (Package tPackage : packages) {
            entries.add(tPackage.label);
            entryValues.add(tPackage.packageName);
        }

        setEntries(entries.toArray(new CharSequence[]{}));
        setEntryValues(entryValues.toArray(new CharSequence[]{}));
    }

    private class Package implements Comparable<Package>{
        String label;
        String packageName;

        @Override
        public int compareTo(Package p2) {
            return this.label.compareTo(p2.label);
        }

    }

    private class PackageComparator implements Comparator<Package> {
        @Override
        public int compare(Package p1, Package p2) {
            return p1.compareTo(p2);
        }
    }

}

