package com.iboard.tusm.iboardmstar;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by tusm on 17/11/21.
 */

public class ApplicationManager {
    public final int INSTALL_REPLACE_EXISTING = 2;



    private PackageDeleteObserver observerdelete;
    private PackageManager pm;
    private Method method;
    private Method uninstallmethod;

    private OnPackagedObserver onInstalledPackaged;



    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            if (onInstalledPackaged != null) {
                onInstalledPackaged.packageDeleted(packageName, returnCode);
            }
        }
    }

    public ApplicationManager(Context context) throws SecurityException, NoSuchMethodException {


        observerdelete = new PackageDeleteObserver();
        pm = context.getPackageManager();

        Class<?>[] uninstalltypes = new Class[] {String.class, IPackageDeleteObserver.class, int.class};

        uninstallmethod = pm.getClass().getMethod("deletePackage", uninstalltypes);
    }

    public void setOnPackagedObserver(OnPackagedObserver onInstalledPackaged) {
        this.onInstalledPackaged = onInstalledPackaged;
    }



    public void uninstallPackage(String packagename) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        uninstallmethod.invoke(pm, new Object[] {packagename, observerdelete, 0});
    }




}
