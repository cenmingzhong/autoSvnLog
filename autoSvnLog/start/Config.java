package start;


import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    public ConfigModel getConfig() {
        // 获取当前工作目录
        String currentDirectory = System.getProperty("user.dir");
        // 使用 Paths 的静态方法 get() 来获取相对路径
        Path parent = Paths.get(currentDirectory).getParent();
        // 相对路径是相对于当前工作目录的
        java.nio.file.Path path = Paths.get(currentDirectory + "/config.json");
        java.nio.file.Path path1 = Paths.get(parent + "/config.json");
        java.nio.file.Path targetPath = path1;
        if (FileUtil.exist(path1.toFile())) {
            targetPath = path1;
        } else if (FileUtil.exist(path.toFile())) {
            targetPath = path;
        }
        // 使用 toAbsolutePath() 方法将相对路径转换为绝对路径
        java.nio.file.Path absolutePath = targetPath.toAbsolutePath();
        System.out.println("Absolute path: " + absolutePath);

        File confJson = FileUtil.file(absolutePath.toString());
        if (FileUtil.exist(confJson)) {
            JSONObject jsonObject = JSONUtil.readJSONObject(confJson, Charset.defaultCharset());
            ConfigModel configModel = JSONUtil.toBean(jsonObject, ConfigModel.class);
            System.out.println(configModel.toString());
            return configModel;
        }
        return null;
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.getConfig();
    }
}
