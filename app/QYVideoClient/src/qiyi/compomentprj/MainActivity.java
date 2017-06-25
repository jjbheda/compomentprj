package qiyi.compomentprj;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import qiyi.bundle.framework.BundleCore;
import qiyi.bundle.framework.BundleException;

public class MainActivity extends Activity {
    private Button mOpenPluginBtn;
    private boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOpenPluginBtn = (Button) findViewById(R.id.open_plugin);
        mOpenPluginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    try {
                        startActivity(new Intent(getApplicationContext(), Class.forName("qiyi.scan.ScanMainActivity")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    setapkClassloaderAndLoadRes2();
                }
            }
        });

        findViewById(R.id.open_sec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (flag) {
                    try {
                        startActivity(new Intent(getApplicationContext(), Class.forName("qiyi.compomentprj.Main_SecActivity")));
                    } catch (Exception e) {
                        e.printStackTrace();
//                    }

                }
//                else {
//                    setapkClassloaderAndLoadRes2();
//                }
            }
        });
    }


    private boolean setapkClassloaderAndLoadRes2() {
        SharedPreferences sharedPreferences;
        boolean isDexInstalled = true;
        final String bundleKey;
        try {
            BundleCore.getInstance().init(getApplication());
            BundleCore.getInstance().ConfigLogger(true, 1);
            Properties properties = new Properties();
            properties.put("ctrip.android.sample.welcome", "ctrip.android.sample.WelcomeActivity"); // launch page
            sharedPreferences = getSharedPreferences("bundlecore_configs", 0);
            String lastBundleKey = sharedPreferences.getString("last_bundle_key", "");
            bundleKey = buildBundleKey();
            if (!TextUtils.equals(bundleKey, lastBundleKey)) {
                properties.put("ctrip.bundle.init", "true");
                isDexInstalled = false;
            }
            BundleCore.getInstance().startup(properties);
            if (isDexInstalled) {
                BundleCore.getInstance().run();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ZipFile zipFile = new ZipFile(getApplicationInfo().sourceDir);
                            List bundleFiles = getBundleEntryNames(zipFile, BundleCore.LIB_PATH, ".so");
                            if (bundleFiles != null && bundleFiles.size() > 0) {
                                processLibsBundles(zipFile, bundleFiles);
                                SharedPreferences.Editor edit = getSharedPreferences("bundlecore_configs", 0).edit();
                                edit.putString("last_bundle_key", bundleKey);
                                edit.commit();
                            } else {
                                Log.e("Error Bundle", "not found bundle in apk");
                            }
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            BundleCore.getInstance().run();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        flag = true;
        return flag;
    }

    private String buildBundleKey() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
        return String.valueOf(packageInfo.versionCode) + "_" + packageInfo.versionName;
    }

    private List<String> getBundleEntryNames(ZipFile zipFile, String str, String str2) {
        List<String> arrayList = new ArrayList();
        try {
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                String name = ((ZipEntry) entries.nextElement()).getName();
                if (name.startsWith(str) && name.endsWith(str2)) {
                    arrayList.add(name);
                }
            }
        } catch (Throwable e) {
            Log.e("getBundleEntryNames", "Exception while get bundles in assets or lib", e);
        }
        return arrayList;
    }

    private void processLibsBundles(ZipFile zipFile, List<String> list) {

        for (String str : list) {
            processLibsBundle(zipFile, str);
        }
    }

    private boolean processLibsBundle(ZipFile zipFile, String str) {

        String packageNameFromEntryName = getPackageNameFromEntryName(str);

        if (BundleCore.getInstance().getBundle(packageNameFromEntryName) == null) {
            try {
                BundleCore.getInstance().installBundle(packageNameFromEntryName, zipFile.getInputStream(zipFile.getEntry(str)));
                Log.e("Succeed install", "Succeed to install bundle " + packageNameFromEntryName);
                return true;
            } catch (BundleException ex) {
                Log.e("Fail install", "Could not install bundle.", ex);
            } catch (IOException iex) {
                Log.e("Fail install", "Could not install bundle.", iex);
            }
        }
        return false;
    }

    private String getPackageNameFromEntryName(String entryName) {
        return entryName.substring(entryName.indexOf(BundleCore.LIB_PATH) + BundleCore.LIB_PATH.length(), entryName.indexOf(".so")).replace("_", ".");
    }

    private boolean setapkClassloaderAndLoadRes() {
        try {
            String cachePath = MainActivity.this.getCacheDir().getAbsolutePath();
            String apkPath = getSoFilePath(0);
            DexClassLoader mClassLoader = new DexClassLoader(apkPath, cachePath, cachePath, getClassLoader());
            HookUtil.inject(mClassLoader);
            flag = true;
            loadResource(apkPath);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "加载完成", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**从assetmanager目录中读取apk文件
     */
    public String getSoFilePath(int dex){
        List<String> soFileList = getSoApkPathList ();
        File apkDir = new File(getFilesDir(),"apkDir");
        apkDir.mkdir();
        String apkFilePath = "";
        try{
            File apkFile = new File(apkDir, soFileList.get(dex));
            InputStream ins = getAssets().open(soFileList.get(dex));
            Log.e("TAG","打开文件");
            if(apkFile.length()!=ins.available()){
                Log.e("TAG","开始读");
                FileOutputStream fos = new FileOutputStream(apkFile);
                byte[] buf = new byte[2048];
                int l;
                while((l=ins.read(buf))!=-1){
                    fos.write(buf,0,l);
                }
                fos.close();
            }
            ins.close();
            apkFilePath = apkFile.getAbsolutePath();
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG","读取发生异常");
            Log.e("TAG",e.toString());
        }
        Log.e("TAG",apkFilePath);
        return apkFilePath;
    }

    public List<String> getSoApkPathList(){
        List<String> apkList = new ArrayList<String>();
//        try {
//            String files[] = getAssets().list("");
//            for(String fName:files){
//                if (fName.endsWith(".so"))
//                    apkList.add(fName);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.e("TAG",apkList.toString());
        apkList.add("baseres/qiyi_scan.so");
        return apkList;
    }

    //资源加载
    public void loadResource(String soFilePath){
        try {
            AssetManager assetManager= getAssets();      //用主app自己的assetManager 实现资源混用
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, soFilePath);
            Method ensureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocks.setAccessible(true);
            ensureStringBlocks.invoke(assetManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}
