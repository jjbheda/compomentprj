package qiyi.compomentprj;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

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
                    setapkClassloaderAndLoadRes();
                }
            }
        });
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
            Log.e("TAG",e.toString());
        }
        Log.e("TAG",apkFilePath);
        return apkFilePath;
    }

    public List<String> getSoApkPathList(){
        List<String> apkList = new ArrayList<>();
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
        apkList.add("qiyi_scan.so");
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
