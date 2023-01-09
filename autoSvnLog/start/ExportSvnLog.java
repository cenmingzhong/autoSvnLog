/*
 * @(#)ExportSvnLog.java    Created on 2016年11月18日
 * Copyright (c) 2016 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package start;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.sun.istack.internal.NotNull;
import org.apache.commons.io.IOUtils;
import org.tmatesoft.svn.cli.SVN;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * @author wangk
 * @version $Revision: 1.0 $, $Date: 2016年11月18日 下午4:52:10 $
 */
public class ExportSvnLog {

    private static SVNRepository repository = null;

    public void setupLibrary(String url, String userName, String password) {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        }
        catch (SVNException e) {
        }
        // 身份验证
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
        repository.setAuthenticationManager(authManager);
    }

    public void filterCommitHistoryTest(String url, final List<String> authors, String localUrl) throws Exception {
        // 过滤条件
        // 时间
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ConfigModel configModel = SingleModel.getInstance().getConfigModel();
        List<String> dateRange = configModel.getDateRange();


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date begin = calendar.getTime();
        Date end = new Date();
        if (dateRange != null && !dateRange.isEmpty()) {
            begin = format.parse(dateRange.get(0));
            end = format.parse(dateRange.get(1));
        }

        // 版本
        long startRevision = 0;
        long endRevision = -1;// 表示最后一个版本

        List<String> history = new ArrayList<String>();
        // String[] 为过滤的文件路径前缀，为空表示不进行过滤

        Date finalBegin = begin;
        Date finalEnd = end;
        repository.log(new String[] { "" }, startRevision, endRevision, true, true, new ISVNLogEntryHandler() {
            @Override
            public void handleLogEntry(SVNLogEntry svnlogentry) throws SVNException {
                // 依据提交时间进行过滤
                if (svnlogentry.getDate().after(finalBegin) && svnlogentry.getDate().before(finalEnd)) {
                    for (String author : authors) {
                        // 依据提交人过滤
                        if (!"".equals(author)) {
                            if (author.equals(svnlogentry.getAuthor())) {
                                fillResult(svnlogentry);
                            }
                        }
                        else {
                            fillResult(svnlogentry);
                        }
                    }
                }
            }

            public void fillResult(SVNLogEntry svnlogentry) {
                // getChangedPaths为提交的历史记录MAP key为文件名，value为文件详情
                // history.addAll(svnlogentry.getChangedPaths().keySet());
                history.add(svnlogentry.getMessage());
            }
        });
        File file = new File(localUrl + format.format(end) + ".txt");
        createFile4Log(file, url, history);
    }

    private void createFile4Log(File file, String url, List<String> history) throws IOException {
        if (!file.getParentFile().exists() || !file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }
        StringBuilder sb = printLog(history);
        String content = sb.toString();

        FileUtil.writeUtf8String(content, file);
        System.out.println("工作日报已输出到" + file.getAbsolutePath());

    }

    @NotNull
    private StringBuilder printLog(List<String> history) {
        String formatDate = getFormatDate();
        StringBuilder sb = new StringBuilder();
        sb.append("工作日报 ").append(formatDate).append("\n");
        sb.append("一、昨天工作（进度/问题）").append("\n");
        sb.append("(一)进度").append("\n");

        ConfigModel configModel = SingleModel.getInstance().getConfigModel();
        List<String> repos = configModel.getRepos();
        for (String repo : repos) {
            int lastSlashIndex = repo.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                String fileName = repo.substring(lastSlashIndex + 1);
                System.out.println("目标SVN文件夹：" + fileName);  // 输出: "file.txt"
                String formatName = getFormatName(configModel, fileName);

                sb.append(formatName).append("\n");
                int i = 1;

                boolean isEn = configModel.getLang().equals("en");

                Map<String, List<FormatModel>> stringListMap = filterHistory(history, isEn);
                for (String key : stringListMap.keySet()) {
                    int j=1;
                    sb.append(i).append(key).append("\n");
                    List<FormatModel> formatModels = stringListMap.get(key);
                    for (FormatModel formatModel : formatModels) {
                        sb.append(i).append(".").append(j);
                        if (configModel.getShowRate().equals(Boolean.TRUE)) {
                            sb.append("【完成100%】 ");
                        }
                        sb.append(formatModel.getContent()).append("\n");
                        j++;
                    }
                    sb.append("\n");
                    i++;
                }
            }
        }
        sb.append("\n");

        sb.append("(二)问题").append("\n");
        sb.append("无").append("\n");
        sb.append("\n");

        sb.append("二、今天安排").append("\n");
        sb.append("1.");
        return sb;
    }

    /**
     * 得到格式名称
     *
     * @param configModel 配置模型
     * @param fileName    文件名称
     * @return {@link String}
     */
    private String getFormatName(ConfigModel configModel, String fileName) {
        String formatName = fileName;
        List<String> format = configModel.getFormat();
        for (String s : format) {
            String[] split = s.split("-");
            if (StrUtil.isNotBlank(split[0]) && split[0].equals(fileName)) {
                formatName = split[1];
            }
        }
        return formatName;
    }

    /**
     * 获取日期格式
     *
     * @return {@link String}
     */
    private String getFormatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date();
        String formattedDate = sdf.format(date);
        System.out.println("当前日期：" + formattedDate);
        return formattedDate;
    }

    /**
     * 过滤器历史
     *
     * @param history 历史
     * @param isEN    是在
     * @return {@link Map}<{@link String}, {@link List}<{@link FormatModel}>>
     */
    private Map<String, List<FormatModel>>  filterHistory(List<String> history, boolean isEN) {
        // 过滤需要的
        ConfigModel configModel = SingleModel.getInstance().getConfigModel();
        List<String> includes = configModel.getIncludes();
        // 过滤不需要的
        List<String> excludes = configModel.getExcludes();
        List<String> need = new ArrayList<>();
        boolean needFlag = null != includes && !includes.isEmpty();
        boolean notNeedFlag = null != excludes && !excludes.isEmpty();

        if (needFlag){
            need = history.stream().filter(x -> includes.stream().anyMatch(y -> x.contains(y))).collect(Collectors.toList());
        } else if (!needFlag && notNeedFlag) {
            need = history.stream().filter(x -> excludes.stream().noneMatch(y -> x.contains(y))).collect(Collectors.toList());
        }


        List<FormatModel> formatModels = new ArrayList<>();
        for (String commit : need) {
            FormatModel formatModel = formatCommit(commit, isEN);
            formatModels.add(formatModel);
        }
        return formatModels.stream().collect(Collectors.groupingBy(FormatModel::getCategory));
    }

    /**
     * 格式提交
     *
     * @param commit 提交
     * @param isEN   是在
     * @return {@link FormatModel}
     */
    private FormatModel formatCommit(String commit, boolean isEN) {
        String type = "chore";
        String category = isEN ? "Chores" : "其他优化";
        if (commit.startsWith("feat")) {
            type = "feat";
            category = isEN ? "Features" : "功能开发";
        }
        if (commit.startsWith("fix")) {
            type = "fix";
            category = isEN ? "Bug Fixes" : "BUG修复";
        }
        if (commit.startsWith("docs")) {
            type = "docs";
            category = isEN ? "Documentation" : "完善文档";
        }
        if (commit.startsWith("style")) {
            type = "style";
            category = isEN ? "Optimized Style" : "优化样式";
        }
        if (commit.startsWith("refactor")) {
            type = "refactor";
            category = isEN ? "Refactored" : "代码重构";
        }
        if (commit.startsWith("test")) {
            type = "test";
            category = isEN ? "Test Cases" : "测试用例";
        }
        String content = commit;
        if (commit.contains(":") || commit.contains("：")) {
            content = commit.substring(type.length() + 1);
        }
        FormatModel formatModel = new FormatModel();
        formatModel.setType(type);
        formatModel.setCategory(category);
        formatModel.setContent(content);
        return formatModel;
    }
}
