package com.example.lenovo.networktools.utils.command;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.lenovo.networktools.utils.FileUtils;


/**
 * GeneralCommand
 * Created by 10129302 on 15-2-6.
 */
public class GeneralCommand
{

    private static final String tag = GeneralCommand.class.getSimpleName();

    /**
     * 简单的请求root权限
     */
    public static void root()
    {
        CommandExecutor.root();
    }

    /**
     * 对于root的手机进行adb connect
     *
     */
    public static void AdbConnect()
    {
        String[] commands = new String[3];
        commands[0] = "stop adbd";
        commands[1] = "setprop service.adb.tcp.port 5555";
        commands[2] = "start adbd";

        CommandExecutor.execCmd(commands);
    }

    public static void chmod(String dir)
    {
        String command = "chmod 777 " + dir;
        CommandExecutor.execCmd(command);
    }

    /**
     * 手机抓包程序
     * 
     */
    public static void TcpDump(Context context)
    {
        chmod("data/local");

        String TCP_DUMP_NAME = "tcpdump";
        File file = new File("data/local/", TCP_DUMP_NAME);

        if (file.exists())
        {
            return;
        }

        InputStream is;
        OutputStream os;
        AssetManager am = context.getAssets();
        try
        {
            is = am.open(TCP_DUMP_NAME);
            os = new FileOutputStream(file);
            FileUtils.copyStream(is, os);

            FileUtils.closeSafely(is);
            FileUtils.closeSafely(os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void startTcpDump(Context context, String name)
    {
        GeneralCommand.TcpDump(context);

        String[] commands = new String[7];
        commands[0] = "adb shell";
        commands[1] = "su";
        commands[4] = "chmod 777 /data/local/tcpdump";
        commands[5] = "cd /data/local";
        commands[6] = "tcpdump -p -vv -s 0 -w " + name;

        CommandExecutor.execCmd(commands);
    }

    public static void stopTcpDump()
    {
        String[] commands = new String[2];
        commands[0] = "adb shell";
        commands[1] = "ps|grep tcpdump|grep root|awk '{print $2}'";
        Process process = CommandExecutor.execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        if (!TextUtils.isEmpty(result))
        {
            String[] pids = result.split("\n");
            String[] killCmds = new String[pids.length];
            for (int i = 0; i < pids.length; ++i)
            {
                killCmds[i] = "kill -9 " + pids[i];
            }
            CommandExecutor.execCmd(killCmds);
        }
    }

    private static String parseInputStream(InputStream is)
    {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();
        try
        {
            while ((line = br.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
