package com.iboard.tusm.iboardmstar;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FtpServerlet extends DefaultFtplet {

    private FtpServer mFtpServer;

    private final int mPort = 2121;

    private final String mDirectory = Environment.getExternalStorageDirectory().getPath() + "/FtpFileTest";

    private  String mUser = "";

    private  String mPassword = "";

    private static FtpServerlet mInstance;

    private Context context;

    public static FtpServerlet getInstance() {
        if (mInstance == null) {
            mInstance = new FtpServerlet();
        }
        return mInstance;
    }

    public void SetUser(String user, String password,Context mContext){
        context = mContext;
        mUser = user;
        mPassword = password;
    }

    /**
     * FTP启动
     *
     * @throws FtpException
     */
    public void start() throws FtpException {

        if (null != mFtpServer && false == mFtpServer.isStopped()) {
            return;
        }

        File file = new File(mDirectory);
        if (!file.exists()) {
            file.mkdirs();
        }

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();

        // 设定端末番号
        listenerFactory.setPort(mPort);

        // 通过PropertiesUserManagerFactory创建UserManager然后向配置文件添加用户
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        UserManager userManager = userManagerFactory.createUserManager();

        List<Authority> auths = new ArrayList<Authority>();
        Authority auth = new WritePermission();
        auths.add(auth);

        //添加用户
        BaseUser user = new BaseUser();
        user.setName(mUser);
        user.setPassword(mPassword);
        user.setHomeDirectory(mDirectory);
        user.setAuthorities(auths);
        userManager.save(user);

        // 设定Ftplet
        Map<String, Ftplet> ftpletMap = new HashMap<String, Ftplet>();
        ftpletMap.put("Ftplet", this);

        serverFactory.setUserManager(userManager);
        serverFactory.addListener("default", listenerFactory.createListener());
        serverFactory.setFtplets(ftpletMap);

        // 创建并启动FTPServer
        mFtpServer = serverFactory.createServer();
        mFtpServer.start();
    }

    /**
     * FTP停止
     */
    public void stop() {

        // FtpServer不存在和FtpServer正在运行中
        if (null != mFtpServer && false == mFtpServer.isStopped()) {
            mFtpServer.stop();
        }
    }

    @Override
    public FtpletResult onAppendStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        System.out.println("onAppendStart");
        return super.onAppendStart(session, request);
    }

    @Override
    public FtpletResult onAppendEnd(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        System.out.println("onAppendEnd");
        return super.onAppendEnd(session, request);
    }

    @Override
    public FtpletResult onLogin(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        System.out.println("onLogin");
        return super.onLogin(session, request);
    }

    @Override
    public FtpletResult onConnect(FtpSession session) throws FtpException,
            IOException {
        System.out.println("onConnect");
        return super.onConnect(session);
    }

    @Override
    public FtpletResult onDisconnect(FtpSession session) throws FtpException,
            IOException {
        System.out.println("onDisconnect");
        return super.onDisconnect(session);
    }

    @Override
    public FtpletResult onUploadStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        System.out.println("onUploadStart");
        return super.onUploadStart(session, request);
    }

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        String FtpUploadPath = mDirectory + "/" + request.getArgument();
        String ExtensionName = ImagicUtill.getExtensionName(FtpUploadPath);
        //接收到文件后立即删除
       Log.i("ftp","FtpUploadPath======"+FtpUploadPath+"======ExtensionName"+ExtensionName);
        if (ExtensionName.equalsIgnoreCase("mp4")
                ||
                ExtensionName.equalsIgnoreCase("mov")
                ||
                ExtensionName.equalsIgnoreCase("3gp")) {
            Log.i("ftp","go in ");
            Intent intent = new Intent();
            intent.setClass(context, MediaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("media", FtpUploadPath);
            context.startActivity(intent);


        }
        return super.onUploadEnd(session, request);
    }
}