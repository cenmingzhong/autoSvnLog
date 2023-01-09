/*
 * @(#)svnLog4DailyReport.java    Created on 2016年11月18日
 * Copyright (c) 2016 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package start;

import java.util.List;

/**
 * @author wangk
 * @version $Revision: 1.0 $, $Date: 2016年11月18日 下午4:47:13 $
 */
public class svnLog4DailyReport {
    // http://192.168.0.2/svn/repos/demo-keel/branches/keeldemo_wangk
    // http://192.168.0.2/svn/repos/etoh2/branches/etoh_nx_base2.9.0.0
    private static String svnUrl = "https://desktop-qrln642:4433/svn/trustmo/trunk/svnLogAutoGenerate";
    private static String svnUserName = "admin";
    private static String svnPassword = "admin";
    private static String localUrl = "E:/工作日报/";
    private static final String author = "admin";

    public static void main(String[] args) throws Exception {

        System.out.println("Author:cenmingzhong");
        System.out.println("Email:2637523025@qq.com");
        System.out.println("Enjoy~~~\n");


        System.setProperty("file.encoding", "ANSI");

        Config config = new Config();
        ConfigModel configModel = config.getConfig();
        SingleModel instance = SingleModel.getInstance();
        instance.setConfigModel(configModel);

        String svnUrl = configModel.getRepos().get(0);
        String svnUserName = configModel.getSvnUserName();
        String svnPassword = configModel.getSvnPassword();
        String exportLogUrl = configModel.getExportLogUrl();
        List<String> authors= configModel.getAuthors();



        System.out.println("工作日报正在生成，请稍后...");
        ExportSvnLog exportSvnLog = new ExportSvnLog();
        // 连接
        exportSvnLog.setupLibrary(svnUrl, svnUserName, svnPassword);
        System.out.println("地址和身份验证成功...");
        // 查找Log
        exportSvnLog.filterCommitHistoryTest(svnUrl, authors, exportLogUrl);
        System.out.println("完成");
    }
}
